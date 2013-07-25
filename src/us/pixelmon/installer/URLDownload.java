package us.pixelmon.installer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class URLDownload {
    private URL url;
    public URLDownload(String url) {
        try {
            this.url = new URL(url);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    public void download(String pathToDownloadTo) throws IOException {
        URLDownload.download(this.url, new File(pathToDownloadTo));
    e
    
    /**
     * Use this to actually download the from a url
     * 
     * @param url
     * @param pathToDownloadTo
     * @throws IOException
     * @throws MalformedURLException
     */
    public static void download(String url, String pathToDownloadTo) throws IOException, MalformedURLException {
        URL u = new URL(url);
        download(u, new File(pathToDownloadTo));
    }
    
    /**
     * Use this to actually download the from a url
     * @param url
     * @param fileToDownloadTo
     * @throws IOException
     */
    public static void download(URL url, File fileToDownloadTo) throws IOException {
        if (!fileToDownloadTo.exists()) {
            fileToDownloadTo.createNewFile();
        }
        Files.copy(url.openStream(), fileToDownloadTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
