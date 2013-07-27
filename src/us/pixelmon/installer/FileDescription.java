package us.pixelmon.installer;

public enum FileDescription {
    MINECRAFTFORGEJAR("MinecraftForge", false),
    MINECRAFTJAR("MinecraftJar", false),
    PIXELMONINSTALLZIP("PixelmonInstallZip", true), //needs to be extracted at .minecraft
    PIXELMONMODZIP("PixelmonModZip", false, false), //this is the actual mod
    CUSTOMNPCSZIP("CustomNPCsZip", false, false); //this one is a mod
    
    private final String desc;
    private final boolean isMCForgeMod;
    private final boolean isMCForgeCoreMod;
    private final boolean extractToMCRoot;
    
    /**
     * 
     * @param desc The description of the jar file
     * @param isMCForgeMod true if the zip is a mod, not a coremod
     */
    private FileDescription(String desc, boolean extractToMCRoot) {
        this.desc = desc;
        this.isMCForgeMod = false;
        this.isMCForgeCoreMod = false;
        this.extractToMCRoot = extractToMCRoot;
    }
    
    /**
     * Use this constructor if the zip is a MCForge mod or coremod.
     * Otherwise, use the other constructor.
     * 
     * @param desc The description of the jar file
     * @param extractToMCRoot Whether or not this zip is extracted in the .minecraft folder root
     * @param isMCForgeCoreMod true if the zip is a core mod,
     * not a regualar mod. If the zip is neither, use the other constructor
     */
    private FileDescription(String desc, boolean extractToMCRoot, boolean isMCForgeCoreMod) {
        this.desc = desc;
        this.extractToMCRoot = extractToMCRoot;
        if (isMCForgeCoreMod) {
            this.isMCForgeCoreMod = true;
            this.isMCForgeMod = false;
        }
        else {
            this.isMCForgeCoreMod = false;
            this.isMCForgeMod = true;
        }
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
