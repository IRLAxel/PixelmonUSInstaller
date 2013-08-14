package us.pixelmon.installer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
                    throw new IOException("Could not delete " + sub.getAbsolutePath());
                }
            }
        }
        if (!toDelete.delete()) {
            throw new IOException("Could not delete " + toDelete.getAbsolutePath());
        }
    }
    
    public static void mkdir(File f) {
        if (!f.exists()) {
            f.mkdir();
        }
    }
    
    /**
     * Copy the contents of one zip into the contents of another and overwrite.
     * This is not just copying; this effectively "merges" the files. Probably not
     * very effecient at all.
     * @param source
     * @param dest
     * @throws IOException
     */
    public static void zip_cp_r(File source, File dest) throws IOException {
        //default buffer size is 32kb
        zip_cp_r(source, dest, new byte[1024 * 32]);
    }
    
    /**
     * Copy the contents of one zip into the contents of another and overwrite.
     * This is not just copying; this effectively "merges" the files. Probably not
     * very effecient at all.
     * @param source
     * @param dest
     * @param buffer
     * @throws IOException
     */
    public static void zip_cp_r(File source, File dest, byte[] buffer) throws IOException {
        boolean destExistsOriginally = dest.exists();
        
        if (!dest.exists()) {
            dest.createNewFile();
        }
        
        File destTmp = null;
        ZipInputStream sourceInStream = null;
        ZipInputStream destTmpInStream = null;
        ZipOutputStream destOutStream = null;
        try {
            //copy source to a temp file and then overwrite the original with source and dest streams
            destTmp = new File(dest.getParentFile(), dest.getName() + ".tmp");
            Files.copy(dest.toPath(), destTmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
    
            sourceInStream = new ZipInputStream(new FileInputStream(source));
            destTmpInStream = new ZipInputStream(new FileInputStream(destTmp));
            destOutStream = new ZipOutputStream(new FileOutputStream(dest));
    
            ZipEntry sourceEntry = sourceInStream.getNextEntry();
            ZipEntry destEntry = destTmpInStream.getNextEntry();
            
            //write all of source to dest first
            while (sourceEntry != null) {
                destOutStream.putNextEntry(sourceEntry);
                
                int len;
                while ((len = sourceInStream.read(buffer)) > 0) {
                    destOutStream.write(buffer, 0, len);
                }
                sourceEntry = sourceInStream.getNextEntry();
            }
            
            //write all of dest to the new dest stream second and don't let it overwrite
            while(destEntry != null && destExistsOriginally) {
                try {
                    destOutStream.putNextEntry(destEntry);
                    
                    int len;
                    while ((len = destTmpInStream.read(buffer)) > 0) {
                        destOutStream.write(buffer, 0, len);
                    }
                }
                catch (ZipException e) {
                    if (e.getMessage().contains("duplicate entry:")) {
                        //skip duplicates
                        destEntry = destTmpInStream.getNextEntry();
                        continue;
                    }
                    else {
                        throw e;
                    }
                }
                destEntry = destTmpInStream.getNextEntry();
            }
        }
        finally {
            destTmpInStream.closeEntry();
            destTmpInStream.close();
            destTmp.delete();
            sourceInStream.closeEntry();
            sourceInStream.close();
            destOutStream.closeEntry();
            destOutStream.close();
        }
    }
    
    /**
     * Use this to delete some entries from a zip file. WARNING!! This method does NOT
     * use very advanced string matching. Check the source before using.
     * @param zipFile
     * @param entriesToDelete
     * @throws IOException
     */
    public static void deleteEntriesFromZip(File zipFile, List<ZipEntry> entriesToDelete) throws IOException {
        byte[] buffer = new byte[1024 * 32];
        File newZipFile = new File(zipFile.getParentFile(), zipFile.getName() + ".tmp");
        
        if (newZipFile.exists()) {
            newZipFile.delete();
        }
        
        ZipOutputStream zos = null;
        ZipInputStream zis = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(newZipFile));
            zis = new ZipInputStream(new FileInputStream(zipFile));
            
            ZipEntry entry = zis.getNextEntry();
            
            while (entry != null) {
                boolean shouldCopy = true;
                for (ZipEntry toCheck : entriesToDelete) {
                    if (entry.getName().contains(toCheck.getName())) {
                        shouldCopy = false;
                    }
                }
                
                if (shouldCopy) {
                    zos.putNextEntry(entry);
                    
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
    
                entry = zis.getNextEntry();
            }
        }
        finally {
            zos.closeEntry();
            zos.close();
            zis.closeEntry();
            zis.close();
        }
        
        //Replace the old zip file
        Files.copy(newZipFile.toPath(), zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        //delete temporary
        newZipFile.delete();
    }
    
    /**
     * Unzip a zip file to a destination.
     * The destination must exist before calling this method!
     * @param zipFile
     * @throws IOException 
     */
    public static void unzip(File zipFile, File destDir) throws IOException {
        unzip(zipFile, destDir, new byte[1024 * 32]);
    }
    
    public static void unzip(File zipFile, File destDir, byte[] buffer) throws IOException {
        if (!destDir.exists()) {
            throw new IOException("Destination directory " + destDir.getAbsolutePath() + " doesn't exist");
        }
        
        ZipInputStream zis = null;
        FileOutputStream fos = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry = zis.getNextEntry();
            
            while (entry != null) {
                File extractTo = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    extractTo.mkdir();
                    entry = zis.getNextEntry();
                    continue;
                }
                else {
                    extractTo.createNewFile();
                }
                fos = new FileOutputStream(extractTo);
                
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                
                fos.close();
                entry = zis.getNextEntry();
            }
        }
        finally {
            zis.closeEntry();
            zis.close();
            fos.close();
        }
    }
}
