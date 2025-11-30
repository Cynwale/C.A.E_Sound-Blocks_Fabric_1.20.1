package net.cynwale.soundblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.Minecraft; // Needed for client player check

public class SoundBlockEntity extends BlockEntity {

    private String soundId = "minecraft:entity.experience_orb.pickup";
    private boolean looping = false;
    private boolean poweredByGui = false;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private int range = 16;
    private int randomDelay = 0;

    public SoundBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SOUND_BLOCK_ENTITY, pos, state);
    }

    // --- Client Tick ---
    public static void clientTick(Level level, BlockPos pos, BlockState state, SoundBlockEntity entity) {
        if (level.getGameTime() % 20 == 0) {
            if (state.getValue(SoundBlock.POWERED)) {
                // OPTIMIZATION: Only start sound if player is close enough to hear it (or almost hear it)
                // Range + 32 blocks buffer
                if (Minecraft.getInstance().player != null) {
                    double distSqr = Minecraft.getInstance().player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                    double maxDist = entity.range + 32;
                    
                    if (distSqr < maxDist * maxDist) {
                        SoundBlockClientManager.startLoop(
                            pos, 
                            entity.soundId, 
                            entity.volume, 
                            entity.pitch, 
                            entity.range, 
                            entity.randomDelay, 
                            entity.looping
                        );
                    }
                }
            }
        }
    }

    // --- Getters & Setters ---
    public String getSoundId() { return soundId; }
    public void setSoundId(String soundId) { this.soundId = soundId; setChanged(); }

    public boolean isLooping() { return looping; }
    public void setLooping(boolean looping) { this.looping = looping; setChanged(); }

    public boolean isPoweredByGui() { return poweredByGui; }
    public void setPoweredByGui(boolean poweredByGui) { this.poweredByGui = poweredByGui; setChanged(); }

    public float getVolume() { return volume; }
    public void setVolume(float volume) { this.volume = volume; setChanged(); }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; setChanged(); }

    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; setChanged(); }

    public int getRandomDelay() { return randomDelay; }
    public void setRandomDelay(int randomDelay) { this.randomDelay = randomDelay; setChanged(); }

    // --- NBT Saving & Loading ---
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("SoundId", soundId);
        tag.putBoolean("Looping", looping);
        tag.putBoolean("PoweredByGui", poweredByGui);
        tag.putFloat("Volume", volume);
        tag.putFloat("Pitch", pitch);
        tag.putInt("Range", range);
        tag.putInt("RandomDelay", randomDelay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("SoundId")) this.soundId = tag.getString("SoundId");
        if (tag.contains("Looping")) this.looping = tag.getBoolean("Looping");
        if (tag.contains("PoweredByGui")) this.poweredByGui = tag.getBoolean("PoweredByGui");
        if (tag.contains("Volume")) this.volume = tag.getFloat("Volume");
        if (tag.contains("Pitch")) this.pitch = tag.getFloat("Pitch");
        if (tag.contains("Range")) this.range = tag.getInt("Range");
        if (tag.contains("RandomDelay")) this.randomDelay = tag.getInt("RandomDelay");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}