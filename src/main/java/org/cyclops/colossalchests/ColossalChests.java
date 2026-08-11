package org.cyclops.colossalchests;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Level;
import org.cyclops.colossalchests.block.ChestWallConfig;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.block.InterfaceConfig;
import org.cyclops.colossalchests.block.UncolossalChestConfig;
import org.cyclops.colossalchests.item.ItemUpgradeToolConfig;
import org.cyclops.colossalchests.modcompat.IronChestModCompat;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.IObjectReference;
import org.cyclops.cyclopscore.init.ItemCreativeTab;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.modcompat.ModCompatLoader;
import org.cyclops.cyclopscore.proxy.ICommonProxy;

import java.util.Map;
import java.util.Set;

/**
 * The main mod class of this mod.
 * @author rubensworks
 *
 */
@Mod(modid = "colossalchests")
public class ColossalChests extends ModBaseVersionable {
    
    @SidedProxy(clientSide = "org.cyclops.colossalchests.proxy.ClientProxy", serverSide = "org.cyclops.colossalchests.proxy.CommonProxy")
    public static ICommonProxy proxy;
    
    @Instance("colossalchests")
    public static ColossalChests _instance;

    public ColossalChests() {
        super("colossalchests", "ColossalChests", "@VERSION@");
    }

    @Override
    protected void loadModCompats(ModCompatLoader modCompatLoader) {
        modCompatLoader.addModCompat(new IronChestModCompat());
    }

    @Override
    protected RecipeHandler constructRecipeHandler() {
        return new RecipeHandler(this, "recipes.xml") {
            protected void loadPredefineds(Map<String, ItemStack> predefinedItems, Set<String> predefinedValues) {
                super.loadPredefineds(predefinedItems, predefinedValues);
                if(GeneralConfig.metalVariants) {
                    predefinedValues.add("colossalchests:metalVariants");
                }
            }
        };
    }

    @EventHandler
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Advancements.load();
    }
    
    @EventHandler
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
    
    @EventHandler
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }
    
    @EventHandler
    @Override
    public void onServerStarting(FMLServerStartingEvent event) {
        super.onServerStarting(event);
    }

    @EventHandler
    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        super.onServerStarted(event);
    }

    @EventHandler
    @Override
    public void onServerStopping(FMLServerStoppingEvent event) {
        super.onServerStopping(event);
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return new ItemCreativeTab(this, new IObjectReference<Item>() {
            @Override
            public Item getObject() {
                return Item.getItemFromBlock(Blocks.CHEST);
            }
        });
    }

    @Override
    public void onMainConfigsRegister(ConfigHandler configs) {
        configs.add(new ChestWallConfig());
        configs.add(new ColossalChestConfig());
        configs.add(new InterfaceConfig());
        configs.add(new UncolossalChestConfig());
        configs.add(new ItemUpgradeToolConfig());
    }

    @Override
    public void onGeneralConfigsRegister(ConfigHandler configHandler) {
        configHandler.add(new GeneralConfig());
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }

    public static void clog(String message) {
        clog(Level.INFO, message);
    }
    
    public static void clog(Level level, String message) {
        ColossalChests._instance.getLoggerHelper().log(level, message);
    }
    
}
