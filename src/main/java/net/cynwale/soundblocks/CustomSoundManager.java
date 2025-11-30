package net.cynwale.soundblocks;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CustomSoundManager {

    // Path: .minecraft/config/sound-blocks/custom_sounds/
    public static final Path CUSTOM_SOUNDS_DIR = FabricLoader.getInstance().getConfigDir().resolve("sound-blocks/custom_sounds");

    // Map sanitized IDs to real file paths to ensure correct loading
    public static final Map<String, Path> SOUND_FILE_MAP = new HashMap<>();
    // Keep the list for order/iteration if needed, or just use the map keys
    public static final List<String> SOUND_IDS = new ArrayList<>();

    public static void init() {
        try {
            // 1. Create folder if missing
            if (!Files.exists(CUSTOM_SOUNDS_DIR)) {
                Files.createDirectories(CUSTOM_SOUNDS_DIR);
            }

            // 2. Scan for .ogg files
            try (Stream<Path> paths = Files.walk(CUSTOM_SOUNDS_DIR)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".ogg"))
                        .forEach(path -> {
                            String filename = path.getFileName().toString();
                            // Remove .ogg
                            String id = filename.substring(0, filename.length() - 4);
                            // Sanitize ID (lowercase, no spaces)
                            id = id.toLowerCase().replaceAll("[^a-z0-9_]", "_");

                            // Prevent duplicates
                            if (!SOUND_FILE_MAP.containsKey(id)) {
                                registerCustomSound(id);
                                SOUND_IDS.add(id);
                                SOUND_FILE_MAP.put(id, path);
                            }
                        });
            }

        } catch (IOException e) {
            SoundBlocks.LOGGER.error("Failed to load custom sounds", e);
        }
    }

    private static void registerCustomSound(String name) {
        ResourceLocation id = new ResourceLocation(SoundBlocks.MOD_ID, name);
        Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        SoundBlocks.LOGGER.info("Registered custom sound: " + id);
    }
}
