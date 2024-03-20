import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class DownloadFolderOrganizer {
    private WatchService watchService;
    private Path downloadPath;
    private boolean isMonitoring;

    public void startMonitoring(String downloadFolderPath) throws IOException {
        // Define the download folder
        downloadPath = Paths.get(downloadFolderPath);

        // Create subfolders if they don't exist
        createSubfolders(downloadFolderPath);

        // Move existing files to appropriate subfolders
        moveExistingFiles(downloadFolderPath);

        // Set up a watch service to monitor the download folder
        watchService = FileSystems.getDefault().newWatchService();
        downloadPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

        // Start monitoring in a new thread
        isMonitoring = true;
        Thread monitorThread = new Thread(this::monitorFolder);
        monitorThread.start();
    }

    public void stopMonitoring() {
        isMonitoring = false;
    }

    private void monitorFolder() {
        // Start an infinite loop to monitor the folder
        while (isMonitoring) {
            WatchKey key;
            try {
                key = watchService.take(); // Wait for events
            } catch (InterruptedException ex) {
                return;
            }

            // Process all events in the key
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Handle only create events
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    // Get the file that was added
                    File newFile = downloadPath.resolve(filename).toFile();

                    // Move the file to appropriate subfolder based on its type
                    moveFileToSubfolder(newFile, downloadPath.toString());
                }
            }

            // Reset the key to receive further events
            boolean valid = key.reset();
            if (!valid) {
                break; // Exit the loop if the key is invalid
            }
        }
    }

    // Create subfolders if they don't exist
    private static void createSubfolders(String downloadFolderPath) {
        String[] subfolders = {"Images", "Videos", "Documents", "Setups", "RARFiles", "Folders"};
        for (String folder : subfolders) {
            File directory = new File(downloadFolderPath + File.separator + folder);
            if (!directory.exists()) {
                directory.mkdir();
            }
        }
    }

    // Move existing files to appropriate subfolders
    private static void moveExistingFiles(String downloadFolderPath) {
        File downloadFolder = new File(downloadFolderPath);
        File[] existingFiles = downloadFolder.listFiles();
        if (existingFiles != null) {
            for (File file : existingFiles) {
                if (file.isFile()) {
                    moveFileToSubfolder(file, downloadFolderPath);
                }
            }
        }
    }

    // Move file to appropriate subfolder based on its type
    private static void moveFileToSubfolder(File file, String downloadFolderPath) {
        String fileName = file.getName().toLowerCase();
        File destinationFolder;

        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) {
            destinationFolder = new File(downloadFolderPath + File.separator + "Images");
        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mkv") || fileName.endsWith(".mov") || fileName.endsWith(".wmv")) {
            destinationFolder = new File(downloadFolderPath + File.separator + "Videos");
        } else if (fileName.endsWith(".pdf") || fileName.endsWith(".doc") || fileName.endsWith(".txt") ||
                fileName.endsWith(".docx") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx") ||
                fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            destinationFolder = new File(downloadFolderPath + File.separator + "Documents");
        } else if (fileName.endsWith(".exe") || fileName.endsWith(".msi")) {
            destinationFolder = new File(downloadFolderPath + File.separator + "Setups");
        } else if (fileName.endsWith(".rar") || fileName.endsWith(".zip")) {
            destinationFolder = new File(downloadFolderPath + File.separator + "RARFiles");
        } else if (file.isDirectory()) {
            destinationFolder = new File(downloadFolderPath + File.separator + "Folders");
        } else {
            return; // Unsupported file type
        }

        // Create destination folder if it doesn't exist
        if (!destinationFolder.exists()) {
            destinationFolder.mkdir();
        }

        // Check if a file with the same name exists in the destination folder
        File newFile = new File(destinationFolder, file.getName());
        if (newFile.exists()) {
            // Append a unique identifier to the file name
            String baseName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            int count = 1;
            while (newFile.exists()) {
                newFile = new File(destinationFolder, baseName + "_" + count + "." + extension);
                count++;
            }
        }

        // Move the file to the destination folder
        if (file.renameTo(newFile)) {
            System.out.println("File moved successfully to " + destinationFolder.getAbsolutePath());
        } else {
            System.out.println("Failed to move the file to " + destinationFolder.getAbsolutePath());
        }
    }

    // Move file to a specified directory
    private static void moveFile(File file, String destinationFolder) {
        File destinationDirectory = new File(destinationFolder);
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdir();
        }
        File newFile = new File(destinationFolder + File.separator + file.getName());
        if (file.renameTo(newFile)) {
            System.out.println("File moved successfully to " + destinationFolder);
        } else {
            System.out.println("Failed to move the file to " + destinationFolder);
        }
    }
}
