package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.ItemStackHandler;
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

/**
 * Tile entity for the Uncolossal Chest - a small chest that looks like a miniature colossal chest.
 * 
 * @author rubensworks
 */
public class TileUncolossalChest extends CyclopsTileEntity implements CyclopsTileEntity.ITickingTile {

    private static final int TICK_MODULUS = 200;
    private static final int INVENTORY_SIZE = 5;
    private static final int STACK_LIMIT = 64;

    @NBTPersist
    private String customName = null;

    // 直接使用 ItemStackHandler，不再需要 LegacySimpleInventory
    private final ItemStackHandler inventory;

    public float prevLidAngle;
    public float lidAngle;
    private int playersUsing;

    private final Block block = UncolossalChest.getInstance();

    public TileUncolossalChest() {
        this.inventory = new ItemStackHandler(INVENTORY_SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }
        };
    }

    // ===================== ITickingTile =====================

    @Override
    public void updateTileEntity() {
        if (world != null && !this.world.isRemote && this.playersUsing != 0 
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

            for (EntityPlayer player : entities) {
                if (player.openContainer instanceof ContainerColossalChest) {
                    ++this.playersUsing;
                }
            }

            world.addBlockEvent(getPos(), block, 1, playersUsing);
        }

        // 箱盖动画
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

    // ===================== 物品栏方法（直接委托给 ItemStackHandler） =====================

    public int getSizeInventory() {
        return inventory.getSlots();
    }

    public boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public ItemStack getStackInSlot(int index) {
        return inventory.getStackInSlot(index);
    }

    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = inventory.getStackInSlot(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.splitStack(count);
        if (stack.isEmpty()) {
            inventory.setStackInSlot(index, ItemStack.EMPTY);
        }
        markDirty();
        return result;
    }

    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory.getStackInSlot(index);
        inventory.setStackInSlot(index, ItemStack.EMPTY);
        markDirty();
        return stack;
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.setStackInSlot(index, stack);
        markDirty();
    }

    public int getInventoryStackLimit() {
        return STACK_LIMIT;
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this
                && player.getDistanceSq((double) this.pos.getX() + 0.5D, 
                                       (double) this.pos.getY() + 0.5D, 
                                       (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            triggerPlayerUsageChange(1);
        }
    }

    public void closeInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            triggerPlayerUsageChange(-1);
        }
    }

    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {}

    public int getFieldCount() {
        return 0;
    }

    public void clear() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        markDirty();
    }

    // ===================== IWorldNameable =====================

    public String getName() {
        return hasCustomName() ? customName : L10NHelpers.localize("general.colossalchests.uncolossalchest.name");
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    // ===================== ISidedInventory =====================

    public int[] getSlotsForFace(EnumFacing side) {
        int size = getSizeInventory();
        ContiguousSet<Integer> integers = ContiguousSet.create(
                Range.closed(0, size - 1), DiscreteDomain.integers()
        );
        return ArrayUtils.toPrimitive(integers.toArray(new Integer[0]));
    }

    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    // ===================== 方块事件 =====================

    @Override
    public boolean receiveClientEvent(int i, int j) {
        if (i == 1) {
            playersUsing = j;
            return true;
        }
        return false;
    }

    // ===================== NBT =====================

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("inventory")) {
            inventory.deserializeNBT(tag.getCompoundTag("inventory"));
        }
        if (tag.hasKey("customName")) {
            customName = tag.getString("customName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setTag("inventory", inventory.serializeNBT());
        if (customName != null && !customName.isEmpty()) {
            tag.setString("customName", customName);
        }
        return tag;
    }

    // ===================== 辅助方法 =====================

    private void triggerPlayerUsageChange(int change) {
        if (world != null) {
            playersUsing += change;
            world.addBlockEvent(getPos(), block, 1, playersUsing);
        }
    }

    public EnumFacing getRotation() {
        IBlockState blockState = getWorld().getBlockState(getPos());
        if (blockState.getBlock() != UncolossalChest.getInstance()) {
            return EnumFacing.NORTH;
        }
        return BlockHelpers.getSafeBlockStateProperty(blockState, UncolossalChest.FACING, EnumFacing.NORTH);
    }

    // ===================== 标记脏数据 =====================

    @Override
    public void markDirty() {
        super.markDirty();
        // ItemStackHandler 的 onContentsChanged 已经调用了 markDirty
        // 但为了安全，这里也保留
    }
}
