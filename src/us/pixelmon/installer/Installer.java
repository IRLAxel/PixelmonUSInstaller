package us.pixelmon.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import us.pixelmon.installer.util.Utils;

public class Installer {
    public static String mcVersion;
    private Map<URL, File> urlToFile;
    private Map<FileDescription, File> descriptionToFile;
    private String configDir; //path in the jar
    private File downloadDir;
    private File mcGameJar;

    public Installer(File downloadDir) {
        this.configDir = "config/";
        this.downloadDir = downloadDir;
        mcVersion = parseVersion();
        this.mcGameJar = new File(getMcRootDir(), "versions/" + Installer.mcVersion + "/" + Installer.mcVersion + ".jar");
        
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
                
                if (fileName.equalsIgnoreCase("minecraft-launcher.jar")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTLAUNCHER, file);
                }
                else if (fileName.equalsIgnoreCase("minecraftforge-installer.jar")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTFORGEINSTALLER, file);
                }
                else if (fileName.toLowerCase().contains("CustomNPCs_1.5.2.zip")) {
                    this.descriptionToFile.put(FileDescription.CUSTOMNPCSZIP, file);
                }
                else if (fileName.equalsIgnoreCase("Pixelmon-Install.zip")) {
                    this.descriptionToFile.put(FileDescription.PIXELMONINSTALLZIP, file);
                }
                
                continue;
            }
            
            try {
                System.out.println("Downloading " + url.toString() + "...");
                URLDownload.download(url, file);
                
                if (fileName.equalsIgnoreCase("minecraft-launcher.jar")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTLAUNCHER, file);
                }
                else if (fileName.equalsIgnoreCase("minecraftforge-installer.jar")) {
                    this.descriptionToFile.put(FileDescription.MINECRAFTFORGEINSTALLER, file);
                }
                else if (fileName.toLowerCase().contains("CustomNPCs_1.5.2.zip")) {
                    this.descriptionToFile.put(FileDescription.CUSTOMNPCSZIP, file);
                }
                else if (fileName.equalsIgnoreCase("Pixelmon-Install.zip")) {
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
        if (!runIfMcJarExists) {
            if (mcGameJar.exists()) {
                System.out.println("No need to run minecraft launcher; it has already been run successfully!");
                return;
            }
        }
        
        File minecraftLauncher = descriptionToFile.get(FileDescription.MINECRAFTLAUNCHER);
        ProcessBuilder proc = null;
        
        if (detach) {
            try {
                if (Utils.isNix()) {
                    proc = new ProcessBuilder("nohup", "java", "-jar", minecraftLauncher.getAbsolutePath(), " &");
                }
                else {
                    proc = new ProcessBuilder("java", "-jar", minecraftLauncher.getAbsolutePath());
                }
                proc.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            proc = new ProcessBuilder("java", "-jar", minecraftLauncher.getAbsolutePath());
            proc.inheritIO();
            try {
                proc.start();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * runs the Minecraft Forge installer jar
     */
    public void patchMinecraftJar() {
        System.out.println("Starting Forge installer...");
        
        ProcessBuilder proc = new ProcessBuilder("java", "-jar", 
                       descriptionToFile.get(FileDescription.MINECRAFTFORGEINSTALLER).getAbsolutePath());
        proc.inheritIO();
        try {
            proc.start();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Call this AFTER you patch with patchMinecraftJar()
     */
    public void addModsAndTextures() {
        File mcRootDir;
        File modsDir;
        File coremodsDir;
        File texturePackDir;
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
        texturePackDir = new File(mcRootDir, "texturepacks");
        
        if (!mcRootDir.exists()) {
            System.err.println("The folder " + mcRootDir.getAbsolutePath() + " doesn't exist.");
            return;
        }
        Utils.mkdir(modsDir); Utils.mkdir(coremodsDir);
        
        for (FileDescription desc : this.descriptionToFile.keySet()) {
            File jarOrZip = this.descriptionToFile.get(desc);
            
            if (desc.shouldExtractToRootMCDir()) {
                try {
                    Utils.unzip(jarOrZip, mcRootDir);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (desc.isMCForgeMod()) {
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
            else if (desc.getDesc().equals("CustomTexturePack")) {
                try {
                    Files.copy(jarOrZip.toPath(), new File(texturePackDir, jarOrZip.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private String parseVersion() {
        Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream(this.configDir + "version.txt"));
        
        while (s.hasNext()) {
            String potential = s.nextLine();
            if (potential.startsWith("version")) {
                s.close();
                return potential.split("=")[1].trim();
            }
            else {
                continue;
            }
        }
        s.close();
        System.err.println("Could not find the minecraft version in " + this.configDir + "version.txt");
        return null;
    }
    
    private Map<URL, File> populateURLs() {
        Map<URL, File> map = new HashMap<URL, File>();
        
        Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream(this.configDir + "downloads.txt"));
        
        while (s.hasNext()) {
            String line = s.nextLine();
            
            //ignore "#" comment lines
            if (line.startsWith("#") || line.equalsIgnoreCase("") || line == null) continue;
            
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
            
            if (fileName.equalsIgnoreCase("minecraft-launcher.jar")) {
                pop.put(FileDescription.MINECRAFTLAUNCHER, file);
            }
            else if (fileName.equalsIgnoreCase("minecraftforge-installer.jar")) {
                pop.put(FileDescription.MINECRAFTFORGEINSTALLER, file);
            }
            else if (fileName.toLowerCase().contains("CustomNPCs_1.5.2.zip")) {
                pop.put(FileDescription.CUSTOMNPCSZIP, file);
            }
            else if (fileName.equalsIgnoreCase("Pixelmon-Install.zip")) {
                pop.put(FileDescription.PIXELMONINSTALLZIP, file);
            }
        }
        
        return pop;
    }
    
    /**
     * True if a .minecraft folder is found and it is a post 1.6.2 minecraft directory
     * with a "versions" folder. Post 1.6.2 installations don't have this folder.
     * @return
     */
    public boolean currentMcGameDirExists() {
        String baseDir;
        if (Utils.isWindows()) {
            baseDir = System.getenv("APPDATA");
        }
        else {
            baseDir = System.getenv("HOME");
        }
        
        File mcGameDir = new File(baseDir, ".minecraft");
        File versions = new File(mcGameDir, "versions");
        
        return mcGameDir.exists() && versions.exists();
        
    }

    public Map<URL, File> getUrlToFile() {
        return urlToFile;
    }
    
    public File getMcRootDir() {
        String baseDir;
        if (Utils.isWindows()) {
            baseDir = System.getenv("APPDATA");
        }
        else {
            baseDir = System.getenv("HOME");
        }
        
        return new File(baseDir, ".minecraft");
    }
    
    public File getMcGameJar() {
        return this.mcGameJar;
    }
}
