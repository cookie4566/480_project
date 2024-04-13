package Project;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.beans.*;
import java.util.Random;

public class SimpleAntivirusGUI extends JFrame impletments ActionListener,PropertyChangeListener{

    private JPanel resultPanel;
    private JTextField directoryTextField;
    private ProgressMonitor progressMonitor;
    private Task task;
    
    class Task extends SwingWorker<Void,Void>{
    	@Override
    	public Void doInBackground() {
    		Random random = new Random();
    		int progress = 0;
    		setProgress(0);
    		try {
    			Thread.sleep(1000);
    			while(progress < 100 && !isCancelled()) {
    				Thread.sleep(random.nextInt(1000));
    				progress += random.nextInt(10);
    				setProgress(Math.min(progress,100));
    			}
    		} catch (InteruptedException ignore) {}
    		return null;
    	}
    	@Override
    	public void done() {
    		Toolkit.getDefaultToolkit().beep();
    		startButton.setEnabled(true);
    		progressMonitor.setProgress(0);
    	}
    }
    
    private List<String> virusSignatures;
    private static Map<String, String> maliciousFiles = new HashMap<>();

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
        ImageIcon antivirusIcon = new ImageIcon("C:\Users\kenne\480_project\Untitled.jpeg");
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

        selectDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDirectory();
            }
        });
        // Added task method for scanner
        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                String directoryPath = directoryTextField.getText();
                try {
                    scanDirectory(directoryPath);
                    progressMonitor = new ProgressMonitor(ProgressMonitorGui.this,"Scanning :)", 0,100);
                    progressMonitor.setProgress(0);
                    task = new Task();
                    task.addPropertyChangeListener(this)
                    task.execute();
                    scanButton.setEnabled(false);
                } catch (IOException | NoSuchAlgorithmException | BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        })
        
        // method for progress bar
        public void propertyChange(PropertyChangeEvent evt) {
        	if ("progress" == evt.getPropertyName()) {
        		int progress = (Integer) evt.getNewValue();
        		progressMonitor.setProgress(progress);
        		String mgs = String.format("Progress %d%%.\n", progress);
        		progressMonitor.setNote(mgs);
        		taskOutput.append(mgs);
        		if(progressMonitor.isCanceled() || task.isDone()) {
        			Toolkit.getDefaultToolkit().beep();
        			if(progessMonitor.isCanceled()) {
        				task.cancel(true);
        				taskOutput.append("Scanning done.\n");
        			}else {
        				taskOutput.append("Scan done.\n");
        			}
        			scanButton.setEnabled(true);
        		}
        	}
        }
        }
        // Load virus signatures from file
        loadVirusSignatures();

        // Populate the malicious files database
        maliciousFiles.put("malicious_file.exe", "HASHVALUE1");
        maliciousFiles.put("another_malicious_file.dll", "HASHVALUE2");
    }
// Monitor gui
	public ProgressMonitorGui() {
		super(new Borderlayout());
		taskOutput = new JTextArea(5,30);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);
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

    private void scanDirectory(String directoryPath) throws IOException, NoSuchAlgorithmException, BadLocationException {
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
                        boolean isMalicious = isMalicious(file);
                        boolean containsVirusSignature = containsVirusSignature(file);
                        JLabel resultLabel = new JLabel("File: " + file.getName());
                        if (isMalicious) {
                            resultLabel.setText(resultLabel.getText() + " contains SHA-256 virus.");
                        } else {
                            resultLabel.setText(resultLabel.getText() + " does not contain SHA-256 virus.");
                        }
                        if (containsVirusSignature) {
                            resultLabel.setText(resultLabel.getText() + " Contains MD5 signature.");
                        } else {
                            resultLabel.setText(resultLabel.getText() + " Does not contain MD5 signature.");
                        }

                        resultPanel.add(resultLabel);
                    } catch (IOException | NoSuchAlgorithmException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        resultPanel.revalidate();
        resultPanel.repaint();
    }
    
    private void done() {
    	done = true;
    	Toolkit.getDefaultToolkit().beep();
    	startButton.setEnabled(true);
    	setCursor(null):
    	progressBar.setValue(progressBar.getMinimum());
    	taskOutput.append("Done!!\n")
    }

    private boolean isMalicious(File file) throws NoSuchAlgorithmException, IOException {
        String hash = calculateSHA256(file);
        return maliciousFiles.containsValue(hash);
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

        for (String signature : virusSignatures) {
            if (result.toString().equals(signature)) {
                return true;
            }
        }

        return false;
    }

    private String calculateSHA256(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(dataBytes)) != -1) {
            digest.update(dataBytes, 0, bytesRead);
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hashHex = new StringBuilder();
        for (byte hashByte : hashBytes) {
            hashHex.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return hashHex.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleAntivirusGUI antivirusGUI = new SimpleAntivirusGUI();
            antivirusGUI.setVisible(true);
        });
    }
}
