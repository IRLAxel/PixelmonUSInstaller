package us.pixelmon.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;

public class Installer {
    private Map<URL, File> urlToFile;
    private Map<Description, File> descriptionToFile;
    private String configDir; //path in the jar
    private File downloadDir;
    private File tmpDir;
    
    public Installer(File downloadDir, File tmpDir) {
        this.configDir = "config/";
        this.downloadDir = downloadDir;
        this.tmpDir = tmpDir;
        
        urlToFile = populateURLs();
        descriptionToFile = new HashMap<Description, File>();
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
                    this.descriptionToFile.put(Description.MINECRAFTJAR, file);
                }
                else if (fileName.toLowerCase().contains("minecraftforge-universal")) {
                    this.descriptionToFile.put(Description.MINECRAFTFORGEJAR, file);
                }
                else if (fileName.toLowerCase().contains("customnpcs")) {
                    this.descriptionToFile.put(Description.CUSTOMNPCSZIP, file);
                }
                else if (fileName.toLowerCase().contains("pixelmon")) {
                    this.descriptionToFile.put(Description.PIXELMONINSTALLZIP, file);
                }
                
                continue;
            }
            
            try {
                System.out.println("Downloading \"" + url.toString() + "\"...");
                URLDownload.download(url, file);
                
                if (fileName.toLowerCase().contains("minecraft") &&
                    fileName.toLowerCase().contains(".jar") &&
                    !fileName.toLowerCase().contains("forge")) {
                    this.descriptionToFile.put(Description.MINECRAFTFORGEJAR, file);
                }
                else if (fileName.toLowerCase().contains("minecraftforge-universal")) {
                    this.descriptionToFile.put(Description.MINECRAFTFORGEJAR, file);
                }
                else if (fileName.toLowerCase().contains("customnpcs")) {
                    this.descriptionToFile.put(Description.CUSTOMNPCSZIP, file);
                }
                else if (fileName.toLowerCase().contains("pixelmon")) {
                    this.descriptionToFile.put(Description.PIXELMONINSTALLZIP, file);
                }
                
                System.out.println("Download successful!");
            } catch (IOException e) {
                System.err.println("There was an error downloading file \"" + url.toString() + "\"");
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
        String baseDir;
        if (Utils.isWindows()) {
            baseDir = System.getenv("APPDATA");
        }
        else {
            baseDir = System.getenv("HOME");
        }
        File mcGameJar = new File(baseDir, "minecraft.jar");
        if (!runIfMcJarExists ) {
            if (mcGameJar.exists()) {
                System.out.println("No need to run minecraft.jar launcher; it has already been run successfully!");
                return;
            }
        }
        
        File downloadedJar = descriptionToFile.get(Description.MINECRAFTJAR);
        Runtime r = Runtime.getRuntime();
        Process run = null;
        
        try {
            run = r.exec("java -jar " + downloadedJar.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Scanner runStd = new Scanner(run.getInputStream());
        Scanner runErr = new Scanner (run.getErrorStream());
        
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
            
            ZipFile mcJarOrig = new ZipFile(new File(baseDir, ".minecraft/bin/minecraft.jar"));
            ZipFile mcForgeJar = new ZipFile(descriptionToFile.get(Description.MINECRAFTFORGEJAR));
            
            //we will extract everything and then make a new jar.
            //unzip mcForgeJar second to overwrite the minecraft files necessary.
            mcJarOrig.extractAll(tmpDir.getAbsolutePath());
            Utils.deleteRecursive(new File(tmpDir, "META-INF"));
            mcForgeJar.extractAll(tmpDir.getAbsolutePath());
            
            File mcJarNewFile = new File(tmpDir, "minecraft.jar");
            if (mcJarNewFile.exists()) {
                mcJarNewFile.delete();
            }
            ZipFile mcJarNew = new ZipFile(mcJarNewFile);
            ZipParameters mcJarNewParam = new ZipParameters();
            mcJarNewParam.setIncludeRootFolder(false);
            mcJarNew.addFolder(tmpDir, mcJarNewParam);
            
            //move original minecraft jar to tmp and move the new to .minecraft
            Files.copy(mcJarOrig.getFile().toPath(), new File(tmpDir, "minecraft-orig-mojang.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(mcJarNew.getFile().toPath(), mcJarOrig.getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (ZipException e) {
            System.err.println("There was an error patching the minecraft jar, specifically with zip utils.");
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
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
}
