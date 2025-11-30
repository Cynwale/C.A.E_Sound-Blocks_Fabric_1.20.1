package net.cynwale.soundblocks;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class CustomSoundResourcePack implements PackResources {

    private static final String NAMESPACE = SoundBlocks.MOD_ID;

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        if (!location.getNamespace().equals(NAMESPACE)) return null;

        String path = location.getPath();

        // 1. Handle Sound Files
        if (path.startsWith("sounds/") && path.endsWith(".ogg")) {
            // Extract ID: sounds/my_sound.ogg -> my_sound
            String soundId = path.substring(7, path.length() - 4);
            
            // Look up the REAL path from the manager
            Path filePath = CustomSoundManager.SOUND_FILE_MAP.get(soundId);

            if (filePath != null && Files.exists(filePath)) {
                return () -> new FileInputStream(filePath.toFile());
            }
        }

        if (path.equals("sounds.json")) {
            return this::generateSoundsJson;
        }

        return null;
    }

    private InputStream generateSoundsJson() {
        JsonObject root = new JsonObject();

        for (String soundId : CustomSoundManager.SOUND_IDS) {
            JsonObject entry = new JsonObject();
            entry.addProperty("category", "block"); // or "ambient"

            JsonArray sounds = new JsonArray();
            JsonObject soundObj = new JsonObject();
            soundObj.addProperty("name", NAMESPACE + ":" + soundId);
            
            // Streaming MUST be true for large/ambient files to avoid static/memory issues
            soundObj.addProperty("stream", true); 
            
            sounds.add(soundObj);

            entry.add("sounds", sounds);
            root.add(soundId, entry);
        }

        return new ByteArrayInputStream(root.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
        if (type == PackType.CLIENT_RESOURCES && namespace.equals(NAMESPACE) && path.equals("sounds")) {
            for (String id : CustomSoundManager.SOUND_IDS) {
                ResourceLocation loc = new ResourceLocation(NAMESPACE, "sounds/" + id + ".ogg");
                resourceOutput.accept(loc, getResource(type, loc));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> set = new HashSet<>();
        set.add(NAMESPACE);
        return set;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) {
        if (deserializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(
                Component.literal("Sound Blocks Custom Audio"),
                15 // Pack format for 1.20.1
            );
        }
        return null;
    }

    @Override
    public String packId() {
        return "SoundBlocksCustomSounds";
    }

    @Override
    public void close() { }

    public Component getTitle() {
        return Component.literal("Sound Blocks Custom Audio");
    }

    public boolean isFixed() {
        return true;
    }
}