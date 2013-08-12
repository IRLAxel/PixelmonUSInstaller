package us.pixelmon.installer;

import java.io.File;

import javax.swing.*;

import us.pixelmon.installer.gui.InstallerGui;
import us.pixelmon.installer.util.Utils;

public class Main {
    public static final File installerDataRoot =    new File("./.installerdata");
    public static final File downloadDir =          new File(installerDataRoot, "./downloads");
    
    public static void main(String args[]) {
        //To be safe, donwload ipv4
        System.setProperty("java.net.preferIPv4Stack" , "true");

        //make it prettier
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("There was an error with the look and feel. Resorting to default.");
        }
        
        Utils.mkdir(installerDataRoot); Utils.mkdir(downloadDir);
        Installer installer = new Installer(downloadDir);

        SwingUtilities.invokeLater(new InstallerGui(installer));
    }
}