package us.pixelmon.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;

import us.pixelmon.installer.util.Utils;

public class Installer {
    private Map<URL, File> urlToFile;
    private Map<FileDescription, File> descriptionToFile;
    private String configDir; //path in the jar
    private File downloadDir;

    public Installer(File downloadDir) {
        this.configDir = "config/";
        this.downloadDir = downloadDir;
        
        urlToFile = populateURLs();
        descriptionToFile = populateDescToFile();
    }
    
    /**
     * Download the files specified in configDirectory/downloads.txt to
     * downloadDirectory
     * 
     * @return Whether the download was successful
     */
    public boolean downloadFiles() {
        for (URL url : urlToFile.keySet()) {
            File file = urlToFile.get(url);
            String fileName = file.getName();
            
            if (file.exists()) {
                System.out.println("\"" + file.getName() + "\" already exists. Skipping it...");
                
                if (fileName.toLowerCase().contains("minecraft") &&
                    fileName.toLowerCase().contains(".jar") &&
                    !fileName.toLowerCase().contains("forge")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTJAR, file);
                }
                else if (fileName.toLowerCase().contains("minecraftforge-universal")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTFORGEJAR, file);
                }
                else if (fileName.toLowerCase().contains("customnpcs")) {
                    this.descriptionToFile.put(FileDescription.CUSTOMNPCSZIP, file);
                }
                else if (fileName.toLowerCase().contains("pixelmon")) {
                    this.descriptionToFile.put(FileDescription.PIXELMONINSTALLZIP, file);
                }
                
                continue;
            }
            
            try {
                System.out.println("Downloading " + url.toString() + "...");
                URLDownload.download(url, file);
                
                if (fileName.toLowerCase().contains("minecraft") &&
                    fileName.toLowerCase().contains(".jar") &&
                    !fileName.toLowerCase().contains("forge")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTJAR, file);
                }
                else if (fileName.toLowerCase().contains("minecraftforge-universal")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTFORGEJAR, file);
                }
                else if (fileName.toLowerCase().contains("customnpcs")) {
                    this.descriptionToFile.put(FileDescription.CUSTOMNPCSZIP, file);
                }
                else if (fileName.toLowerCase().contains("pixelmon")) {
                    this.descriptionToFile.put(FileDescription.PIXELMONINSTALLZIP, file);
                }
                
                System.out.println("Download successful!");
            } catch (IOException e) {
                System.err.println("There was an error downloading file " + url.toString() + "");
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
    
    /**
     * 
     * @param runIfMcJarExists Whether to run minecraft even if .minecraft/minecraft.jar
     * already exists 
     */
    public void runMinecraft(boolean runIfMcJarExists) {
        runMinecraft(runIfMcJarExists, false);
    }
    
    /**
     * 
     * @param runIfMcJarExists Whether to run minecraft even if .minecraft/minecraft.jar
     * already exists 
     * @param detach Whether to let this process run detached from the instance of this program
     */
    public void runMinecraft(boolean runIfMcJarExists, boolean detach) {
        String baseDir;
        if (Utils.isWindows()) {
            baseDir = System.getenv("APPDATA");
        }
        else {
            baseDir = System.getenv("HOME");
        }
        File mcGameJar = new File(baseDir, ".minecraft/bin/minecraft.jar");
        if (!runIfMcJarExists) {
            if (mcGameJar.exists()) {
                System.out.println("No need to run minecraft.jar launcher; it has already been run successfully!");
                return;
            }
        }
        
        File downloadedJar = descriptionToFile.get(FileDescription.MINECRAFTJAR);
        Runtime r = Runtime.getRuntime();
        Process run = null;
        
        if (detach) {
            try {
                if (Utils.isNix()) {
                    run = r.exec("nohup java -jar " + downloadedJar.getAbsolutePath() + " &");
                }
                else {
                    run = r.exec("java -jar " + downloadedJar.getAbsolutePath()); //not sure about windows?
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                run = r.exec("java -jar " + downloadedJar.getAbsolutePath());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            
            Scanner runStd = new Scanner(run.getInputStream());
            Scanner runErr = new Scanner(run.getErrorStream());
            
            while (true) {
                if (runStd.hasNext()) {
                    System.out.println(runStd.nextLine());
                }
                if (runErr.hasNext()) {
                    System.err.println(runErr.nextLine());
                }
                else {
                    break;
                }
            }
            runStd.close();
            runErr.close();
        }
    }
    
    public void patchMinecraftJar() {
        System.out.println("Patching minecraft.jar...");
        try {
            String baseDir;
            if (Utils.isWindows()) {
                baseDir = System.getenv("APPDATA");
            }
            else {
                baseDir = System.getenv("HOME");
            }
            /*TFile mcJarOrig = new TFile(baseDir, ".minecraft/bin/minecraft.jar");
            TFile mcForgeJar = new TFile(descriptionToFile.get(FileDescription.MINECRAFTFORGEJAR));
            
            //delete META-INF from minecraft jar
            new TFile(mcJarOrig, "META-INF").rm_r();

            //patch the jar and overwrite
            mcForgeJar.cp_r(mcJarOrig);

            //update it all
            TVFS.umount();*/
            
            File mcJarOrig = new File(baseDir, ".minecraft/bin/minecraft.jar");
            File mcForgeJar = descriptionToFile.get(FileDescription.MINECRAFTFORGEJAR);
            
            List<ZipEntry> toDelete = new ArrayList<ZipEntry>(1);
            toDelete.add(new ZipEntry("META-INF"));
            
            Utils.deleteEntriesFromZip(mcJarOrig, toDelete);
            Utils.zip_cp_r(mcForgeJar, mcJarOrig);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Call this AFTER you patch with patchMinecraftJar()
     */
    public void addModsAndCoremods() {
        File mcRootDir;
        File modsDir;
        File coremodsDir;
        String baseDir;
        if (Utils.isWindows()) {
            baseDir = System.getenv("APPDATA");
        }
        else {
            baseDir = System.getenv("HOME");
        }
        mcRootDir = new File(baseDir, ".minecraft");
        modsDir = new File(mcRootDir, "mods");
        coremodsDir = new File(mcRootDir, "coremods");
        
        if (!mcRootDir.exists()) {
            System.err.println("The folder " + mcRootDir.getAbsolutePath() + " doesn't exist.");
            return;
        }
        Utils.mkdir(modsDir); Utils.mkdir(coremodsDir);
        
        for (FileDescription desc : this.descriptionToFile.keySet()) {
            File jarOrZip = this.descriptionToFile.get(desc);
            
            if (desc.shouldExtractToRootMCDir()) {
                try {
//                    TFile zipToExtract = new TFile(jarOrZip);
//                    zipToExtract.cp_rp(mcRootDir);
                    Utils.unzip(jarOrZip, mcRootDir);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (desc.isMCForgeMod()) {
                try {
                Files.copy(jarOrZip.toPath(), new File(modsDir, jarOrZip.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (desc.isMCForgeCoreMod()) {
                try {
                    Files.copy(jarOrZip.toPath(), new File(coremodsDir, jarOrZip.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private Map<URL, File> populateURLs() {
        Map<URL, File> map = new HashMap<URL, File>();
        
        //Scanner for download.txt in the jar
        Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream(this.configDir + "downloads.txt"));
        
        while (s.hasNext()) {
            String line = s.nextLine();
            try {
                URL url = new URL(line.split(" ")[0]);
                File fileName = new File(downloadDir, line.split(" ")[1].trim());
                map.put(url, fileName);
            }
            catch (MalformedURLException e) {
                System.err.println("Download link \"" + line + "\" is an invalid url. Exiting...");
                System.exit(1);
            }
        }
        s.close();
        return map;
    }
    
    private Map<FileDescription, File> populateDescToFile() {
        Map<FileDescription, File> pop = new HashMap<FileDescription, File>();
        
        for (URL url : urlToFile.keySet()) {
            File file = urlToFile.get(url);
            String fileName = file.getName();
            
            if (file.exists()) {
                if (fileName.toLowerCase().contains("minecraft") &&
                    fileName.toLowerCase().contains(".jar") &&
                    !fileName.toLowerCase().contains("forge")) {
                    pop.put(FileDescription.MINECRAFTJAR, file);
                }
                else if (fileName.toLowerCase().contains("minecraftforge-universal")) {
                    pop.put(FileDescription.MINECRAFTFORGEJAR, file);
                }
                else if (fileName.toLowerCase().contains("customnpcs")) {
                    pop.put(FileDescription.CUSTOMNPCSZIP, file);
                }
                else if (fileName.toLowerCase().contains("pixelmon")) {
                    pop.put(FileDescription.PIXELMONINSTALLZIP, file);
                }
                
                continue;
            }
        }
        
        return pop;
    }
    
    public boolean mcGameDirExists() {
        String baseDir;
        if (Utils.isWindows()) {
            baseDir = System.getenv("APPDATA");
        }
        else {
            baseDir = System.getenv("HOME");
        }
        File mcGameDir = new File(baseDir, ".minecraft");
        return mcGameDir.exists();
    }

    public Map<URL, File> getUrlToFile() {
        return urlToFile;
    }
}
