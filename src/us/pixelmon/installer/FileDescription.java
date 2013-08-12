package us.pixelmon.installer;

public enum FileDescription {
    MINECRAFTFORGEJAR("MinecraftForge", false, false, false),
    MINECRAFTJAR("MinecraftJar", false, false, false),
    PIXELMONINSTALLZIP("PixelmonInstallZip", true, false, false), //needs to be extracted at .minecraft
    PIXELMONMODZIP("PixelmonModZip", false, true, false), //this is the actual mod
    CUSTOMNPCSZIP("CustomNPCsZip", false, true, false), //this one is a mod
    CUSTOMTEXTUREPACK("CustomTexturePack", false, false, false);
    
    private final String desc;
    private final boolean isMCForgeMod;
    private final boolean isMCForgeCoreMod;
    private final boolean extractToMCRoot;
    
    /**
     * Use this constructor if the zip is a MCForge mod or coremod.
     * Otherwise, use the other constructor.
     * 
     * @param desc The description of the jar file
     * @param extractToMCRoot Whether or not this zip is extracted in the .minecraft folder root
     * @param isMCForgeCoreMod true if the zip is a core mod,
     * not a regualar mod. If the zip is neither, use the other constructor
     */
    private FileDescription(String desc, boolean extractToMCRoot, boolean isMCForgeMod, boolean isMCForgeCoreMod) {
        this.desc = desc;
        this.extractToMCRoot = extractToMCRoot;
        this.isMCForgeMod = isMCForgeMod;
        this.isMCForgeCoreMod = isMCForgeCoreMod;
    }
    
    @Override
    public String toString() {
        return this.desc;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isMCForgeMod() {
        return isMCForgeMod;
    }

    public boolean isMCForgeCoreMod() {
        return isMCForgeCoreMod;
    }

    public boolean shouldExtractToRootMCDir() {
        return extractToMCRoot;
    }
    
    
}
