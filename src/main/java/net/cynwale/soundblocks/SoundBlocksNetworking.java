package net.cynwale.soundblocks;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SoundBlocksNetworking {
    public static final ResourceLocation SOUND_BLOCK_SYNC_ID = new ResourceLocation(SoundBlocks.MOD_ID, "sound_block_sync");
    public static final ResourceLocation OPEN_CONFIG_SCREEN_ID = new ResourceLocation(SoundBlocks.MOD_ID, "open_config_screen");

    public static final ResourceLocation SYNC_LOOP_START = new ResourceLocation(SoundBlocks.MOD_ID, "sync_loop_start");
    public static final ResourceLocation SYNC_LOOP_STOP = new ResourceLocation(SoundBlocks.MOD_ID, "sync_loop_stop");
    public static final ResourceLocation SYNC_LOOP_VOLUME = new ResourceLocation(SoundBlocks.MOD_ID, "sync_loop_volume");
    public static final ResourceLocation SYNC_LOOP_PITCH = new ResourceLocation(SoundBlocks.MOD_ID, "sync_loop_pitch");
    public static final ResourceLocation SYNC_LOOP_RANGE = new ResourceLocation(SoundBlocks.MOD_ID, "sync_loop_range");
    public static final ResourceLocation SYNC_LOOP_DELAY = new ResourceLocation(SoundBlocks.MOD_ID, "sync_loop_delay");

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_LOOP_START, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String soundId = buf.readUtf();
            float volume = buf.readFloat();
            float pitch = buf.readFloat();
            int range = buf.readInt();
            int delay = buf.readInt();
            boolean isLooping = buf.readBoolean(); // Read Boolean

            client.execute(() -> SoundBlockClientManager.startLoop(pos, soundId, volume, pitch, range, delay, isLooping));
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_LOOP_STOP, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> SoundBlockClientManager.stopLoop(pos));
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_LOOP_VOLUME, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            float volume = buf.readFloat();
            client.execute(() -> SoundBlockClientManager.updateVolume(pos, volume));
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_LOOP_PITCH, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            float pitch = buf.readFloat();
            client.execute(() -> SoundBlockClientManager.updatePitch(pos, pitch));
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_LOOP_RANGE, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int range = buf.readInt();
            client.execute(() -> SoundBlockClientManager.updateRange(pos, range));
        });

        ClientPlayNetworking.registerGlobalReceiver(SYNC_LOOP_DELAY, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int delay = buf.readInt();
            client.execute(() -> SoundBlockClientManager.updateDelay(pos, delay));
        });
    }

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SOUND_BLOCK_SYNC_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            String newSoundId = buf.readUtf();
            boolean newLooping = buf.readBoolean();
            boolean newPoweredByGui = buf.readBoolean();
            float newVolume = buf.readFloat();
            float newPitch = buf.readFloat();
            int newRange = buf.readInt();
            int newDelay = buf.readInt();

            server.execute(() -> {
                if (player.level().isLoaded(pos) && player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64) {
                    if (player.level().getBlockEntity(pos) instanceof SoundBlockEntity soundBlockEntity) {
                        soundBlockEntity.setSoundId(newSoundId);
                        soundBlockEntity.setLooping(newLooping);
                        soundBlockEntity.setPoweredByGui(newPoweredByGui);
                        soundBlockEntity.setVolume(newVolume);
                        soundBlockEntity.setPitch(newPitch);
                        soundBlockEntity.setRange(newRange);
                        soundBlockEntity.setRandomDelay(newDelay);

                        BlockState currentState = player.level().getBlockState(pos);
                        if (currentState.getBlock() instanceof SoundBlock soundBlock) {
                            boolean isRedstonePowered = player.level().hasNeighborSignal(pos);
                            boolean shouldBePowered = newPoweredByGui || isRedstonePowered;
                            boolean isCurrentlyPowered = currentState.getValue(SoundBlock.POWERED);

                            // 1. Live Updates
                            if (isCurrentlyPowered) {
                                soundBlock.updateBlockVolume(player.level(), pos, newVolume);
                                soundBlock.updateBlockPitch(player.level(), pos, newPitch);
                                soundBlock.updateBlockRange(player.level(), pos, newRange);
                                soundBlock.updateBlockDelay(player.level(), pos, newDelay);
                            }

                            // 2. State Logic
                            if (shouldBePowered != isCurrentlyPowered) {
                                if (shouldBePowered) {
                                    soundBlock.playBlockSound(player.level(), pos);
                                    player.level().setBlock(pos, currentState.setValue(SoundBlock.POWERED, true), 3);
                                } else {
                                    soundBlock.stopBlockSound(player.level(), pos);
                                    player.level().setBlock(pos, currentState.setValue(SoundBlock.POWERED, false), 3);
                                }
                            }
                            else if (isCurrentlyPowered) {
                                // Restart if needed (mainly for ID changes)
                                soundBlock.stopBlockSound(player.level(), pos);
                                soundBlock.playBlockSound(player.level(), pos);
                            }
                        }
                    }
                }
            });
        });
    }
}