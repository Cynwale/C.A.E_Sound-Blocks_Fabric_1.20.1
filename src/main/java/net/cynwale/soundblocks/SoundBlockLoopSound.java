package net.cynwale.soundblocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class SoundBlockLoopSound extends AbstractTickableSoundInstance {
    private final BlockPos pos;
    private boolean stopped = false;

    private float baseVolume = 1.0F;
    private int range = 16;

    public SoundBlockLoopSound(SoundEvent soundEvent, BlockPos pos) {
        super(soundEvent, SoundSource.BLOCKS, RandomSource.create());
        this.pos = pos;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;

        this.attenuation = Attenuation.NONE;
    }

    public void setBaseVolume(float volume) {
        this.baseVolume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setLooping(boolean loop) {
        this.looping = loop;
    }

    @Override
    public void tick() {
        if (this.stopped) {
            this.stop();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            double distSqr = player.distanceToSqr(this.x, this.y, this.z);
            float distance = (float) Math.sqrt(distSqr);

            // OPTIMIZATION: If player is VERY far (Range + 32 blocks buffer), stop to save memory.
            // The BlockEntity ticker will restart it when we get close.
            if (distance > this.range + 32) {
                // Only stop if it's an infinite loop (Delay=0).
                // If it's a One-Shot or Random Delay, let it finish naturally.
                if (this.looping) {
                    // We call SoundBlockClientManager to clean up the map entry too
                    SoundBlockClientManager.stopLoop(this.pos);
                    return;
                }
            }

            if (distance > this.range) {
                this.volume = 0.0F;
            } else {
                float fade = 1.0F - (distance / (float) this.range);
                this.volume = this.baseVolume * fade;
            }
        }
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}