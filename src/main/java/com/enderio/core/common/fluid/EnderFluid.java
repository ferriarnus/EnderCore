package com.enderio.core.common.fluid;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;

import java.lang.reflect.Field;
import java.util.Random;

// Inspired by CoFH Core implementation.

public abstract class EnderFluid {
    protected RegistryObject<EnderFlowingFluid> stillFluid;
    protected RegistryObject<EnderFlowingFluid> flowingFluid;
    protected RegistryObject<EnderFluidBlock> block;
    protected RegistryObject<Item> bucket;

    private ForgeFlowingFluid.Properties properties;

    protected EnderFluid() {

    }

    // region Attributes and Properties.

    // Viscocity
    private static final Field viscosity;

    static {
        viscosity = ObfuscationReflectionHelper.findField(FluidAttributes.Builder.class, "viscosity");
    }

    protected void setAttributes(FluidAttributes.Builder attributes) {
        // Create properties
        properties = new EnderFlowingFluid.Properties(stillFluid, flowingFluid, attributes).block(block);

        // ADd bucket if it isnt null
        if (bucket != null) {
            properties.bucket(bucket);
        }

        // Try to set tick rate proportionally to viscosity
        try {
            properties.tickRate(((int) viscosity.get(attributes)) / 200);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected ForgeFlowingFluid.Properties getProperties() {
        return properties;
    }

    // endregion

    // region Builder
    // The builder will be used for creating basic or dumb fluids, without custom behaviours

    public static class Builder {
        private String id;
        private FluidAttributes.Builder attributes;

        private boolean bucket;

        private DeferredRegister<Fluid> fluidRegistry;
        private DeferredRegister<Block> blockRegistry;
        private DeferredRegister<Item> itemRegistry;

        public Builder(String id) {
            this.id = id;
        }

        public Builder bucket() {
            bucket = true;
            return this;
        }

        public Builder attributes(FluidAttributes.Builder attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder fluidRegistry(DeferredRegister<Fluid> reg) {
            fluidRegistry = reg;
            return this;
        }

        public Builder blockRegistry(DeferredRegister<Block> reg) {
            blockRegistry = reg;
            return this;
        }

        public Builder itemRegistry(DeferredRegister<Item> reg) {
            itemRegistry = reg;
            return this;
        }

        public EnderFluid build() {
            if (attributes == null)
                throw new IllegalStateException("Attributes must be set!");
            if (fluidRegistry == null)
                throw new IllegalStateException("Fluid registry must be set!");
            if (blockRegistry == null)
                throw new IllegalStateException("Block registry must be set!");
            if (itemRegistry == null && bucket)
                throw new IllegalStateException("Item registry must be set if a bucket is to be added!");

            EnderFluid fluid = new EnderFluid() {};

            // Create fluids and block
            fluid.stillFluid = fluidRegistry.register(id, () -> new EnderFlowingFluid.Source(fluid, fluid.getProperties()));
            fluid.flowingFluid = fluidRegistry.register(flowing(id), () -> new EnderFlowingFluid.Flowing(fluid, fluid.getProperties()));
            fluid.block = blockRegistry.register(block(id), () -> new EnderFluidBlock(fluid.stillFluid, AbstractBlock.Properties.create(Material.WATER), 0xFF705e41));

            // Setup bucket.
            if (bucket) {
                fluid.bucket = itemRegistry.register(EnderFluid.bucket(id), () -> new BucketItem(fluid.stillFluid, new Item.Properties()));
            }

            // Configure properties.
            fluid.setAttributes(attributes);

            return fluid;
        }
    }

    // endregion

    // region Fluid Behaviours

    public void onEntityCollision(EnderFlowingFluid fluid, BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        // Can be overidden for custom implementations
    }

    public void preTick(EnderFlowingFluid fluid, World worldIn, BlockPos pos, FluidState state) {
        // Can also be overidden.
    }

    public void postTick(EnderFlowingFluid fluid, World worldIn, BlockPos pos, FluidState state) {
        // Can also be overidden.
    }

    public boolean ticksRandomly(EnderFlowingFluid fluid) {
        return false;
    }

    public void randomTick(EnderFlowingFluid fluid, World worldIn, BlockPos pos, FluidState state, Random rand) {
        // Overriddable
    }

    // endregion

    // region Identifier helpers

    protected static String flowing(String id) {
        return id + "_flowing";
    }

    protected static String block(String id) {
        return id + "_block";
    }

    protected static String bucket(String id) {
        return id + "_bucket";
    }

    // endregion

    // region Render layer

    @OnlyIn(Dist.CLIENT)
    public void setRenderLayer(RenderType layer) {
        RenderTypeLookup.setRenderLayer(block.get(), layer);
        RenderTypeLookup.setRenderLayer(stillFluid.get(), layer);
        RenderTypeLookup.setRenderLayer(flowingFluid.get(), layer);
    }

    // endregion
}
