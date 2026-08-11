package org.cyclops.colossalchests.client.gui.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.colossalchests.network.packet.ClickWindowPacketOverride;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.client.gui.component.button.GuiButtonArrow;
import org.cyclops.cyclopscore.client.gui.container.ScrollingGuiContainer;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiColossalChest extends ScrollingGuiContainer {

    private static final int TEXTUREWIDTH = 195;
    private static final int TEXTUREHEIGHT = 194;

    private static final ResourceLocation GUI_TEXTURE = 
            new ResourceLocation("colossalchests", "textures/gui/colossal_chest.png");

    private final TileColossalChest tile;

    private GuiButtonArrow buttonUp;
    private GuiButtonArrow buttonDown;

    public GuiColossalChest(InventoryPlayer inventory, TileColossalChest tile) {
        super(new ContainerColossalChest(inventory, tile));
        this.tile = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(buttonUp = new GuiButtonArrow(0, this.guiLeft + 173, this.guiTop + 7, GuiButtonArrow.Direction.NORTH));
        buttonList.add(buttonDown = new GuiButtonArrow(1, this.guiLeft + 173, this.guiTop + 129, GuiButtonArrow.Direction.SOUTH));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        int multiplier = Minecraft.getMinecraft().player.isSneaking() ? 9 : 1;
        int i = (button == buttonUp) ? 1 * multiplier : ((button == buttonDown ? -1 * multiplier : 0));
        this.currentScroll = (float)((double)this.currentScroll - (double)i / (double)getScrollStep());
        this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
        getScrollingInventoryContainer().scrollTo(this.currentScroll);
    }

    @Override
    protected boolean isSearchEnabled() {
        return false;
    }

    @Override
    protected boolean isSubsetRenderSlots() {
        return true;
    }

    @Override
    public String getGuiTexture() {
        return "colossalchests:textures/gui/colossal_chest.png";
    }

    @Override
    protected int getBaseXSize() {
        return TEXTUREWIDTH;
    }

    @Override
    protected int getBaseYSize() {
        return TEXTUREHEIGHT;
    }

    protected void drawForgegroundString() {
        fontRenderer.drawString(tile.getName(), 8 + offsetX, 6 + offsetY, 4210752);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawForgegroundString();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // 1. 绘制背景纹理
        this.drawDefaultBackground();
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        // 2. 绘制红色滑块（调试用）
        drawDebugScrollBar();
    }

    private void drawDebugScrollBar() {
        int scrollBarX = this.guiLeft + 175;
        int scrollBarY = this.guiTop + 25;
        int scrollBarHeight = 105;
        int sliderWidth = 12;
        int sliderHeight = 15;

        int sliderY = scrollBarY + (int) ((scrollBarHeight - sliderHeight) * this.currentScroll);
        if (sliderY < scrollBarY) sliderY = scrollBarY;
        if (sliderY + sliderHeight > scrollBarY + scrollBarHeight) {
            sliderY = scrollBarY + scrollBarHeight - sliderHeight;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(scrollBarX, sliderY + sliderHeight, 0).color(255, 0, 0, 255).endVertex();
        buffer.pos(scrollBarX + sliderWidth, sliderY + sliderHeight, 0).color(255, 0, 0, 255).endVertex();
        buffer.pos(scrollBarX + sliderWidth, sliderY, 0).color(255, 0, 0, 255).endVertex();
        buffer.pos(scrollBarX, sliderY, 0).color(255, 0, 0, 255).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, ClickType clickType) {
        if (slotIn != null) {
            slotId = slotIn.slotNumber;
        }
        windowClick(this.inventorySlots.windowId, slotId, clickedButton, clickType, this.mc.player);
    }

    protected ItemStack windowClick(int windowId, int slotId, int mouseButtonClicked, ClickType p_78753_4_, EntityPlayer playerIn) {
        short short1 = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
        ItemStack itemstack = playerIn.openContainer.slotClick(slotId, mouseButtonClicked, p_78753_4_, playerIn);
        ColossalChests._instance.getPacketHandler().sendToServer(
                new ClickWindowPacketOverride(windowId, slotId, mouseButtonClicked, p_78753_4_, itemstack, short1));
        return itemstack;
    }
}
