package net.cynwale.soundblocks;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlocks {

    // Updated Properties: No Collision, No Occlusion (transparent), Instabreak
    public static final Block SOUND_BLOCK = registerBlock("sound_block",
            new SoundBlock(FabricBlockSettings.copyOf(Blocks.BARRIER) // Barrier is a good template
                    .noCollission() // Walk through it
                    .noOcclusion() // Don't hide faces of other blocks
                    .instabreak() // Easy to remove in creative
            ));

    public static final BlockEntityType<SoundBlockEntity> SOUND_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(SoundBlocks.MOD_ID, "sound_block_entity"),
            FabricBlockEntityTypeBuilder.create(SoundBlockEntity::new, SOUND_BLOCK).build()
    );

    // --- Helper Methods ---

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(SoundBlocks.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(SoundBlocks.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        SoundBlocks.LOGGER.info("Registering ModBlocks for " + SoundBlocks.MOD_ID);

        // Add to Redstone Creative Tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(content -> {
            content.accept(SOUND_BLOCK);
        });
    }
}