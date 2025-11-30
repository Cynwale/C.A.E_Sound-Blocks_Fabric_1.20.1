package net.cynwale.soundblocks.mixin;

import net.cynwale.soundblocks.CustomSoundResourcePack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Shadow private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RepositorySource[] sources, CallbackInfo ci) {
        // Define our custom source
        RepositorySource customSource = (consumer) -> {
            Pack pack = Pack.readMetaAndCreate(
                    "soundblocks_custom",
                    Component.literal("Sound Blocks Custom"),
                    true,
                    (path) -> new CustomSoundResourcePack(),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
            );
            if (pack != null) {
                consumer.accept(pack);
            }
        };

        // Add it to the set
        if (this.sources instanceof HashSet) {
            this.sources.add(customSource);
        } else {
            // Fallback: Re-wrap it if immutable
            this.sources = new HashSet<>(this.sources);
            this.sources.add(customSource);
        }
    }
}