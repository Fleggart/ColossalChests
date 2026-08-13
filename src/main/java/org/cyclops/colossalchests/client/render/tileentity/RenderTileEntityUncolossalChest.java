package org.cyclops.colossalchests.client.render.tileentity;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

/**
 * Renderer for the Uncolossal Chest.
 * * @author rubensworks
 */
public class RenderTileEntityUncolossalChest extends TileEntitySpecialRenderer<TileUncolossalChest> {

    private static final ResourceLocation TEXTURE_CHEST = 
        new ResourceLocation("textures/entity/chest/normal.png");
    
    private final ModelChest model;

    public RenderTileEntityUncolossalChest() {
        this.model = new ModelChest();
    }

    @Override
    public void render(TileUncolossalChest tile, double x, double y, double z, 
                       float partialTicks, int destroyStage, float alpha) {
        
        GlStateManager.pushMatrix();
        
        // 1. 将原点平移到方块的中心点 (0.5, 0.5, 0.5)
        GlStateManager.translate((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
        
        // 2. 旋转朝向
        if (tile != null) {
            float rotation = 0F;
            switch (tile.getRotation()) {
                case NORTH: rotation = 180F; break;
                case SOUTH: rotation = 0F; break;
                case WEST:  rotation = 90F; break;
                case EAST:  rotation = -90F; break;
                default:    rotation = 0F;
            }
            GlStateManager.rotate(rotation, 0F, 1F, 0F);
        }
        
        // 3. 核心修复：Y 轴与 Z 轴同时乘以 -1.0F，将 ModelChest “翻正”
        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        
        // 4. 应用你的迷你箱子缩放比例
        float size = 0.3F * 1.125F;
        GlStateManager.scale(size, size, size);
        
        // 5. 调整垂直位置，让迷你箱子落到地面（根据实际视觉微调 Y 轴平移）
        // 因为前面经过了 -1.0F 翻转，这里的 Y 轴负方向代表向上，正方向代表向下
        GlStateManager.translate(-0.5F, -0.5F, -0.5F); 
        
        // 绑定纹理
        bindTexture(TEXTURE_CHEST);
        
        // 箱盖动画
        if (tile != null) {
            float lidAngle = tile.prevLidAngle + (tile.lidAngle - tile.prevLidAngle) * partialTicks;
            lidAngle = 1.0F - lidAngle;
            lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
            model.chestLid.rotateAngleX = -(lidAngle * (float) Math.PI / 2.0F);
        }
        
        // 渲染模型
        model.renderAll();
        
        GlStateManager.popMatrix();
    }
}
