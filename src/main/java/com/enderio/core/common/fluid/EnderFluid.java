package com.enderio.core.common.fluid;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.function.Function;

// Inspired by CoFH Core implementation.

public final class EnderFluid {
    private EnderFlowingFluid stillFluid;
    private EnderFlowingFluid flowingFluid;
    private EnderFluidBlock block;
    private Item bucket;

    private ForgeFlowingFluid.Properties properties;

    public EnderFluid(String id, FluidAttributes.Builder attributes, Function<EnderFluid, EnderFluidBlock> blockFunction, boolean bucket) {
        // Configure properties.
        setAttributes(attributes);

        // Create fluids and block
        this.stillFluid = new EnderFlowingFluid.Source(properties);
        this.flowingFluid = new EnderFlowingFluid.Flowing(properties);
        this.block = blockFunction.apply(this);

        // Setup bucket.
        if (bucket) {
            this.bucket = new BucketItem(() -> stillFluid, new Item.Properties());
        }

        // Set registry names
        setNames(id);
    }

    // region Getters

    public EnderFlowingFluid getStill() {
        return stillFluid;
    }

    public EnderFlowingFluid getFlowing() {
        return flowingFluid;
    }

    public EnderFluidBlock getBlock() {
        return block;
    }

    public Item getBucket() {
        return bucket;
    }

    // endregion

    // region Attributes and Properties.

    private static final Field viscosity;

    static {
        viscosity = ObfuscationReflectionHelper.findField(FluidAttributes.Builder.class, "viscosity");
    }

    private void setAttributes(FluidAttributes.Builder attributes) {
        // Create properties
        properties = new EnderFlowingFluid.Properties(() -> stillFluid, () -> flowingFluid, attributes).block(() -> block);

        // Add bucket if it isnt null
        if (bucket != null) {
            properties.bucket(() -> bucket);
        }

        // Try to set tick rate proportionally to viscosity
        try {
            properties.tickRate(Math.round((int) viscosity.get(attributes) / 200f)); // TODO: Can we just build the attributes and grab it here?
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
        RenderTypeLookup.setRenderLayer(block, layer);
        RenderTypeLookup.setRenderLayer(stillFluid, layer);
        RenderTypeLookup.setRenderLayer(flowingFluid, layer);
    }

    // endregion

    // region Registration

    protected void setNames(String id) {
        stillFluid.setRegistryName(id);
        flowingFluid.setRegistryName(flowing(id));
        block.setRegistryName(block(id));

        if (bucket != null)
            bucket.setRegistryName(bucket(id));
    }

    public void registerFluids(@Nonnull RegistryEvent.Register<Fluid> event) {
        event.getRegistry().register(stillFluid);
        event.getRegistry().register(flowingFluid);
    }

    public void registerBlock(@Nonnull RegistryEvent.Register<Block> event) {
        event.getRegistry().register(block);
    }

    public void registerBucket(@Nonnull RegistryEvent.Register<Item> event) {
        if (bucket != null)
            event.getRegistry().register(bucket);
    }

    // endregion
}
