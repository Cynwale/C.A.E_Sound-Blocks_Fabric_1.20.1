package net.cynwale.soundblocks;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SoundBlockClothLibrary {

    /**
     * Links a Boolean Toggle to another Entry.
     * When the Toggle is FALSE, the Dependent Entry becomes grayed out (uneditable).
     * When the Toggle is TRUE, the Dependent Entry becomes active.
     */
    public static void link(BooleanToggleBuilder toggleBuilder, AbstractConfigListEntry<?> dependentEntry) {
        // We hook into the Text Supplier because it runs every time the GUI updates.
        // This allows us to check the boolean state in real-time.
        toggleBuilder.setYesNoTextSupplier((bool) -> {
            // 1. Update the dependent entry's status
            if (dependentEntry != null) {
                dependentEntry.setEditable(bool);
            }

            // 2. Return the standard Yes/No text (mimicking default Cloth Config behavior)
            if (bool) {
                return Component.translatable("text.cloth-config.boolean.value.true").withStyle(ChatFormatting.GREEN);
            } else {
                return Component.translatable("text.cloth-config.boolean.value.false").withStyle(ChatFormatting.RED);
            }
        });
    }
}