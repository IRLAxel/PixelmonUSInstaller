package us.pixelmon.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class Installer {
	private Map<URL, File> urlToFile;
	private Map<Description, File> descriptionToFile;
	private String configDir; //path in the jar
	private File downloadDir;
	private File minecraftDirToMake;
	private File tmpDir;
	
	public Installer(File downloadDir, File minecraftDirToMake, File tmpDir) {
		this.configDir = "config/";
		this.downloadDir = downloadDir;
		this.minecraftDirToMake = minecraftDirToMake;
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
	
	public void runMinecraftInitial() {
		Runtime r = Runtime.getRuntime();
		String originalPath = (new File(".").getAbsolutePath());
		
		//make the minecraft directory
		if (!this.minecraftDirToMake.exists()) {
			this.minecraftDirToMake.mkdir();
		}
		
		Path downloadedJar = (new File(this.downloadDir, "minecraft.jar")).toPath();
		Path newJar = (new File(this.minecraftDirToMake, "minecraft.jar")).toPath();
		
		try {
			Files.copy(downloadedJar, newJar, StandardCopyOption.REPLACE_EXISTING);
			
			String[] commands = {"cd " + this.minecraftDirToMake,
								 "java -jar minecraft.jar"};
			r.exec(commands);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void patchMinecraftJar() {
		try {
			ZipFile mcJar;
			String baseDir;
			if (Utils.isWindows()) {
				baseDir = System.getenv("APPDATA");
			}
			else {
				baseDir = System.getenv("HOME");
			}
			
			mcJar = new ZipFile(new File(baseDir, ".minecraft/bin/minecraft.jar"));
			
			ZipFile forgeZip = new ZipFile(this.descriptionToFile.get(Description.MINECRAFTFORGEJAR));
			
			
/*			String extractedPath = this.tmpDir.getAbsolutePath() + "/" + forgeZip.getFile().getAbsolutePath().replace(".zip", "");
			forgeZip.extractAll(extractedPath);
			
			ZipParameters p = new ZipParameters();
			p.setIncludeRootFolder(false);
			mcJar.addFolder(extractedPath, );*/
		}
		catch (ZipException e) {
			System.err.println("There was an error patching the minecraft jar, specifically with zip utils.");
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
