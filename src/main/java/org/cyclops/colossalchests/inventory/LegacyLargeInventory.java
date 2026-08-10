package org.cyclops.colossalchests.inventory;

/**
 * 兼容旧版 CyclopsCore LargeInventory 的替代类
 * 本质上和 LegacySimpleInventory 一样，只是名字不同
 */
public class LegacyLargeInventory extends LegacySimpleInventory {

    public LegacyLargeInventory() {
        super();
    }

    public LegacyLargeInventory(int size, String name, int stackLimit) {
        super(size, name, stackLimit);
    }
}
