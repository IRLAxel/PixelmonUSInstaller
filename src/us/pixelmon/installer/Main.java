package us.pixelmon.installer;

import java.io.File;

public class Main {
    public static void main(String args[]) {
        File downloadDir = new File("./downloads");
        File mcDirToMake = new File("./mcdir");
        File tmpDir      = new File("./tmp");
        
        mkdir(downloadDir); mkdir(mcDirToMake); mkdir(tmpDir);
        
        Installer installer = new Installer(downloadDir, mcDirToMake, tmpDir);
        installer.downloadFiles();
    }
    
    private static void mkdir(File f) {
        if (!f.exists()) {
            f.mkdir();
        }
    }
}
