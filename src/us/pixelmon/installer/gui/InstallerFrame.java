package us.pixelmon.installer.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import us.pixelmon.installer.Installer;
import us.pixelmon.installer.util.Utils;

public class InstallerFrame extends JFrame {
    private static final long serialVersionUID = 1390402159643961362L;
    
    private Installer installer;
    private GridLayout layout;
    public InstallerFrame(String title, Installer installer) {
        super(title);
        this.installer = installer;
        this.layout = new GridLayout(3, 1, 5, 5);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(layout);
        setSize(600, 500);
    }

    public void begin() {
        startWelcome();
    }

    /*
     * Draws the welcome screen
     */
    public void startWelcome() {
        JLabel mainText = null;
        JButton launchMc = new JButton("<html>Launch Minecraft");
        final JButton cont = new JButton("Continue");
        cont.setEnabled(false);
        launchMc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                installer.runMinecraft(true, true);
                System.exit(0);
            }
        });
        
        if (installer.currentMcGameDirExists()) {
            launchMc.setEnabled(true);
            mainText = new JLabel("<html><h1><b>Welcome!</b></h1>You already have a .minecraft directory. If " +
                                  "would like to run minecraft from here, click the \"Launch Minecraft\" " +
                                  "button below.<br /><br />If you would like to patch your current " +
                                  ".minecraft directory, click \"Continue\".");
        }
        else {
            SwingWorker<Void, Void> dialog = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    if (installer.getMcRootDir().exists()) {
                        String message = "<html>It looks like you have a corrupted or a pre-1.6.2 minecraft installation" +
                                         " and .minecraft folder. We will now delete it.";
                        int response = JOptionPane.showConfirmDialog(null, message, "Confirm Deletion", 
                                                                     JOptionPane.YES_NO_OPTION, 
                                                                     JOptionPane.QUESTION_MESSAGE);
                        try {
                            if (response == 0) {
                                Utils.deleteRecursive(installer.getMcRootDir());
                            }
                            else if (response == 1) {
                                JOptionPane.showMessageDialog(null, "<html>The installer will now exit");
                                System.exit(1);
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    return null;
                }

                @Override
                protected void done() {
                    cont.setEnabled(true);
                }
                
                
            };
            dialog.execute();
            
            launchMc.setEnabled(false);
            mainText = new JLabel("<html><h1><b>Welcome!</b></h1>This installer will automatically download " +
                                   "all of the resources necessary for Pixelmon.us! It will also automatically patch " +
                                   "your minecraft.jar. When this installer is done, you will be ready to log on " +
                                   "to Pixelmon.us!</h3>");
        }
        cont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDownload();
            }
        });

        //this panel is the last row in the frame
        JPanel third = new JPanel();
        third.setLayout(new GridLayout(1, 3));
        //1                         //2                     //3
        third.add(launchMc); third.add(new JPanel()); third.add(cont);
        
        //populate the grid; a little confusing to read
        getContentPane().add(mainText);
        getContentPane().add(new JPanel());
        getContentPane().add(third);
    }

    /*
     * Handles downloading the files
     */
    public void startDownload() {
        clearAll();
        final JLabel statusText = new JLabel("<html><center><b>Downloading Files... This may take" +
                                             " a while...");

        final JButton cont = new JButton("Continue");
        cont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initMinecraft(false);
            }
        });

        final JProgressBar progress = new JProgressBar(1, 100);
        progress.setIndeterminate(true);
        progress.setStringPainted(true);
        progress.setString("Downloading...");

        getContentPane().add(statusText);
        getContentPane().add(progress);
        update();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                installer.downloadFiles();
                return null;
            }

            @Override
            protected void done() {
                statusText.setText("<html><h1 \"style=text-align:center\"><b>Done Downloading!");
                progress.setIndeterminate(false);
                progress.setString("Done!");
                progress.setValue(100);

                JPanel third = new JPanel(new GridLayout(1, 3));
                third.add(new JPanel()); third.add(new JPanel()); third.add(cont);
                getContentPane().add(third);
            }
        };
        worker.execute();
    }

    /**
     * Starts minecraft in a worker thread depending on the patched status for different text
     * @param alreadyPatched
     */
    void initMinecraft(boolean alreadyPatched) {
        clearAll();

        final JLabel infoText;
        final JButton proceed = new JButton("<html>Proceed/Run Minecraft");
        if (alreadyPatched) {
            infoText = new JLabel("<html>Minecraft will now start. It should now be fully modded. Check " +
                                  "for the \"mods\" option once you get to the main screen after logging in." +
                                  " If you see it and it shows Pixelmon and CustomNPCs, we are done!");
            
            proceed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    proceed.setEnabled(false);
                    SwingWorker<Void, Void> runMc = new SwingWorker<Void, Void>() {
                        
                        @Override
                        protected Void doInBackground() throws Exception {
                            installer.runMinecraft(true, false);

                            return null;
                        }

                        @Override
                        protected void done() {
                            startCompleted();
                        }
                    };
                    runMc.execute();
                }
            });
        }
        else {
            infoText = new JLabel("<html>Minecraft will now run once unmodded. This will let minecraft " +
                                  "download all necessary files before patching the jar with MinecraftForge. " +
                                  "<br /><br />Login and then click \"Play\". Once you get to the main Minecraft" +
                                  " logged-in game screen, exit from minecraft and we will proceed.");
            
            proceed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    proceed.setEnabled(false);
                    SwingWorker<Void, Void> runMc = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            installer.runMinecraft(false);

                            return null;
                        }

                        @Override
                        protected void done() {
                            startPatching();
                        }
                    };
                    runMc.execute();
                }
            });
        }
        getContentPane().add(infoText);
        getContentPane().add(new JPanel());

        JPanel third = new JPanel(new GridLayout(1, 3));
        third.add(new JPanel()); third.add(new JPanel()); third.add(proceed);

        getContentPane().add(third);
        update();
    }

    /*
     * Handles patching the new jar
     */
    public void startPatching() {
        if (!installer.getMcGameJar().exists()) {
            SwingWorker<Void, Void> notification = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    JOptionPane.showMessageDialog(null, "The last step did not complete correctly. Please try again");
                    return null;
                }

                @Override
                protected void done() {
                    initMinecraft(false);
                }
                
                
            };
            notification.execute();
        }
        
        clearAll();
        final JLabel info = new JLabel("<html><h1>Patching the jar...");
        final JProgressBar progress = new JProgressBar(1, 100);
        progress.setIndeterminate(true);
        progress.setStringPainted(true);
        progress.setString("Patching Minecraft");

        getContentPane().add(info);
        getContentPane().add(progress);
        update();

        SwingWorker<Void, Void> patchWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                installer.patchMinecraftJar();
                installer.addModsAndTextures();
                initMinecraft(true);

                return null;
            }
        };
        patchWorker.execute();
    }

    public void startCompleted() {
        clearAll();
        JLabel finishedText = new JLabel("<html><h1><b>Installation Complete!</b></h1>Minecraft should now be fully " +
                                         "modded. Just start this program again and choose \"Launch Minecraft\" to play" +
                                         ". Login, choose \"Multiplayer\" and add our server at address \"pixelmon.us\".");
        JButton close = new JButton("Exit");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        JLabel second = new JLabel("<html>We also installed our Custom Texture Pack! You can enable it by going to " +
                                   "Options> Texture Packs and choosing \"Pixelmon_us.zip\"<br /><br />" +
                                   "Thank you for playing on our server!");
        
        JPanel third = new JPanel(new GridLayout());
        third.add(new JPanel()); third.add(new JPanel()); third.add(close);

        getContentPane().add(finishedText);
        getContentPane().add(second);
        getContentPane().add(third);
        update();
    }
    /*
     * Some utility methods
     */
    private void update() {
        revalidate();
        repaint();
    }
    private void clearAll() {
        getContentPane().removeAll();
        update();
    }
}
