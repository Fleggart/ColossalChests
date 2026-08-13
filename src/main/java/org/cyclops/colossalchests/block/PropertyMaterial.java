package org.cyclops.colossalchests.block;

import net.minecraft.block.properties.IProperty;
import org.cyclops.colossalchests.Reference;
import org.cyclops.cyclopscore.helper.L10NHelpers;

import java.util.*;
import java.util.stream.Collectors;

public class PropertyMaterial implements IProperty<PropertyMaterial.Type> {

    private final String name;
    private final Set<Type> allowedValues;

    private PropertyMaterial(String name, Collection<Type> values) {
        this.name = name;
        this.allowedValues = Collections.unmodifiableSet(new HashSet<>(values));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<Type> getAllowedValues() {
        return this.allowedValues;
    }

    @Override
    public Class<Type> getValueClass() {
        return Type.class;
    }

    @Override
    public Optional<Type> parseValue(String value) {
        for (Type type : allowedValues) {
            if (type.toString().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getName(Type value) {
        return value.toString().toLowerCase(Locale.ENGLISH);
    }

    // ============ 静态工厂方法 ============

    public static PropertyMaterial create(String name) {
        return new PropertyMaterial(name, Arrays.asList(Type.values()));
    }

    // ============ 内部枚举 ============

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
