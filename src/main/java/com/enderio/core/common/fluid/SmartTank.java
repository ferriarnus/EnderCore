package com.enderio.core.common.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.FluidUtil;
import com.enderio.core.common.util.NullHelper;
import com.google.common.base.Strings;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

public class SmartTank extends FluidTank {
    // Note: NBT-safe as long as the restriction isn't using NBT

    protected @Nullable Fluid restriction;

    public SmartTank(@Nonnull FluidStack fluidStack, int capacity) {
        super(capacity);
        this.fluid = fluidStack;
        setFluidRestriction(fluidStack.getFluid());
    }

    public SmartTank(int capacity) {
        super(capacity);
        setFluidRestriction(null);
    }

    public SmartTank(@Nullable Fluid restriction, int capacity) {
        super(capacity);
        setFluidRestriction(restriction);
    }

    public void setFluidRestriction(@Nullable Fluid fluidRestriction) {
        this.restriction = fluidRestriction;
        if (fluidRestriction == null)
            this.validator = e -> true;
        else this.validator = e -> (restriction == null || (e != null && e.getFluid() != null && FluidUtil.areFluidsTheSame(restriction, e.getFluid())));
    }

    public float getFilledRatio() {
        return (float) getFluidAmount() / getCapacity();
    }

    public boolean isFull() {
        return getFluidAmount() >= getCapacity();
    }

    public boolean isEmpty() {
        return getFluidAmount() == 0;
    }

    public boolean hasFluid(@Nullable Fluid candidate) {
        final FluidStack fluid2 = fluid;
        return !(fluid2 == null || candidate == null || fluid2.getAmount() <= 0 || fluid2.getFluid() != candidate);
    }

    public void setFluidAmount(int amount) {
        if (amount > 0) {
            if (fluid != FluidStack.EMPTY) {
                fluid.setAmount(Math.min(capacity, amount));
            } else if (restriction != null) {
                this.fluid = new FluidStack(restriction, Math.min(capacity, amount));
            } else {
                throw new RuntimeException("Cannot set fluid amount of an empty tank");
            }
        } else {
            this.fluid = FluidStack.EMPTY;
        }
        onContentsChanged();
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        if (fluid != FluidStack.EMPTY) {
            return fluid;
        } else if (restriction != null) {
            return new FluidStack(restriction, 0);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Nonnull
    public FluidStack getFluidNN() {
        return NullHelper.notnull(getFluid(), "Internal Logic Error. Non-Empty tank has no fluid.");
    }

    public int getAvailableSpace() {
        return getCapacity() - getFluidAmount();
    }

    public void addFluidAmount(int amount) {
        setFluidAmount(getFluidAmount() + amount);
    }

    public int removeFluidAmount(int amount) {
        int drained = 0;
        if (getFluidAmount() > amount) {
            setFluidAmount(getFluidAmount() - amount);
            drained = amount;
        } else if (!isEmpty()) {
            drained = getFluidAmount();
            setFluidAmount(0);
        } else {
            return 0;
        }
        return drained;
    }

    public void writeCommon(@Nonnull String name, @Nonnull CompoundNBT nbtRoot) {
        CompoundNBT tankRoot = new CompoundNBT();
        fluid.writeToNBT(nbtRoot);
        if (restriction != null) {
            tankRoot.putString("FluidRestriction", NullHelper.notnullF(restriction.getRegistryName().toString(), "encountered fluid with null name"));
        }
        tankRoot.putInt("Capacity", capacity);
        nbtRoot.put(name, tankRoot);
    }

    public void readCommon(@Nonnull String name, @Nonnull CompoundNBT nbtRoot) {
        if (nbtRoot.contains(name)) {
            CompoundNBT tankRoot = (CompoundNBT) nbtRoot.get(name);
            fluid = FluidStack.loadFluidStackFromNBT(nbtRoot);
            if (tankRoot.contains("FluidRestriction")) {
                String fluidName = tankRoot.getString("FluidRestriction");
                if (!Strings.isNullOrEmpty(fluidName)) {
                    setFluidRestriction(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName)));
                }
            }
            if (tankRoot.contains("Capacity")) {
                capacity = tankRoot.getInt("Capacity");
            }
        } else {
            this.fluid = FluidStack.EMPTY;
            // not reseting 'restriction' here on purpose---it would destroy the one that was set at tank creation
        }
    }

    public static SmartTank createFromNBT(@Nonnull String name, @Nonnull CompoundNBT nbtRoot) {
        SmartTank result = new SmartTank(0);
        result.readCommon(name, nbtRoot);
        if (result.getFluidAmount() > result.getCapacity()) {
            result.setCapacity(result.getFluidAmount());
        }
        return result;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        // TODO: Work this stuff out.
//        if (tile instanceof ITankAccess) {
//            ((ITankAccess) tile).setTanksDirty();
//        } else if (tile != null) {
//            tile.markDirty();
//        }
    }
}
