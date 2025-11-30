package net.cynwale.soundblocks;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SoundBlockClientManager {

    private static class LoopController {
        SoundBlockLoopSound activeSound;
        String soundId;
        float volume;
        float pitch;
        int range;
        int maxDelaySeconds;
        boolean shouldLoop;
        int ticksUntilNextPlay;
        final Random random = new Random();

        public LoopController(String soundId, float volume, float pitch, int range, int maxDelaySeconds, boolean shouldLoop) {
            this.soundId = soundId;
            this.volume = volume;
            this.pitch = pitch;
            this.range = range;
            this.maxDelaySeconds = maxDelaySeconds;
            this.shouldLoop = shouldLoop;
            this.ticksUntilNextPlay = 0;
        }
    }

    private static final Map<BlockPos, LoopController> CONTROLLERS = new HashMap<>();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    private static void tick() {
        CONTROLLERS.forEach((pos, ctrl) -> {
            boolean isPlaying = ctrl.activeSound != null && !ctrl.activeSound.isStopped();

            if (!isPlaying) {
                if (ctrl.shouldLoop) {
                    if (ctrl.maxDelaySeconds == 0) {
                        playSound(pos, ctrl, true);
                    } else {
                        if (ctrl.ticksUntilNextPlay > 0) {
                            ctrl.ticksUntilNextPlay--;
                        } else {
                            playSound(pos, ctrl, false);
                            int delayTicks = ctrl.random.nextInt(ctrl.maxDelaySeconds * 20 + 1);
                            ctrl.ticksUntilNextPlay = delayTicks;
                        }
                    }
                }
            }
        });

        CONTROLLERS.entrySet().removeIf(entry -> {
            LoopController ctrl = entry.getValue();
            boolean isPlaying = ctrl.activeSound != null && !ctrl.activeSound.isStopped();
            return !isPlaying && !ctrl.shouldLoop;
        });
    }

    private static void playSound(BlockPos pos, LoopController ctrl, boolean engineLoop) {
        try {
            ResourceLocation location = new ResourceLocation(ctrl.soundId);
            SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(location);

            ctrl.activeSound = new SoundBlockLoopSound(soundEvent, pos);
            ctrl.activeSound.setBaseVolume(ctrl.volume);
            ctrl.activeSound.setPitch(ctrl.pitch);
            ctrl.activeSound.setRange(ctrl.range);
            ctrl.activeSound.setLooping(engineLoop);

            Minecraft.getInstance().getSoundManager().play(ctrl.activeSound);

        } catch (Exception e) {
            System.out.println("Failed to play sound: " + ctrl.soundId);
        }
    }

    public static void startLoop(BlockPos pos, String soundId, float volume, float pitch, int range, int delay, boolean isLooping) {
        if (CONTROLLERS.containsKey(pos)) {
            // Optimization: Return early if controller exists to save object creation
            LoopController ctrl = CONTROLLERS.get(pos);
            ctrl.volume = volume;
            ctrl.pitch = pitch;
            ctrl.range = range;
            ctrl.maxDelaySeconds = delay;
            ctrl.shouldLoop = isLooping;
            return;
        }

        LoopController newCtrl = new LoopController(soundId, volume, pitch, range, delay, isLooping);
        CONTROLLERS.put(pos, newCtrl);

        boolean engineLoop = isLooping && delay == 0;
        playSound(pos, newCtrl, engineLoop);
    }

    public static void stopLoop(BlockPos pos) {
        if (CONTROLLERS.containsKey(pos)) {
            LoopController ctrl = CONTROLLERS.get(pos);
            if (ctrl.activeSound != null) {
                ctrl.activeSound.setStopped(true);
            }
            CONTROLLERS.remove(pos);
        }
    }

    public static void updateVolume(BlockPos pos, float volume) {
        if (CONTROLLERS.containsKey(pos)) {
            LoopController ctrl = CONTROLLERS.get(pos);
            ctrl.volume = volume;
            if (ctrl.activeSound != null) ctrl.activeSound.setBaseVolume(volume);
        }
    }

    public static void updatePitch(BlockPos pos, float pitch) {
        if (CONTROLLERS.containsKey(pos)) {
            LoopController ctrl = CONTROLLERS.get(pos);
            ctrl.pitch = pitch;
            if (ctrl.activeSound != null) ctrl.activeSound.setPitch(pitch);
        }
    }

    public static void updateRange(BlockPos pos, int range) {
        if (CONTROLLERS.containsKey(pos)) {
            LoopController ctrl = CONTROLLERS.get(pos);
            ctrl.range = range;
            if (ctrl.activeSound != null) ctrl.activeSound.setRange(range);
        }
    }

    public static void updateDelay(BlockPos pos, int delay) {
        if (CONTROLLERS.containsKey(pos)) {
            LoopController ctrl = CONTROLLERS.get(pos);
            boolean wasContinuous = ctrl.maxDelaySeconds == 0;
            boolean nowContinuous = delay == 0;
            ctrl.maxDelaySeconds = delay;
            if (wasContinuous != nowContinuous && ctrl.activeSound != null) {
                ctrl.activeSound.setStopped(true);
            }
        }
    }
}