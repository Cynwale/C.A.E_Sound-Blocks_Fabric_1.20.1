package net.cynwale.soundblocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class SoundBlocksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 1. Initialize the Audio Logic Tick Handler
        SoundBlockClientManager.init();

        // 2. Register the Custom Renderer (for the icon when holding the block)
        BlockEntityRendererRegistry.register(ModBlocks.SOUND_BLOCK_ENTITY, SoundBlockRenderer::new);

        // 3. Register Networking (Packets from Server -> Client)
        SoundBlocksNetworking.registerS2CPackets();

        // 4. Register the GUI Open packet
        ClientPlayNetworking.registerGlobalReceiver(SoundBlocksNetworking.OPEN_CONFIG_SCREEN_ID, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String syncedSoundId = buf.readUtf();
            boolean syncedLooping = buf.readBoolean();
            boolean syncedPoweredByGui = buf.readBoolean();
            float syncedVolume = buf.readFloat();
            float syncedPitch = buf.readFloat();
            int syncedRange = buf.readInt();
            int syncedDelay = buf.readInt();

            client.execute(() -> {
                if (client.level.getBlockEntity(pos) instanceof SoundBlockEntity entity) {
                    entity.setSoundId(syncedSoundId);
                    entity.setLooping(syncedLooping);
                    entity.setPoweredByGui(syncedPoweredByGui);
                    entity.setVolume(syncedVolume);
                    entity.setPitch(syncedPitch);
                    entity.setRange(syncedRange);
                    entity.setRandomDelay(syncedDelay);

                    Minecraft.getInstance().setScreen(SoundBlockConfigScreen.create(entity));
                }
            });
        });
    }
}