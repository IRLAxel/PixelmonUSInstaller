package us.pixelmon.installer;

import java.io.File;
import java.io.IOException;

public class Utils {
    public static boolean isNix() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("nix") ||
            os.contains("nux")  ||
            os.contains("aix")) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * This method acts much like bash's "rm -rf $file" command. It will
     * delete the directory and all of its contents! If the File passed is
     * not a folder, it will be deleted, too.
     * 
     * **This WILL follow symlinks! Be careful!
     * 
     * @param folderToDelete Folder to delete with all of its contents
     * @return Whether the delete was successful
     */
    public static void deleteRecursive(File toDelete) throws IOException {
        for (File sub : toDelete.listFiles()) {
            if (sub.isDirectory()) {
                deleteRecursive(sub);
            }
            else {
                if (!sub.delete()) {
                    throw new IOException();
                }
            }
        }
    }
    
    public static void mkdir(File f) {
        if (!f.exists()) {
            f.mkdir();
        }
    }
}
