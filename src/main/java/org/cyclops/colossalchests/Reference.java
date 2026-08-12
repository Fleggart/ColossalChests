package org.cyclops.colossalchests;

import net.minecraft.util.ResourceLocation;

/**
 * Class that can hold basic static things that are better not hard-coded
 * like mod details, texture paths, ID's...
 * @author rubensworks
 *
 */
@SuppressWarnings("javadoc")
public class Reference {
    
    // Mod info
    public static final String MOD_ID = "colossalchests";
    public static final String MOD_NAME = "ColossalChests";
    public static final String MOD_VERSION = "@VERSION@";
    // 删除 MOD_BUILD_NUMBER
    public static final String MOD_CHANNEL = MOD_ID;
    public static final String MOD_MC_VERSION = "@MC_VERSION@";
    // 删除 MOD_FINGERPRINT
    public static final String GA_TRACKING_ID = "UA-65307010-5";
    public static final String VERSION_URL = "https://raw.githubusercontent.com/CyclopsMC/Versions/master/1.12/ColossalChests.txt";
    
    // Paths
    public static final String TEXTURE_PATH_GUI = "textures/gui/";
    public static final String TEXTURE_PATH_SKINS = "textures/skins/";
    public static final String TEXTURE_PATH_MODELS = "textures/models/";
    public static final String TEXTURE_PATH_ENTITIES = "textures/entities/";
    public static final String TEXTURE_PATH_GUIBACKGROUNDS = "textures/gui/title/background/";
    public static final String TEXTURE_PATH_ITEMS = "textures/items/";
    public static final String TEXTURE_PATH_PARTICLES = "textures/particles/";
    public static final String MODEL_PATH = "models/";
    
    // MOD ID's
    // 删除 MOD_FORGE 和 MOD_FORGE_VERSION
    public static final String MOD_FORGE_VERSION_MIN = "14.23.5.2768";
    public static final String MOD_CYCLOPSCORE = "cyclopscore";
    public static final String MOD_CYCLOPSCORE_VERSION = "@CYCLOPSCORE_VERSION@";
    public static final String MOD_CYCLOPSCORE_VERSION_MIN = "1.3.0";
    public static final String MOD_COMMONCAPABILITIES = "commoncapabilities";
    public static final String MOD_COMMONCAPABILITIES_VERSION_MIN = "2.4.0";
    // 删除 MOD_IRONCHEST
    
    // Dependencies
    public static final String MOD_DEPENDENCIES = 
            "required-after:forge@[14.23.5.2768,);" +
            "required-after:cyclopscore@[1.3.0,);" +
            "after:commoncapabilities@[2.4.0,);";
}
