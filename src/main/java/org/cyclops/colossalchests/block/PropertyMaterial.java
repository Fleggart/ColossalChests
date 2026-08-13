package org.cyclops.colossalchests.block;

import net.minecraft.block.properties.PropertyEnum;
import org.cyclops.colossalchests.Reference;
import org.cyclops.cyclopscore.helper.L10NHelpers;

import java.util.Locale;

public class PropertyMaterial extends PropertyEnum<PropertyMaterial.Type> {

    // ========== 单例 ==========
    private static final PropertyMaterial INSTANCE = new PropertyMaterial("material", Type.class, java.util.Arrays.asList(Type.values()));

    private PropertyMaterial(String name, Class<Type> valueClass, java.util.Collection<Type> allowedValues) {
        super(name, valueClass, allowedValues);
    }

    public static PropertyMaterial getInstance() {
        return INSTANCE;
    }

    public enum Type {
        WOOD(1),
        COPPER(1.666),
        IRON(2),
        SILVER(2.666),
        GOLD(3),
        DIAMOND(4),
        OBSIDIAN(4);

        private final double inventoryMultiplier;

        Type(double inventoryMultiplier) {
            this.inventoryMultiplier = inventoryMultiplier;
        }

        public double getInventoryMultiplier() {
            return this.inventoryMultiplier;
        }

        public String getLocalizedName() {
            return L10NHelpers.localize("material." + Reference.MOD_ID + "." + 
                    toString().toLowerCase(Locale.ENGLISH));
        }

        public boolean isExplosionResistant() {
            return this == OBSIDIAN;
        }
    }
}
