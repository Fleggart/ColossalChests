package org.cyclops.colossalchests.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.Slot;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

import java.util.List;
import java.util.Map;

// 移除 @ChestContainer 注解
public class ContainerUncolossalChest extends ContainerHopper {
    public ContainerUncolossalChest(InventoryPlayer playerInventory, TileUncolossalChest tile) {
        super(playerInventory, tile, playerInventory.player);
    }

}
