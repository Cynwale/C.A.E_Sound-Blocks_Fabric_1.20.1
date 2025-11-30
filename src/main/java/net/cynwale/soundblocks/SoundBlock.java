package net.cynwale.soundblocks;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext; // Add this import if needed
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SoundBlock extends BaseEntityBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public SoundBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    // --- CHANGE: Conditional Shape ---
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Check if the context has an entity (player)
        if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof LivingEntity entity) {
            // Check Main Hand OR Off Hand
            if (entity.getMainHandItem().is(ModBlocks.SOUND_BLOCK.asItem()) || 
                entity.getOffhandItem().is(ModBlocks.SOUND_BLOCK.asItem())) {
                return Shapes.block();
            }
        }
        // Also fallback: if context.isHolding works (it might be a mapping issue), try that
        // But the manual check above is 100% safe.
        
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoundBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return createTickerHelper(type, ModBlocks.SOUND_BLOCK_ENTITY, SoundBlockEntity::clientTick);
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            stopBlockSound(level, pos);
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        boolean isReceivingPower = level.hasNeighborSignal(pos);
        boolean isPoweredByGui = false;
        if (level.getBlockEntity(pos) instanceof SoundBlockEntity entity) {
            isPoweredByGui = entity.isPoweredByGui();
        }

        boolean isEffectivePower = isReceivingPower || isPoweredByGui;
        boolean isAlreadyPowered = state.getValue(POWERED);

        if (isEffectivePower && !isAlreadyPowered) {
            playBlockSound(level, pos);
            level.setBlock(pos, state.setValue(POWERED, true), 3);
        }
        else if (!isEffectivePower && isAlreadyPowered) {
            stopBlockSound(level, pos);
            level.setBlock(pos, state.setValue(POWERED, false), 3);
        }
    }

    public void updateBlockVolume(Level level, BlockPos pos, float volume) {
        if (level.isClientSide) return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeFloat(volume);
        sendToNearby(level, pos, SoundBlocksNetworking.SYNC_LOOP_VOLUME, buf);
    }

    public void updateBlockPitch(Level level, BlockPos pos, float pitch) {
        if (level.isClientSide) return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeFloat(pitch);
        sendToNearby(level, pos, SoundBlocksNetworking.SYNC_LOOP_PITCH, buf);
    }

    public void updateBlockRange(Level level, BlockPos pos, int range) {
        if (level.isClientSide) return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(range);
        sendToNearby(level, pos, SoundBlocksNetworking.SYNC_LOOP_RANGE, buf);
    }

    public void updateBlockDelay(Level level, BlockPos pos, int delay) {
        if (level.isClientSide) return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(delay);
        sendToNearby(level, pos, SoundBlocksNetworking.SYNC_LOOP_DELAY, buf);
    }

    private void sendToNearby(Level level, BlockPos pos, ResourceLocation packetId, FriendlyByteBuf buf) {
        ((ServerLevel)level).getChunkSource().chunkMap.getPlayers(new net.minecraft.world.level.ChunkPos(pos), false).forEach(player -> {
            ServerPlayNetworking.send(player, packetId, buf);
        });
    }

    public void playBlockSound(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof SoundBlockEntity soundEntity) {
            String soundId = soundEntity.getSoundId();
            float volume = soundEntity.getVolume();
            float pitch = soundEntity.getPitch();
            int range = soundEntity.getRange();
            int delay = soundEntity.getRandomDelay();
            boolean isLooping = soundEntity.isLooping();

            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(pos);
            buf.writeUtf(soundId);
            buf.writeFloat(volume);
            buf.writeFloat(pitch);
            buf.writeInt(range);
            buf.writeInt(delay);
            buf.writeBoolean(isLooping);

            sendToNearby(level, pos, SoundBlocksNetworking.SYNC_LOOP_START, buf);
        }
    }

    public void stopBlockSound(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        sendToNearby(level, pos, SoundBlocksNetworking.SYNC_LOOP_STOP, buf);
    }

    // --- CHANGE: Conditional Interaction ---
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Check if player is holding the Sound Block item
        // If not, PASS (let them click through it)
        if (!player.getItemInHand(hand).is(ModBlocks.SOUND_BLOCK.asItem())) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (world.getBlockEntity(pos) instanceof SoundBlockEntity entity) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeBlockPos(pos);
                buf.writeUtf(entity.getSoundId());
                buf.writeBoolean(entity.isLooping());
                buf.writeBoolean(entity.isPoweredByGui());
                buf.writeFloat(entity.getVolume());
                buf.writeFloat(entity.getPitch());
                buf.writeInt(entity.getRange());
                buf.writeInt(entity.getRandomDelay());

                ServerPlayNetworking.send((ServerPlayer) player, SoundBlocksNetworking.OPEN_CONFIG_SCREEN_ID, buf);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}