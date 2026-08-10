package org.cyclops.colossalchests.inventory;

import net.minecraft.item.ItemStack;

public class LegacyIndexedInventory extends LegacySimpleInventory {

    public LegacyIndexedInventory() {
        super();
    }

    public LegacyIndexedInventory(int size, String name, int stackLimit) {
        super(size, name, stackLimit);
    }

    public int getInventoryHash() {
        int hash = 0;
        for (int i = 0; i < getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                hash = 31 * hash + stack.hashCode();
            }
        }
        return hash;
    }
}
