package org.cyclops.colossalchests.block;

import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.block.properties.PropertyHelper;
import org.cyclops.colossalchests.Reference;
import org.cyclops.cyclopscore.helper.L10NHelpers;

import java.util.Collection;
import java.util.Locale;

/**
 * Material property for the chests
 * @author rubensworks
 */
public class PropertyMaterial extends PropertyHelper<PropertyMaterial.Type> {

    private final Set<PropertyMaterial.Type> allowedValues;

    protected PropertyMaterial(String name, Collection<PropertyMaterial.Type> values) {
        super(name, Type.class);
        this.allowedValues = Collections.unmodifiableSet(new HashSet<>(values));
    }

    public Collection<PropertyMaterial.Type> getAllowedValues() {
        return this.allowedValues;
    }

    @Override
    public Optional<Type> parseValue(String value) {
        for (PropertyMaterial.Type type : allowedValues) {
            if(type.toString().equalsIgnoreCase(value)) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public String getName(PropertyMaterial.Type value) {
        return value.toString().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Create a new PropertyStatus with all Enum constants of the given class.
     * @param name The property name.
     * @param clazz The property class.
     * @return The property
     */
    public static PropertyMaterial create(String name, Class clazz) {
        return create(name, clazz, t -> true);
    }

    /**
     * Create a new PropertyStatus with all Enum constants of the given class.
     * @param name The property name.
     * @param clazz The property class.
     * @param filter The filter for checking property values.
     * @return The property
     */
    public static PropertyMaterial create(String name, Class clazz, Predicate filter) {
        
        return create(name, clazz, Arrays.stream(clazz.getEnumConstants())
            .filter(filter)
            .collect(Collectors.toList())
         );
        
    }

    /**
     * Create a new PropertyStatus with all Enum constants of the given class.
     * @param name The property name.
     * @param clazz The property class.
     * @param values The possible property values.
     * @return The property
     */
    public static PropertyMaterial create(String name, Class clazz, Collection values) {
        return new PropertyMaterial(name, values);
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

        private Type(double inventoryMultiplier) {
            this.inventoryMultiplier = inventoryMultiplier;
        }

        public double getInventoryMultiplier() {
            return this.inventoryMultiplier;
        }

        public String getLocalizedName() {
            return L10NHelpers.localize("material." + Reference.MOD_ID + "." + toString().toLowerCase(Locale.ENGLISH));
        }

        public boolean isExplosionResistant() {
            return this == OBSIDIAN;
        }

    }

}
