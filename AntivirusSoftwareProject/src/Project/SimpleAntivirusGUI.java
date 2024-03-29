package Project;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class SimpleAntivirusGUI extends JFrame {
    private JPanel resultPanel;
    private JTextField directoryTextField;

    private List<String> virusSignatures;

    public SimpleAntivirusGUI() {
        setTitle("Simple Antivirus");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultPanel);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        directoryTextField = new JTextField();
        directoryTextField.setPreferredSize(new Dimension(200, 25)); // Set preferred size
        JButton selectDirectoryButton = new JButton("Select Directory");
        JButton scanButton = new JButton("Scan Directory");

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(new JLabel("Directory Path:"));
        inputPanel.add(directoryTextField);
        inputPanel.add(selectDirectoryButton);
        inputPanel.add(scanButton);

        getContentPane().add(inputPanel, BorderLayout.SOUTH);

        // Add image above the title
        ImageIcon antivirusIcon = new ImageIcon("C:/Users/Nate/eclipse-workspace/COSC480/anti.PNG");
        JLabel imageLabel = new JLabel(antivirusIcon, JLabel.CENTER);

        JLabel titleLabel = new JLabel("The one and only Antivirus Scanner");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Add description above the input box and submit button
        JLabel descriptionLabel = new JLabel("Enter the directory path and click 'Scan Directory' to check for viruses.");
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Adjusted the layout to include a new panel for the image and title
        JPanel imageTitlePanel = new JPanel();
        imageTitlePanel.setLayout(new BoxLayout(imageTitlePanel, BoxLayout.Y_AXIS));
        imageTitlePanel.add(imageLabel);
        imageTitlePanel.add(titleLabel);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(imageTitlePanel, BorderLayout.NORTH);
        titlePanel.add(descriptionLabel, BorderLayout.SOUTH);

        getContentPane().add(titlePanel, BorderLayout.NORTH);

        // Load virus signatures from file
        loadVirusSignatures();

        selectDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDirectory();
            }
        });

        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String directoryPath = directoryTextField.getText();
                try {
                    scanDirectory(directoryPath);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void selectDirectory() {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Select Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showDialog(this, "Select");
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            directoryTextField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void loadVirusSignatures() {
        virusSignatures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("virus_signatures.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                virusSignatures.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void scanDirectory(String directoryPath) throws BadLocationException {
        resultPanel.removeAll(); // Clear previous results
        resultPanel.revalidate();
        resultPanel.repaint();

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            resultPanel.add(new JLabel("Invalid directory path."));
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        if (containsVirusSignature(file)) {
                            JLabel virusLabel = new JLabel("<html>Virus found in file: <font color='red'>" + file.getAbsolutePath() + "</font></html>");
                            JButton deleteButton = new JButton("Delete");
                            deleteButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    deleteFile(file);
                                    virusLabel.setText("<html>File deleted: <font color='red'>" + file.getAbsolutePath() + "</font></html>");
                                }
                            });

                            resultPanel.add(virusLabel);
                            resultPanel.add(deleteButton);
                        } else {
                            resultPanel.add(new JLabel("File is clean: " + file.getAbsolutePath()));
                        }
                    } catch (IOException | NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        resultPanel.revalidate();
        resultPanel.repaint();
    }

    private boolean containsVirusSignature(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192]; // 8 KB buffer

        try (var fileInputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }

        byte[] fileHashBytes = md.digest();
        StringBuilder result = new StringBuilder();
        for (byte b : fileHashBytes) {
            result.append(String.format("%02x", b));
        }

        // Add console logging
        System.out.println("Calculated Hash: " + result.toString());

        for (String signature : virusSignatures) {
            System.out.println("Expected Virus Signature: " + signature);
            if (result.toString().equals(signature)) {
                return true;
            }
        }

        return false;
    }

    private void deleteFile(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleAntivirusGUI antivirusGUI = new SimpleAntivirusGUI();
            antivirusGUI.setVisible(true);
        });
    }
}