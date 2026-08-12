package org.cyclops.colossalchests;

import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableType;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    /**
     * If items should be ejected from the chests if one of the structure blocks are removed.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "If items should be ejected from the chests if one of the structure blocks are removed.")
    public static boolean ejectItemsOnDestroy = false;

    /**
     * If the higher tier metal variants can be crafted.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "If the higher tier metal variants can be crafted.")
    public static boolean metalVariants = true;

    /**
     * Maximum buffer byte size for adaptive inventory slots fragmentation.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "Maximum buffer byte size for adaptive inventory slots fragmentation.")
    public static int maxPacketBufferSize = 20000;

    /**
     * If the interface input overlay should always be rendered on chests.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "If the interface input overlay should always be rendered on chests.", isCommandable = true)
    public static boolean alwaysShowInterfaceOverlay = true;

    /**
     * The type of this config.
     */
    public static ConfigurableType TYPE = ConfigurableType.DUMMY;

    /**
     * Create a new instance.
     */
    public GeneralConfig() {
        super(ColossalChests._instance, true, "general", null, GeneralConfig.class);
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
