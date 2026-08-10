package org.cyclops.cyclopscore.inventory;

/**
 * 兼容旧版 CyclopsCore IndexedInventory 的替代类
 */
public class LegacyIndexedInventory extends LegacySimpleInventory {

    public LegacyIndexedInventory() {
        super();
    }

    public LegacyIndexedInventory(int size, String name, int stackLimit) {
        super(size, name, stackLimit);
    }

    // IndexedInventory 特有的方法
    public int getInventoryHash() {
        // 计算 inventory 的 hash
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
