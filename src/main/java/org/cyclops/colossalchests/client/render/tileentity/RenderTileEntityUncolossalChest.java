package org.cyclops.colossalchests.client.render.tileentity;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

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
        
        GlStateManager.translate((float)x + 0.5F, (float)y + 0.2F, (float)z + 0.5F);   
        
        // 2. 旋转朝向
        if (tile != null) {
            float rotation = 0F;
            switch (tile.getRotation()) {
                case NORTH: rotation = 0F; break;
                case SOUTH: rotation = 180F; break;
                case WEST:  rotation = 90F; break;
                case EAST:  rotation = -90F; break;
                default:    rotation = 0F;
            }
            GlStateManager.rotate(rotation, 0F, 1F, 0F);
        }
        
        GlStateManager.scale(1.0F, -1.0F, -1.0F);
        
        float size = 0.2F;
        GlStateManager.scale(size, size, size);
        
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
