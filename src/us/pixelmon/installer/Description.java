package us.pixelmon.installer;

public enum Description {
    MINECRAFTFORGEJAR("MinecraftForge"),
    MINECRAFTJAR("MinecraftJar"),
    PIXELMONINSTALLZIP("PixelmonInstallZip"),
    CUSTOMNPCSZIP("CustomNPCsZip");
    
    private final String desc;
    private Description(String desc) {
        this.desc = desc;
    }
    
    @Override
    public String toString() {
        return this.desc;
    }
}
