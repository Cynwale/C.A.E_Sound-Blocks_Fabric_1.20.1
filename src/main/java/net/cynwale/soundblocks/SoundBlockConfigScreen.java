package net.cynwale.soundblocks;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SoundBlockConfigScreen {

    // OPTIMIZATION: Cache the list of sounds so we don't scan the registry every time the menu opens.
    private static List<String> CACHED_SOUNDS = null;

    public static Screen create(SoundBlockEntity entity) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(Minecraft.getInstance().screen)
                .setTitle(Component.literal("Sound Block Configuration"));

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // --- Collect Sounds (With Caching) ---
        if (CACHED_SOUNDS == null) {
            CACHED_SOUNDS = new ArrayList<>();
            for (ResourceLocation id : BuiltInRegistries.SOUND_EVENT.keySet()) {
                if (id.getNamespace().equals(SoundBlocks.MOD_ID)) {
                    CACHED_SOUNDS.add(id.toString());
                }
            }
            CACHED_SOUNDS.sort(String::compareTo);

            if (CACHED_SOUNDS.isEmpty()) {
                CACHED_SOUNDS.add("sound-blocks:electric_fan");
            }
        }

        // Use Cached List
        List<String> shortNames = CACHED_SOUNDS.stream().map(SoundBlockConfigScreen::formatSoundId).toList();
        String currentShortName = formatSoundId(entity.getSoundId());

        if (!shortNames.contains(currentShortName)) {
            currentShortName = shortNames.get(0);
        }

        // --- 1. Random Delay (Linked to Looping) ---
        AbstractConfigListEntry<?> delayEntry = entryBuilder.startIntSlider(
                        Component.literal("Random Delay"),
                        entity.getRandomDelay(),
                        0, 60
                )
                .setDefaultValue(0)
                .setTextGetter(val -> Component.literal(val == 0 ? "None (Continuous)" : "Max " + val + "s"))
                .setTooltip(Component.literal("Adds a random pause between loops.\nOnly works if Looping Mode is ON."))
                .setSaveConsumer(newValue -> {
                    entity.setRandomDelay(newValue);
                    saveData(entity);
                })
                .build();

        // --- 2. Looping Mode ---
        BooleanToggleBuilder loopingBuilder = entryBuilder.startBooleanToggle(Component.literal("Looping Mode"), entity.isLooping())
                .setDefaultValue(false)
                .setTooltip(Component.literal("If true, sound loops while powered."))
                .setSaveConsumer(newValue -> {
                    entity.setLooping(newValue);
                    saveData(entity);
                });

        SoundBlockClothLibrary.link(loopingBuilder, delayEntry);
        general.addEntry(loopingBuilder.build());

        // --- 3. Play Sound (Manual) ---
        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Play Sound (Manual)"), entity.isPoweredByGui())
                .setDefaultValue(false)
                .setTooltip(Component.literal("Turn ON to power the block and play sound immediately."))
                .setSaveConsumer(newValue -> {
                    entity.setPoweredByGui(newValue);
                    saveData(entity);
                })
                .build());

        // 4. Volume Slider
        general.addEntry(entryBuilder.startIntSlider(
                        Component.literal("Volume"),
                        (int) (entity.getVolume() * 100),
                        0, 200
                )
                .setDefaultValue(100)
                .setTextGetter(val -> Component.literal(val + "%"))
                .setSaveConsumer(newValue -> {
                    entity.setVolume(newValue / 100.0f);
                    saveData(entity);
                })
                .build());

        // 5. Pitch Slider
        general.addEntry(entryBuilder.startIntSlider(
                Component.literal("Pitch"),
                (int) (entity.getPitch() * 100), 
                10, 200 // CHANGE: Min 10 (0.1f), Max 200 (2.0f). 0 is forbidden.
                )
                .setDefaultValue(100)
                .setTextGetter(val -> Component.literal(val + "%"))
                .setSaveConsumer(newValue -> {
                    // Extra safety clamp
                    int safeValue = Math.max(10, newValue);
                    entity.setPitch(safeValue / 100.0f);
                    saveData(entity);
                })
                .build());

        // 6. Range Slider
        general.addEntry(entryBuilder.startIntSlider(
                        Component.literal("Range"),
                        entity.getRange(),
                        1, 64
                )
                .setDefaultValue(16)
                .setTextGetter(val -> Component.literal(val + " Blocks"))
                .setSaveConsumer(newValue -> {
                    entity.setRange(newValue);
                    saveData(entity);
                })
                .build());

        // 7. Add the Linked Delay Entry
        general.addEntry(delayEntry);

        // 8. Sound Selection
        general.addEntry(entryBuilder.startStringDropdownMenu(
                        Component.literal("Sound Selection"),
                        currentShortName
                )
                .setSelections(shortNames)
                .setDefaultValue(shortNames.get(0))
                .setTooltip(Component.literal("Select a custom sound from the list"))
                .setSaveConsumer(newValue -> {
                    String fullId = "sound-blocks:" + newValue;
                    entity.setSoundId(fullId);
                    saveData(entity);
                })
                .setSuggestionMode(true) // Change this to TRUE to make it searchable
                .build());

        return builder.build();
    }

    private static String formatSoundId(String fullId) {
        if (fullId.startsWith("sound-blocks:")) {
            return fullId.replace("sound-blocks:", "");
        }
        return fullId;
    }

    private static void saveData(SoundBlockEntity entity) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(entity.getBlockPos());
        buf.writeUtf(entity.getSoundId());
        buf.writeBoolean(entity.isLooping());
        buf.writeBoolean(entity.isPoweredByGui());
        buf.writeFloat(entity.getVolume());
        buf.writeFloat(entity.getPitch());
        buf.writeInt(entity.getRange());
        buf.writeInt(entity.getRandomDelay());

        ClientPlayNetworking.send(SoundBlocksNetworking.SOUND_BLOCK_SYNC_ID, buf);
    }
}