package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.experimental.Delegate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.colossalchests.Capabilities;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.block.ChestWall;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.block.Interface;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.colossalchests.inventory.LegacyIndexedInventory;
import org.cyclops.colossalchests.inventory.LegacyLargeInventory;
import org.cyclops.colossalchests.inventory.LegacySimpleInventory;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.block.multi.AllowedBlock;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.block.multi.CubeSizeValidator;
import org.cyclops.cyclopscore.block.multi.ExactBlockCountValidator;
import org.cyclops.cyclopscore.block.multi.HollowCubeDetector;
import org.cyclops.cyclopscore.block.multi.MaximumSizeValidator;
import org.cyclops.cyclopscore.block.multi.MinimumSizeValidator;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.DirectionHelpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTPersist;
import org.cyclops.cyclopscore.tileentity.CyclopsTileEntity;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TileColossalChest extends CyclopsTileEntity implements IInventory, CyclopsTileEntity.ITickingTile, ILootContainer {

    private static final int TICK_MODULUS = 200;

    @SuppressWarnings("unchecked")
    public static CubeDetector detector = new HollowCubeDetector(
            new AllowedBlock[]{
                    new AllowedBlock(ChestWall.getInstance()),
                    new AllowedBlock(ColossalChest.getInstance()).addCountValidator(new ExactBlockCountValidator(1)),
                    new AllowedBlock(Interface.getInstance())
            },
            Lists.newArrayList(ColossalChest.getInstance(), ChestWall.getInstance(), Interface.getInstance())
    )
            .addSizeValidator(new MinimumSizeValidator(new Vec3i(1, 1, 1)))
            .addSizeValidator(new CubeSizeValidator())
            .addSizeValidator(new MaximumSizeValidator(getMaxSize()) {
                @Override
                public Vec3i getMaximumSize() {
                    return getMaxSize();
                }
            });

    @Delegate
    private final ITickingTile tickingTileComponent = new TickingTileComponent(this);

    @NBTPersist
    private LegacySimpleInventory lastValidInventory = null;
    private LegacySimpleInventory inventory = null;

    @NBTPersist
    private Vec3i size = LocationHelpers.copyLocation(Vec3i.NULL_VECTOR);
    @NBTPersist
    private Vec3d renderOffset = new Vec3d(0, 0, 0);
    @NBTPersist
    private String customName = null;
    @NBTPersist
    private int materialId = 0;
    @NBTPersist
    private int _modVersion = 0;
    @NBTPersist(useDefaultValue = false)
    private List<Vec3i> interfaceLocations = Lists.newArrayList();
    private static final int _MOD_VERSION = 1;

    public float prevLidAngle;
    public float lidAngle;
    private int playersUsing;
    private boolean recreateNullInventory = true;

    private Block block = ColossalChest.getInstance();
    private EnumFacingMap<int[]> facingSlots = EnumFacingMap.newMap();

    public TileColossalChest() {
        if (Capabilities.SLOTLESS_ITEMHANDLER != null) {
            addSlotlessItemHandlerCapability();
        }
    }

    protected void addSlotlessItemHandlerCapability() {
        // Temporarily disabled due to API changes
    }

    public Vec3i getSize() {
        return size;
    }

    public void setSize(Vec3i size) {
        this.size = size;
        facingSlots.clear();
        if(isStructureComplete()) {
            this._modVersion = _MOD_VERSION;
            this.inventory = constructInventory();

            if(this.lastValidInventory != null) {
                int slot = 0;
                while(slot < Math.min(this.lastValidInventory.getSizeInventory(), this.inventory.getSizeInventory())) {
                    ItemStack contents = this.lastValidInventory.getStackInSlot(slot);
                    if (!contents.isEmpty()) {
                        this.inventory.setInventorySlotContents(slot, contents);
                        this.lastValidInventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                    }
                    slot++;
                }
                if(slot < this.lastValidInventory.getSizeInventory()) {
                    MinecraftHelpers.dropItems(getWorld(), this.lastValidInventory, getPos());
                }
                this.lastValidInventory = null;
            }
        } else {
            interfaceLocations.clear();
            if(this.inventory != null) {
                if(GeneralConfig.ejectItemsOnDestroy) {
                    MinecraftHelpers.dropItems(getWorld(), this.inventory, getPos());
                    this.lastValidInventory = null;
                } else {
                    this.lastValidInventory = this.inventory;
                }
            }
            this.inventory = new LegacyLargeInventory(0, "invalid", 0);
        }
        sendUpdate();
    }

    public void setMaterial(PropertyMaterial.Type material) {
        this.materialId = material.ordinal();
    }

    public PropertyMaterial.Type getMaterial() {
        return PropertyMaterial.Type.values()[this.materialId];
    }

    public int getSizeSingular() {
        return getSize().getX() + 1;
    }

    protected boolean isClientSide() {
        return getWorld() != null && getWorld().isRemote;
    }

    protected LegacySimpleInventory constructInventory() {
        if (!isClientSide() && GeneralConfig.creativeChests) {
            return constructInventoryDebug();
        }
        return !isClientSide() ? new LegacyIndexedInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64)
                : new LegacyLargeInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64);
    }

    protected LegacySimpleInventory constructInventoryDebug() {
        LegacySimpleInventory inv = !isClientSide() ? new LegacyIndexedInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64)
                : new LegacyLargeInventory(calculateInventorySize(), ColossalChestConfig._instance.getNamedId(), 64);
        Random random = new Random();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, new ItemStack(Item.REGISTRY.getRandomObject(random)));
        }
        return inv;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        LegacySimpleInventory oldInventory = this.inventory;
        LegacySimpleInventory oldLastInventory = this.lastValidInventory;
        this.inventory = null;
        this.lastValidInventory = null;
        this.recreateNullInventory = false;
        NBTTagCompound tag = super.getUpdateTag();
        this.inventory = oldInventory;
        this.lastValidInventory = oldLastInventory;
        this.recreateNullInventory = true;
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        LegacySimpleInventory oldInventory = this.inventory;
        LegacySimpleInventory oldLastInventory = this.lastValidInventory;

        if (getWorld() != null && getWorld().isRemote) {
            this.inventory = null;
            this.lastValidInventory = null;
            this.recreateNullInventory = false;
        }
        super.readFromNBT(tag);
        if (getWorld() != null && getWorld().isRemote) {
            this.inventory = oldInventory;
            this.lastValidInventory = oldLastInventory;
            this.recreateNullInventory = true;
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, getUpdateTag());
    }

    protected int calculateInventorySize() {
        int size = getSizeSingular();
        if (size == 1) {
            return 0;
        }
        return (int) Math.ceil((Math.pow(size, 3) * 27) * getMaterial().getInventoryMultiplier() / 9) * 9;
    }

    @Override
    public void updateTileEntity() {
        super.updateTileEntity();

        if(world != null) {
            if(this._modVersion != _MOD_VERSION && this.isStructureComplete()) {
                ColossalChests.clog("Upgrading colossal chest from old mod version at " + getPos());
                TileColossalChest.detector.detect(getWorld(), getPos(), null, new CubeDetector.IValidationAction() {
                    @Override
                    public L10NHelpers.UnlocalizedString onValidate(BlockPos location, IBlockState blockState) {
                        getWorld().setBlockState(location, blockState.
                                withProperty(ColossalChest.ACTIVE, true).
                                withProperty(ColossalChest.MATERIAL, PropertyMaterial.Type.WOOD));
                        return null;
                    }
                }, false);
                this._modVersion = _MOD_VERSION;
            }
        }

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
        float increaseAngle = 0.15F / Math.min(5, getSizeSingular());
        if (playersUsing > 0 && lidAngle == 0.0F) {
            world.playSound(
                    (double) getPos().getX() + 0.5D,
                    (double) getPos().getY() + 0.5D,
                    (double) getPos().getZ() + 0.5D,
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
                        (float) (0.5F + (0.5F * Math.log(getSizeSingular()))),
                        world.rand.nextFloat() * 0.05F + 0.45F + increaseAngle,
                        true
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
    @Override
    public int getSizeInventory() {
        return getInventory().getSizeInventory();
    }

    @Override
    public boolean isEmpty() {
        return getInventory().isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return getInventory().getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return getInventory().decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return getInventory().removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        getInventory().setInventorySlotContents(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return getInventory().getInventoryStackLimit();
    }

    @Override
    public void markDirty() {
        getInventory().markDirty();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return getInventory().isUsableByPlayer(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            getInventory().openInventory(player);
            triggerPlayerUsageChange(1);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!player.isSpectator()) {
            getInventory().closeInventory(player);
            triggerPlayerUsageChange(-1);
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return getInventory().isItemValidForSlot(index, stack);
    }

    @Override
    public int getField(int id) {
        return getInventory().getField(id);
    }

    @Override
    public void setField(int id, int value) {
        getInventory().setField(id, value);
    }

    @Override
    public int getFieldCount() {
        return getInventory().getFieldCount();
    }

    @Override
    public void clear() {
        getInventory().clear();
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : L10NHelpers.localize("general.colossalchests.colossalchest.name",
                getMaterial().getLocalizedName(), getSizeSingular());
    }

    @Override
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }
    // ===================== IInventory 接口结束 =====================

    private void triggerPlayerUsageChange(int change) {
        if (world != null) {
            playersUsing += change;
            world.addBlockEvent(getPos(), block, 1, playersUsing);
        }
    }

    public LegacySimpleInventory getInventory() {
        if (getWorld() != null && getWorld().isRemote && (inventory == null || inventory.getSizeInventory() != calculateInventorySize())) {
            return inventory = constructInventory();
        }
        if(lastValidInventory != null) {
            return new LegacyIndexedInventory();
        }
        if(inventory == null && this.recreateNullInventory) {
            inventory = constructInventory();
        }
        return inventory;
    }

    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return getSizeSingular() > 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        int size = getSizeSingular();
        return new AxisAlignedBB(getPos().subtract(new Vec3i(size, size, size)), getPos().add(size, size * 2, size));
    }

    public void setCenter(Vec3d center) {
        EnumFacing rotation;
        double dx = Math.abs(center.x - getPos().getX());
        double dz = Math.abs(center.z - getPos().getZ());
        boolean equal = (center.x - getPos().getX()) == (center.z - getPos().getZ());
        if(dx > dz || (!equal && getSizeSingular() == 2)) {
            rotation = DirectionHelpers.getEnumFacingFromXSign((int) Math.round(center.x - getPos().getX()));
        } else {
            rotation = DirectionHelpers.getEnumFacingFromZSing((int) Math.round(center.z - getPos().getZ()));
        }
        this.setRotation(rotation);
        this.renderOffset = new Vec3d(getPos().getX() - center.x, getPos().getY() - center.y, getPos().getZ() - center.z);
    }

    public Vec3d getRenderOffset() {
        return this.renderOffset;
    }

    public static void detectStructure(World world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == null) {
            side = EnumFacing.UP;
        }
        int[] slots = facingSlots.get(side);
        if(slots == null) {
            ContiguousSet<Integer> integers = ContiguousSet.create(
                    Range.closedOpen(0, getSizeInventory()), DiscreteDomain.integers()
            );
            slots = ArrayUtils.toPrimitive(integers.toArray(new Integer[integers.size()]));
            facingSlots.put(side, slots);
        }
        return slots;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    public boolean isStructureComplete() {
        return !getSize().equals(Vec3i.NULL_VECTOR);
    }

    public static Vec3i getMaxSize() {
        int size = ColossalChestConfig.maxSize;
        return new Vec3i(size, size, size);
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    public void addInterface(Vec3i blockPos) {
        interfaceLocations.add(blockPos);
    }

    public List<Vec3i> getInterfaceLocations() {
        return Collections.unmodifiableList(interfaceLocations);
    }

    @Override
    public ResourceLocation getLootTable() {
        return new ResourceLocation("dummy");
    }
}
