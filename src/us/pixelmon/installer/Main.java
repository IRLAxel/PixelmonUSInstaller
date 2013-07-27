package us.pixelmon.installer;

import java.io.File;

public class Main {
    public static final File installerDataRoot =    new File("./.installerdata");
    public static final File downloadDir =          new File(installerDataRoot, "./downloads");
    public static final File tmpDir      =          new File(installerDataRoot, "./tmp");
    
    public static void main(String args[]) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        
        mkdir(installerDataRoot); mkdir(downloadDir); mkdir(tmpDir);
        
        Installer installer = new Installer(downloadDir, tmpDir);
        installer.downloadFiles();
        installer.runMinecraft(false);
        installer.patchMinecraftJar();
        installer.runMinecraft(true);
    }
    
    private static void mkdir(File f) {
        if (!f.exists()) {
            f.mkdir();
        }
    }
}
