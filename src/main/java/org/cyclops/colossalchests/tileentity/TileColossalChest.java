package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;

import java.util.*;

public class TileColossalChest extends CyclopsTileEntity implements IInventory, ISidedInventory, ITickable, ILootContainer {

    private static final int TICK_MODULUS = 200;

    // ========== 结构检测器（替代 CyclopsCore CubeDetector） ==========
    public static class Detector {
        public boolean detect(World world, BlockPos center, BlockPos ignore, Object validationAction, boolean flag) {
            // 简化的检测逻辑：直接返回 true，表示结构有效
            // 完整实现需要扫描周围的 ChestWall/Interface 方块
            return true;
        }
    }
    public static final Detector detector = new Detector();

    // ========== 物品栏 ==========
    private ItemStackHandler inventory;
    private ItemStackHandler lastValidInventory;

    private Vec3i size = Vec3i.NULL_VECTOR;
    private Vec3d renderOffset = new Vec3d(0, 0, 0);
    private String customName = null;
    private int materialId = 0;
    private int modVersion = 0;
    private List<Vec3i> interfaceLocations = new ArrayList<>();
    private static final int MOD_VERSION = 1;

    public float prevLidAngle;
    public float lidAngle;
    private int playersUsing;
    private boolean recreateNullInventory = true;

    private Block block;
    private Map<EnumFacing, int[]> facingSlots = new HashMap<>();

    public TileColossalChest() {
        this.inventory = new ItemStackHandler(0);
        this.block = ColossalChest.getInstance();
    }

    public static void detectStructure(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        // 空实现，结构检测由 ColossalChest 类处理
    }

    public Vec3i getSize() {
        return size;
    }

    public void setSize(Vec3i size) {
        this.size = size;
        facingSlots.clear();
        if (isStructureComplete()) {
            this.modVersion = MOD_VERSION;
            this.inventory = constructInventory();

            if (this.lastValidInventory != null) {
                int slot = 0;
                while (slot < Math.min(this.lastValidInventory.getSlots(), this.inventory.getSlots())) {
                    ItemStack contents = this.lastValidInventory.getStackInSlot(slot);
                    if (!contents.isEmpty()) {
                        this.inventory.setStackInSlot(slot, contents);
                        this.lastValidInventory.setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    slot++;
                }
                if (slot < this.lastValidInventory.getSlots()) {
                    dropItems(this.lastValidInventory);
                }
                this.lastValidInventory = null;
            }
        } else {
            interfaceLocations.clear();
            if (this.inventory != null) {
                if (GeneralConfig.ejectItemsOnDestroy) {
                    dropItems(this.inventory);
                    this.lastValidInventory = null;
                } else {
                    this.lastValidInventory = this.inventory;
                }
            }
            this.inventory = new ItemStackHandler(0);
        }
        markDirty();
        sendUpdate();
    }

    private void dropItems(ItemStackHandler inv) {
        World world = getWorld();
        if (world == null || world.isRemote) return;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.entity.item.EntityItem item = new net.minecraft.entity.item.EntityItem(
                        world, getPos().getX(), getPos().getY(), getPos().getZ(), stack);
                world.spawnEntity(item);
            }
        }
    }

    public boolean isStructureComplete() {
        return !size.equals(Vec3i.NULL_VECTOR);
    }

    public static Vec3i getMaxSize() {
        int size = ColossalChestConfig.maxSize;
        return new Vec3i(size, size, size);
    }

    public int getSizeSingular() {
        return size.getX() + 1;
    }

    public PropertyMaterial.Type getMaterial() {
        return PropertyMaterial.Type.values()[this.materialId];
    }

    public void setMaterial(PropertyMaterial.Type material) {
        this.materialId = material.ordinal();
    }

    private ItemStackHandler constructInventory() {
        int size = calculateInventorySize();
        if (GeneralConfig.creativeChests && !getWorld().isRemote) {
            ItemStackHandler inv = new ItemStackHandler(size);
            Random random = new Random();
            for (int i = 0; i < size; i++) {
                inv.setStackInSlot(i, new ItemStack(Item.REGISTRY.getRandomObject(random)));
            }
            return inv;
        }
        return new ItemStackHandler(size);
    }

    private int calculateInventorySize() {
        int size = getSizeSingular();
        if (size == 1) return 0;
        return (int) Math.ceil((Math.pow(size, 3) * 27) * getMaterial().getInventoryMultiplier() / 9) * 9;
    }

    public ItemStackHandler getInventory() {
        if (getWorld() != null && getWorld().isRemote && (inventory == null || inventory.getSlots() != calculateInventorySize())) {
            return inventory = constructInventory();
        }
        if (lastValidInventory != null) {
            return new ItemStackHandler(0);
        }
        if (inventory == null && this.recreateNullInventory) {
            inventory = constructInventory();
        }
        return inventory;
    }

    public int getInventoryHash() {
        if (inventory == null) return 0;
        int hash = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                hash = 31 * hash + stack.hashCode();
            }
        }
        return hash;
    }

    // ===================== IInventory =====================

    @Override
    public int getSizeInventory() {
        return inventory != null ? inventory.getSlots() : 0;
    }

    @Override
    public boolean isEmpty() {
        if (inventory == null) return true;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory != null && index < inventory.getSlots() ? inventory.getStackInSlot(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (inventory == null || index >= inventory.getSlots()) return ItemStack.EMPTY;
        ItemStack stack = inventory.getStackInSlot(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = stack.splitStack(count);
        if (stack.isEmpty()) {
            inventory.setStackInSlot(index, ItemStack.EMPTY);
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (inventory == null || index >= inventory.getSlots()) return ItemStack.EMPTY;
        ItemStack stack = inventory.getStackInSlot(index);
        inventory.setStackInSlot(index, ItemStack.EMPTY);
        markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (inventory == null || index >= inventory.getSlots()) return;
        inventory.setStackInSlot(index, stack);
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        super.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            playersUsing++;
            world.addBlockEvent(pos, block, 1, playersUsing);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            playersUsing--;
            world.addBlockEvent(pos, block, 1, playersUsing);
        }
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
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        if (inventory != null) {
            for (int i = 0; i < inventory.getSlots(); i++) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
        markDirty();
    }

    // ===================== IWorldNameable =====================

    @Override
    public String getName() {
        return hasCustomName() ? customName : 
            net.minecraft.util.text.translation.I18n.translateToLocalFormatted(
                "general.colossalchests.colossalchest.name", 
                getMaterial().getLocalizedName(), 
                getSizeSingular()
            );
    }

    @Override
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    // ===================== ISidedInventory =====================

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        int size = getSizeInventory();
        int[] slots = facingSlots.get(side);
        if (slots == null) {
            slots = new int[size];
            for (int i = 0; i < size; i++) slots[i] = i;
            facingSlots.put(side, slots);
        }
        return slots;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    // ===================== ITickable =====================

    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        // 动画
        prevLidAngle = lidAngle;
        float increaseAngle = 0.15F / Math.min(5, getSizeSingular());
        if (playersUsing > 0 && lidAngle == 0.0F) {
            world.playSound(
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.BLOCK_CHEST_OPEN,
                SoundCategory.BLOCKS,
                (float) (0.5F + (0.5F * Math.log(getSizeSingular()))),
                world.rand.nextFloat() * 0.1F + 0.45F + increaseAngle,
                true
            );
        }
        if (playersUsing == 0 && lidAngle > 0.0F || playersUsing > 0 && lidAngle < 1.0F) {
            float preIncreaseAngle = lidAngle;
            if (playersUsing > 0) {
                lidAngle += increaseAngle;
            } else {
                lidAngle -= increaseAngle;
            }
            if (lidAngle > 1.0F) lidAngle = 1.0F;
            if (lidAngle < 0.0F) lidAngle = 0.0F;
        }
    }

    // ===================== NBT =====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        
        if (inventory != null) {
            compound.setTag("inventory", inventory.serializeNBT());
        }
        if (lastValidInventory != null) {
            compound.setTag("lastValidInventory", lastValidInventory.serializeNBT());
        }
        
        compound.setInteger("sizeX", size.getX());
        compound.setInteger("sizeY", size.getY());
        compound.setInteger("sizeZ", size.getZ());
        compound.setDouble("renderOffsetX", renderOffset.x);
        compound.setDouble("renderOffsetY", renderOffset.y);
        compound.setDouble("renderOffsetZ", renderOffset.z);
        compound.setInteger("materialId", materialId);
        compound.setInteger("modVersion", modVersion);
        
        if (customName != null) {
            compound.setString("customName", customName);
        }
        
        NBTTagList list = new NBTTagList();
        for (Vec3i v : interfaceLocations) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("x", v.getX());
            tag.setInteger("y", v.getY());
            tag.setInteger("z", v.getZ());
            list.appendTag(tag);
        }
        compound.setTag("interfaceLocations", list);
        
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        size = new Vec3i(
            compound.getInteger("sizeX"),
            compound.getInteger("sizeY"),
            compound.getInteger("sizeZ")
        );
        
        renderOffset = new Vec3d(
            compound.getDouble("renderOffsetX"),
            compound.getDouble("renderOffsetY"),
            compound.getDouble("renderOffsetZ")
        );
        
        materialId = compound.getInteger("materialId");
        modVersion = compound.getInteger("modVersion");
        
        if (compound.hasKey("customName")) {
            customName = compound.getString("customName");
        }
        
        if (compound.hasKey("inventory")) {
            inventory = new ItemStackHandler(getSizeInventory());
            inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        }
        
        if (compound.hasKey("lastValidInventory")) {
            lastValidInventory = new ItemStackHandler(0);
            lastValidInventory.deserializeNBT(compound.getCompoundTag("lastValidInventory"));
        }
        
        interfaceLocations.clear();
        NBTTagList list = compound.getTagList("interfaceLocations", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            interfaceLocations.add(new Vec3i(
                tag.getInteger("x"),
                tag.getInteger("y"),
                tag.getInteger("z")
            ));
        }
    }

    // ===================== 网络 =====================

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    private void sendUpdate() {
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            playersUsing = type;
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    // ===================== 渲染 =====================

    public Vec3d getRenderOffset() {
        return renderOffset;
    }

    public void setCenter(Vec3d center) {
        this.renderOffset = new Vec3d(pos.getX() - center.x, pos.getY() - center.y, pos.getZ() - center.z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int size = getSizeSingular();
        return new AxisAlignedBB(
            pos.add(-size, -size, -size),
            pos.add(size + 1, size * 2 + 1, size + 1)
        );
    }

    // ===================== 接口管理 =====================

    public void addInterface(Vec3i location) {
        if (!interfaceLocations.contains(location)) {
            interfaceLocations.add(location);
        }
    }

    public List<Vec3i> getInterfaceLocations() {
        return Collections.unmodifiableList(interfaceLocations);
    }

    public boolean canInteractWith(EntityPlayer player) {
        return isUsableByPlayer(player);
    }

    // ===================== ILootContainer =====================

    @Override
    public ResourceLocation getLootTable() {
        return new ResourceLocation("dummy");
    }
}
