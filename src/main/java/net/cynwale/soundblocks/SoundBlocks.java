package net.cynwale.soundblocks;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundBlocks implements ModInitializer {
    public static final String MOD_ID = "sound-blocks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Running Sound Blocks... Done.");

        // 1. Init Custom Manager (Scan folder for external OGGs)
        CustomSoundManager.init();

        // 2. Register Core Blocks
        ModBlocks.registerModBlocks();

        // 3. Register Built-in Sounds
        ModSounds.registerSounds();

        // 4. Register Networking
        SoundBlocksNetworking.registerC2SPackets();
    }
}
