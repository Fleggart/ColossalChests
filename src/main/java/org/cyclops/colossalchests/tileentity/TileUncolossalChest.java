package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.colossalchests.block.UncolossalChest;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;
import org.cyclops.cyclopscore.tileentity.InventoryTileEntity;

import java.util.List;

public class TileUncolossalChest extends InventoryTileEntity implements CyclopsTileEntity.ITickingTile {

    private static final int TICK_MODULUS = 200;

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private String customName = null;

    public float prevLidAngle;
    public float lidAngle;
    private int playersUsing;

    private Block block = UncolossalChest.getInstance();

    public TileUncolossalChest() {
        super(5, "uncolossalChest", 64);
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();

        if(world != null
                && !this.world.isRemote
                && this.playersUsing != 0
                && WorldHelpers.efficientTick(world, TICK_MODULUS, getPos().hashCode())) {
            this.playersUsing = 0;
            float range = 5.0F;
            @SuppressWarnings("unchecked")
            List<EntityPlayer> entities = this.world.getEntitiesWithinAABB(
                    EntityPlayer.class,
                    new AxisAlignedBB(
                            getPos().add(new Vec3i(-range, -range, -range)),
                            getPos().add(new Vec3i(1 + range, 1 + range, 1 + range))
                    )
            );

            for(EntityPlayer player : entities) {
                if (player.openContainer instanceof ContainerColossalChest) {
                    ++this.playersUsing;
                }
            }

            world.addBlockEvent(getPos(), block, 1, playersUsing);
        }

        prevLidAngle = lidAngle;
        float increaseAngle = 0.25F;
        if (playersUsing > 0 && lidAngle == 0.0F) {
            world.playSound(
                    (double) getPos().getX() + 0.5D,
                    (double) getPos().getY() + 0.5D,
                    (double) getPos().getZ() + 0.5D,
                    SoundEvents.BLOCK_CHEST_OPEN,
                    SoundCategory.BLOCKS,
                    0.5F,
                    world.rand.nextFloat() * 0.2F + 1.15F,
                    false
            );
        }
        if (playersUsing == 0 && lidAngle > 0.0F || playersUsing > 0 && lidAngle < 1.0F) {
            float preIncreaseAngle = lidAngle;
            if (playersUsing > 0) {
                lidAngle += increaseAngle;
            } else {
                lidAngle -= increaseAngle;
            }
            if (lidAngle > 1.0F) {
                lidAngle = 1.0F;
            }
            float closedAngle = 0.5F;
            if (lidAngle < closedAngle && preIncreaseAngle >= closedAngle) {
                world.playSound(
                        (double) getPos().getX() + 0.5D,
                        (double) getPos().getY() + 0.5D,
                        (double) getPos().getZ() + 0.5D,
                        SoundEvents.BLOCK_CHEST_CLOSE,
                        SoundCategory.BLOCKS,
                        0.5F,
                        world.rand.nextFloat() * 0.2F + 1.15F,
                        false
                );
            }
            if (lidAngle < 0.0F) {
                lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean receiveClientEvent(int i, int j) {
        if (i == 1) {
            playersUsing = j;
        }
        return true;
    }

    // ===================== IInventory 接口全部实现 =====================
    // 这里直接调用 super. 是安全的，因为父类 InventoryTileEntity 提供了具体代码
    @Override
    public int getSizeInventory() {
        return super.getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return super.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return super.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return super.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        super.setInventorySlotContents(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return super.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        super.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return super.isUsableByPlayer(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            super.openInventory(player);
            triggerPlayerUsageChange(1);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            super.closeInventory(player);
            triggerPlayerUsageChange(-1);
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return super.isItemValidForSlot(index, stack);
    }

    @Override
    public int getField(int id) {
        return super.getField(id);
    }

    @Override
    public void setField(int id, int value) {
        super.setField(id, value);
    }

    @Override
    public int getFieldCount() {
        return super.getFieldCount();
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public boolean hasCustomName() {
        return super.hasCustomName();
    }

    @Override
    public ITextComponent getDisplayName() {
        return super.getDisplayName();
    }
    // ===================== IInventory 接口结束 =====================

    private void triggerPlayerUsageChange(int change) {
        if (world != null) {
            playersUsing += change;
            world.addBlockEvent(getPos(), block, 1, playersUsing);
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        ContiguousSet<Integer> integers = ContiguousSet.create(
                Range.closed(0, getSizeInventory()), DiscreteDomain.integers()
        );
        return ArrayUtils.toPrimitive(integers.toArray(new Integer[integers.size()]));
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    @Override
    public EnumFacing getRotation() {
        IBlockState blockState = getWorld().getBlockState(getPos());
        if(blockState.getBlock() != UncolossalChest.getInstance()) return EnumFacing.NORTH;
        return BlockHelpers.getSafeBlockStateProperty(blockState, UncolossalChest.FACING, EnumFacing.NORTH);
    }
}
