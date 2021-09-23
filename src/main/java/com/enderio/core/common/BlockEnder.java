package com.enderio.core.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockEnder<T extends BlockEntityBase> extends Block implements EntityBlock {

  protected final @Nullable Class<? extends T> teClass;

  protected BlockEnder(@Nullable Class<? extends T> teClass) {
    this(teClass, BlockBehaviour.Properties.of(Material.METAL));
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, @Nonnull Material mat) {
    this(teClass, BlockBehaviour.Properties.of(mat, mat.getColor()));
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, @Nonnull Material mat, MaterialColor matColor) {
    this(teClass, BlockBehaviour.Properties.of(mat, matColor));
  }

  protected BlockEnder(@Nullable Class<? extends T> teClass, BlockBehaviour.Properties properties) {
    super(properties.strength(0.5f).sound(SoundType.METAL)/*.harvestLevel(0).harvestTool(ToolType.PICKAXE)*/);
    this.teClass = teClass;
  }

  @Override
  public PushReaction getPistonPushReaction(BlockState state) {
    // Some mods coremod vanilla to ignore this condition, so let's try to enforce it.
    // If this doesn't work, we need code to blow up the block when it detects it was moved...
    return teClass != null ? PushReaction.BLOCK : super.getPistonPushReaction(state);
  }

  // TODO THIS NEEDS TO BE FIXED ASAP!
  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
    return null;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    if (teClass != null) {
      try {
        T te = teClass.newInstance();
        te.init();
        return te;
      } catch (Exception e) {
        throw new RuntimeException("Could not create tile entity for block " + getDescriptionId() + " for class " + teClass, e);
      }
    }
    throw new RuntimeException(
            "Cannot create a TileEntity for a block that doesn't have a TileEntity. This is not a problem with EnderCore, this is caused by the caller.");
  }

  /* Subclass Helpers */

  @Override
  public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
    if (player.isShiftKeyDown()) {
      return InteractionResult.FAIL;
    }

    BlockEntity te = getTileEntity(worldIn, pos);
    if (te instanceof ITankAccess) {
      if (FluidUtil.fillInternalTankFromPlayerHandItem(worldIn, pos, player, handIn, (ITankAccess) te)) {
        return InteractionResult.PASS;
      }
      if (FluidUtil.fillPlayerHandItemFromInternalTank(worldIn, pos, player, handIn, (ITankAccess) te)) {
        return InteractionResult.PASS;
      }
    }

    return openGui(worldIn, pos, player, hit.getDirection());
  }


  protected InteractionResult openGui(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player entityPlayer, @Nonnull Direction side) {
    return InteractionResult.FAIL;
  }

  @Override
  public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
    if (willHarvest) {
      return true;
    }
    return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
  }

  @Override
  public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
    super.playerDestroy(worldIn, player, pos, state, te, stack);
    worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
  }

  @Override
  public List<ItemStack> getDrops(BlockState blockState, LootContext.Builder lootContext) {
    List<ItemStack> drops = super.getDrops(blockState, lootContext);
    BlockEntity tileentity = lootContext.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (tileentity != null && teClass == tileentity.getClass()) {
      ItemStack drop = new ItemStack(this);
      CompoundTag tileNBT = tileentity.save(new CompoundTag());
      drop.addTagElement("BlockEntityTag", tileNBT);
      CompoundTag compoundnbt1 = new CompoundTag();
      ListTag listnbt = new ListTag();
      listnbt.add(StringTag.valueOf("\"(+NBT)\""));
      compoundnbt1.put("Lore", listnbt);
      drop.addTagElement("display", compoundnbt1);
      drops.add(drop);
    }

    return drops;
  }

  @Override
  public final void setPlacedBy(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity placer,
      @Nonnull ItemStack stack) {
    onBlockPlaced(worldIn, pos, state, placer, stack);
    T te = getTileEntity(worldIn, pos);
    if (te != null) {
      te.readCustomNBT(stack);
      onBlockPlaced(worldIn, pos, state, placer, te);
    }
  }

  public void onBlockPlaced(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity placer,
      @Nonnull ItemStack stack) {
  }

  public void onBlockPlaced(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull LivingEntity placer, @Nonnull T te) {
  }

  /**
   * Tries to load this block's TileEntity if it exists. Will create the TileEntity if it doesn't yet exist.
   * <p>
   * <strong>This will crash if used in any other thread than the main (client or server) thread!</strong>
   *
   */
  protected @Nullable T getTileEntity(@Nonnull BlockGetter world, @Nonnull BlockPos pos) {
    final Class<? extends T> teClass2 = teClass;
    if (teClass2 != null) {
      BlockEntity te = world.getBlockEntity(pos);
      if (teClass2.isInstance(te)) {
        return teClass2.cast(te);
      }
    }
    return null;
  }

  /**
   * Tries to load this block's TileEntity if it exists. Will not create the TileEntity when used in a render thread with the correct IBlockAccess.
   *
   */
  protected @Nullable T getTileEntitySafe(@Nonnull BlockGetter world, @Nonnull BlockPos pos) {
    if (world instanceof RenderChunkRegion) {
      final Class<? extends T> teClass2 = teClass;
      if (teClass2 != null) {
        BlockEntity te = ((RenderChunkRegion) world).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        if (teClass2.isInstance(te)) {
          return teClass2.cast(te);
        }
      }
      return null;
    } else {
      return getTileEntity(world, pos);
    }
  }

  /**
   * Tries to load any block's TileEntity if it exists. Will not create the TileEntity when used in a render thread with the correct IBlockAccess. Will not
   * cause chunk loads.
   *
   */
  public static @Nullable BlockEntity getAnyTileEntitySafe(@Nonnull BlockGetter world, @Nonnull BlockPos pos) {
    return getAnyTileEntitySafe(world, pos, BlockEntity.class);
  }

  /**
   * Tries to load any block's TileEntity if it exists. Will not create the TileEntity when used in a render thread with the correct IBlockAccess. Will not
   * cause chunk loads. Also works with interfaces as the class parameter.
   *
   */
  @SuppressWarnings("unchecked")
  public static @Nullable <Q> Q getAnyTileEntitySafe(@Nonnull BlockGetter world, @Nonnull BlockPos pos, Class<Q> teClass) {
    BlockEntity te = null;
    if (world instanceof RenderChunkRegion) {
      te = ((RenderChunkRegion) world).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    } else if (world instanceof Level) {
      if (((Level) world).hasChunkAt(pos)) {
        te = world.getBlockEntity(pos);
      }
    } else {
      te = world.getBlockEntity(pos);
    }
    if (teClass == null) {
      return (Q) te;
    }
    if (teClass.isInstance(te)) {
      return teClass.cast(te);
    }
    return null;
  }

  /**
   * Tries to load any block's TileEntity if it exists. Not suitable for tasks outside the main thread. Also works with interfaces as the class parameter.
   *
   */
  @SuppressWarnings("unchecked")
  public static @Nullable <Q> Q getAnyTileEntity(@Nonnull BlockGetter world, @Nonnull BlockPos pos, Class<Q> teClass) {
    BlockEntity te = world.getBlockEntity(pos);
    if (teClass == null) {
      return (Q) te;
    }
    if (teClass.isInstance(te)) {
      return teClass.cast(te);
    }
    return null;
  }

  protected boolean shouldDoWorkThisTick(@Nonnull Level world, @Nonnull BlockPos pos, int interval) {
    T te = getTileEntity(world, pos);
    if (te == null) {
      return world.getGameTime() % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval);
    }
  }

  protected boolean shouldDoWorkThisTick(@Nonnull Level world, @Nonnull BlockPos pos, int interval, int offset) {
    T te = getTileEntity(world, pos);
    if (te == null) {
      return (world.getGameTime() + offset) % interval == 0;
    } else {
      return te.shouldDoWorkThisTick(interval, offset);
    }
  }

  public Class<? extends T> getTeClass() {
    return teClass;
  }

//  // wrapper because vanilla null-annotations are wrong
//  @SuppressWarnings("null")
//  @Override
//  public @Nonnull Block setCreativeTab(@Nullable CreativeTabs tab) {
//    return super.setCreativeTab(tab);
//  }

//  public void setShape(IShape<T> shape) {
//    this.shape = shape;
//  }


  // TODO: This shit defo needs redoing, so for now I'm commenting it out

//  @Override
//  public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//    if (shape != null) {
//      T te = getTileEntitySafe(worldIn, pos);
//      if (te != null) {
//        return shape.getBlockFaceShape(worldIn, state, pos, face, te);
//      } else {
//        return shape.getBlockFaceShape(worldIn, state, pos, face);
//      }
//    }
//    return super.getShape(state, worldIn, pos, context);
//  }
//
//  @Override
//  public final @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos,
//      @Nonnull EnumFacing face) {
//    if (shape != null) {
//      T te = getTileEntitySafe(worldIn, pos);
//      if (te != null) {
//        return shape.getBlockFaceShape(worldIn, state, pos, face, te);
//      } else {
//        return shape.getBlockFaceShape(worldIn, state, pos, face);
//      }
//    }
//    return super.getBlockFaceShape(worldIn, state, pos, face);
//  }
//
//  private IShape<T> shape = null;
//
//  public static interface IShape<T> {
//    @Nonnull
//    BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face);
//
//    default @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos,
//        @Nonnull EnumFacing face, @Nonnull T te) {
//      return getBlockFaceShape(worldIn, state, pos, face);
//    }
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape allFaces) {
//    return new IShape<T>() {
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return allFaces;
//      }
//    };
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape upDown, @Nonnull BlockFaceShape allSides) {
//    return new IShape<T>() {
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return face == EnumFacing.UP || face == EnumFacing.DOWN ? upDown : allSides;
//      }
//    };
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape down, @Nonnull BlockFaceShape up, @Nonnull BlockFaceShape allSides) {
//    return new IShape<T>() {
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return face == EnumFacing.UP ? up : face == EnumFacing.DOWN ? down : allSides;
//      }
//    };
//  }
//
//  protected @Nonnull IShape<T> mkShape(@Nonnull BlockFaceShape... faces) {
//    return new IShape<T>() {
//      @SuppressWarnings("null")
//      @Override
//      @Nonnull
//      public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
//        return faces[face.ordinal()];
//      }
//    };
//  }

}
