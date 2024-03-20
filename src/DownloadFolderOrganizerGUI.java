import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class DownloadFolderOrganizerGUI extends JFrame {
    private JTextField downloadPathField;
    private JButton browseButton;
    private JButton startButton;
    private JButton stopButton;
    private DownloadFolderOrganizer organizer;
    private TrayIcon trayIcon;
    private SystemTray systemTray;

    public DownloadFolderOrganizerGUI() {
        setTitle("Download Folder Organizer");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // Hide instead of exit on close
        setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel downloadPathLabel = new JLabel("Download Path:");
        downloadPathField = new JTextField(20);
        downloadPathField.setToolTipText("Add your download folder path here");
        browseButton = new JButton("Browse");
        browseButton.setPreferredSize(new Dimension(100, 30));
        topPanel.add(downloadPathLabel, BorderLayout.NORTH);
        topPanel.add(downloadPathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.EAST);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(100, 40));
        startButton.setBackground(new Color(64, 168, 152));
        startButton.setForeground(Color.WHITE);
        startButton.setBorderPainted(false);
        startButton.setFocusable(false);

        stopButton = new JButton("Stop");
        stopButton.setPreferredSize(new Dimension(100, 40));
        stopButton.setBackground(new Color(237, 85, 101));
        stopButton.setForeground(Color.WHITE);
        stopButton.setBorderPainted(false);
        stopButton.setFocusable(false);
        stopButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(buttonPanel, BorderLayout.CENTER);

        add(contentPanel);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileChooser();
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDownloadOrganizer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopDownloadOrganizer();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray();
            }
        });
    }

    private void openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            downloadPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void startDownloadOrganizer() {
        String downloadPath = downloadPathField.getText();
        if (downloadPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a download path.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        organizer = new DownloadFolderOrganizer();
        try {
            organizer.startMonitoring(downloadPath);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error starting Download Folder Organizer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopDownloadOrganizer() {
        if (organizer != null) {
            organizer.stopMonitoring();
        }

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void minimizeToTray() {
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit().getImage("icon.ico");

            trayIcon = new TrayIcon(image, "Download Folder Organizer");
            trayIcon.setImageAutoSize(true);

            // Create a popup menu for the tray icon
            PopupMenu popupMenu = new PopupMenu();
            MenuItem openItem = new MenuItem("Open");
            MenuItem exitItem = new MenuItem("Exit");

            openItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    setExtendedState(JFrame.NORMAL);
                    systemTray.remove(trayIcon);
                }
            });

            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exitApplication();
                }
            });

            popupMenu.add(openItem);
            popupMenu.add(exitItem);

            trayIcon.setPopupMenu(popupMenu);

            trayIcon.addMouseListener(new TrayIconMouseListener());
            try {
                systemTray.add(trayIcon);
                setVisible(false);
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        } else {
            // System tray is not supported
            System.out.println("System tray is not supported.");
        }
    }

    private void exitApplication() {
        if (organizer != null) {
            organizer.stopMonitoring();
        }
        System.exit(0);
    }

    private class TrayIconMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                systemTray.remove(trayIcon);
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                DownloadFolderOrganizerGUI gui = new DownloadFolderOrganizerGUI();
                gui.setVisible(true);
            }
        });
    }
}
