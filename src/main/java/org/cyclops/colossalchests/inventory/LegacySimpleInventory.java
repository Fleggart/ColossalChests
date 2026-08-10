package org.cyclops.colossalchests.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * 兼容旧版 CyclopsCore SimpleInventory 的替代类
 * 内部使用 ItemStackHandler 存储
 */
public class LegacySimpleInventory implements IInventory {

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

    // ===== 旧版 CyclopsCore 兼容方法 =====
    public void setStackInSlot(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    public int getContainerSize() {
        return handler.getSlots();
    }

    public int getSlots() {
        return handler.getSlots();
    }

    public void setItem(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    public ItemStack getItem(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    public int getInventoryStackLimit() {
        return stackLimit;
    }

    @Override
    public void markDirty() {
        // 不需要操作
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        // 不需要操作
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        // 不需要操作
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        // 不需要操作
    }

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

    // ===== NBT 序列化 =====
    public NBTTagCompound serializeNBT() {
        return handler.serializeNBT();
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        handler.deserializeNBT(nbt);
    }

    // ===== 旧版兼容方法 =====
    public int getSize() {
        return handler.getSlots();
    }

    public void setSize(int size) {
        // 重新创建 handler
        ItemStackHandler newHandler = new ItemStackHandler(size);
        int copySize = Math.min(size, handler.getSlots());
        for (int i = 0; i < copySize; i++) {
            newHandler.setStackInSlot(i, handler.getStackInSlot(i));
        }
        this.handler = newHandler;
    }
}
