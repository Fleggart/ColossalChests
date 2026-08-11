package org.cyclops.colossalchests.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.ItemStackHandler;
import org.cyclops.cyclopscore.inventory.INBTInventory;

public class LegacySimpleInventory implements IInventory, INBTInventory {

    private ItemStackHandler handler;
    private String name;
    private int stackLimit;

    public LegacySimpleInventory() {
        this(0, "inventory", 64);
    }

    public LegacySimpleInventory(int size, String name, int stackLimit) {
        this.handler = new ItemStackHandler(size);
        this.name = name;
        this.stackLimit = stackLimit;
    }

    @Override
    public int getSizeInventory() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // 这个方法映射到 isEmpty()，不加 @Override 避免冲突
    public boolean func_191420_l() {
        return isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = handler.getStackInSlot(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.splitStack(count);
        if (stack.isEmpty()) {
            handler.setStackInSlot(index, ItemStack.EMPTY);
        } else {
            handler.setStackInSlot(index, stack);
        }
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = handler.getStackInSlot(index);
        handler.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return stackLimit;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < handler.getSlots(); i++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    // ===== INBTInventory methods =====
    @Override
    public NBTTagCompound toNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void fromNBT(NBTTagCompound nbt) {
        handler.deserializeNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("inventory", handler.serializeNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("inventory")) {
            handler.deserializeNBT(nbt.getCompoundTag("inventory"));
        }
    }

    // ===== Convenience methods =====
    public NBTTagCompound serializeNBT() {
        return handler.serializeNBT();
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        handler.deserializeNBT(nbt);
    }

    public int getSlots() {
        return handler.getSlots();
    }

    public void setStackInSlot(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    public void setItem(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    public ItemStack getItem(int index) {
        return handler.getStackInSlot(index);
    }

    public void setSize(int size) {
        ItemStackHandler newHandler = new ItemStackHandler(size);
        int copySize = Math.min(size, handler.getSlots());
        for (int i = 0; i < copySize; i++) {
            newHandler.setStackInSlot(i, handler.getStackInSlot(i));
        }
        this.handler = newHandler;
    }
}
