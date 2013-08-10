package us.pixelmon.installer.gui;

import us.pixelmon.installer.Installer;

public class InstallerGui implements Runnable {
    public Installer installer;
    public InstallerGui(Installer installer) {
        this.installer = installer;
    }
    public void run() {
        InstallerFrame frame = new InstallerFrame("Pixelmon.us Installer", installer);
        frame.setVisible(true);
        frame.begin();
    }
}
