package net.cynwale.soundblocks;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

    // Define the Sound Events
    public static final SoundEvent ELECTRIC_FAN = registerSoundEvent("electric_fan");
    public static final SoundEvent ELECTRICAL_TRANSFORMER = registerSoundEvent("electrical_transformer");
    public static final SoundEvent SERVER_ROOM = registerSoundEvent("server_room");
    public static final SoundEvent STORMY_WINDS_FOREST = registerSoundEvent("stormy_winds_forest");
    public static final SoundEvent WIND_HOWLING_TRANSFORMER_BOX = registerSoundEvent("wind_howling_transformer_box");

    private static SoundEvent registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(SoundBlocks.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerSounds() {
        SoundBlocks.LOGGER.info("Registering Sounds for " + SoundBlocks.MOD_ID);
    }
}