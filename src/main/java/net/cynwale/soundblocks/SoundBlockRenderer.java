package net.cynwale.soundblocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class SoundBlockRenderer implements BlockEntityRenderer<SoundBlockEntity> {

    // OPTIMIZATION: Cache the stack so we don't create a new object every frame
    private static final ItemStack RENDER_STACK = new ItemStack(ModBlocks.SOUND_BLOCK);

    public SoundBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SoundBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // Check if player is holding the Sound Block item
        boolean holdingBlock = client.player.getMainHandItem().is(ModBlocks.SOUND_BLOCK.asItem()) ||
                client.player.getOffhandItem().is(ModBlocks.SOUND_BLOCK.asItem());

        if (holdingBlock) {
            poseStack.pushPose();

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(1.0f, 1.0f, 1.0f);

            poseStack.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            ItemRenderer itemRenderer = client.getItemRenderer();
            // Use the cached stack
            BakedModel model = itemRenderer.getModel(RENDER_STACK, entity.getLevel(), null, 0);

            itemRenderer.render(RENDER_STACK, ItemDisplayContext.FIXED, false, poseStack, bufferSource, packedLight, packedOverlay, model);

            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}