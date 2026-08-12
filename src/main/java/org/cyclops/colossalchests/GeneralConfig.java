
package org.cyclops.colossalchests;

import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableType;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.tracking.Analytics;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    /**
     * The current mod version, will be used to check if the player's config isn't out of date and
     * warn the player accordingly.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "Config version for " + Reference.MOD_NAME +".\nDO NOT EDIT MANUALLY!", showInGui = false)
    public static String version = Reference.MOD_VERSION;

    /**
     * If the recipe loader should crash when finding invalid recipes.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If the recipe loader should crash when finding invalid recipes.", requiresMcRestart = true)
    public static boolean crashOnInvalidRecipe = false;

    /**
     * If mod compatibility loader should crash hard if errors occur in that process.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If mod compatibility loader should crash hard if errors occur in that process.", requiresMcRestart = true)
    public static boolean crashOnModCompatCrash = false;

    /**
     * If an anonymous mod startup analytics request may be sent to our analytics service.
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.CORE, comment = "If an anonymous mod startup analytics request may be sent to our analytics service.")
    public static boolean analytics = true;

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
     * Always create full creative-mode chests when formed. Should not be used in survival worlds!
     */
    @ConfigurableProperty(category = ConfigurableTypeCategory.GENERAL, comment = "Always create full creative-mode chests when formed. Should not be used in survival worlds!", isCommandable = true)
    public static boolean creativeChests = false;

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
    public void onRegistered() {
        getMod().putGenericReference(ModBase.REFKEY_CRASH_ON_INVALID_RECIPE, GeneralConfig.crashOnInvalidRecipe);
        getMod().putGenericReference(ModBase.REFKEY_CRASH_ON_MODCOMPAT_CRASH, GeneralConfig.crashOnModCompatCrash);

        if(analytics) {
            Analytics.registerMod(getMod(), Reference.GA_TRACKING_ID);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
