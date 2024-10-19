import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StudyMaterialApp extends JFrame {
    private JTextField titleField;
    private JTextField subjectField; // New field for the subject
    private JTextArea contentArea;
    private JTextField searchField; // Search bar field
    private JTable materialsTable;
    private DefaultTableModel tableModel;
    private String uploadedFilePath; // Variable to store the uploaded PDF path
    private JLabel fileNameLabel; // Label to display selected file name
    private CardLayout cardLayout; // For switching between panels
    private JPanel mainPanel; // Main panel for the application

    public StudyMaterialApp() {
        // Frame settings
        setTitle("Study Material Sharing App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800); // Set a larger window size
        setLocationRelativeTo(null); // Center the window

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout); // Use CardLayout for switching screens
        mainPanel.add(createHomeScreen(), "Home"); // Add home screen
        mainPanel.add(createMaterialScreen(), "Materials"); // Add materials screen

        add(mainPanel);
        setVisible(true);
    }

    // Method to create the home screen
    private JPanel createHomeScreen() {
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("ShareNotes", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 34));
        homePanel.add(titleLabel, BorderLayout.CENTER);

        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.PLAIN, 20));
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "Materials"); // Switch to the materials screen
            }
        });
        homePanel.add(startButton, BorderLayout.SOUTH);

        return homePanel;
    }

    // Method to create the materials screen
    private JPanel createMaterialScreen() {
        JPanel materialPanel = new JPanel();
        materialPanel.setLayout(new BorderLayout());

        // Create input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(7, 2)); // Adjust grid for the new subject field

        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Subject:")); // New label for subject
        subjectField = new JTextField(); // Field for subject input
        inputPanel.add(subjectField);

        inputPanel.add(new JLabel("Content:"));
        contentArea = new JTextArea(3, 20);
        inputPanel.add(new JScrollPane(contentArea));

        JButton uploadButton = new JButton("Upload PDF");
        inputPanel.add(uploadButton);
        fileNameLabel = new JLabel("No file selected");
        inputPanel.add(fileNameLabel);

        JButton addButton = new JButton("Add Material");
        inputPanel.add(addButton);

        JButton deleteButton = new JButton("Delete Material"); // Delete button
        inputPanel.add(deleteButton);

        JButton showMaterialsButton = new JButton("Show Materials"); // Show Materials button
        inputPanel.add(showMaterialsButton); // Add to input panel

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchPanel.add(new JLabel("Search by Subject:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchButton, BorderLayout.EAST);

        materialPanel.add(searchPanel, BorderLayout.NORTH); // Add the search panel at the top

        // Create table to display materials
        String[] columnNames = {"ID", "Title", "Subject", "Content", "Date", "PDF Path", "Download"};
        tableModel = new DefaultTableModel(columnNames, 0);
        materialsTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };

        materialsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = materialsTable.rowAtPoint(e.getPoint());
                int col = materialsTable.columnAtPoint(e.getPoint());
                if (col == 6) { // Check if download button is clicked
                    String pdfPath = (String) tableModel.getValueAt(row, 5); // Get PDF path
                    downloadPDF(pdfPath);
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(materialsTable);

        // Add components to the materials panel
        materialPanel.add(inputPanel, BorderLayout.CENTER);
        materialPanel.add(tableScrollPane, BorderLayout.SOUTH);

        // Button actions
        uploadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadPDF();
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addMaterial();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedMaterial();
            }
        });

        showMaterialsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showMaterials(); // Refresh the materials table when button is clicked
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchMaterialsBySubject(); // Perform search when button is clicked
            }
        });

        return materialPanel;
    }

    // Method to upload a PDF file
    private void uploadPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            uploadedFilePath = selectedFile.getAbsolutePath();
            fileNameLabel.setText(selectedFile.getName()); // Display the file name
        }
    }

    // Method to fetch and display data from the database
    public void showMaterials() {
        Connection connection = DatabaseConnection.getConnection();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM materials");

            // Clear the table before displaying new data
            tableModel.setRowCount(0);

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String subject = rs.getString("subject"); // Get subject from database
                String content = rs.getString("content");
                String date = rs.getString("date");
                String pdfPath = rs.getString("pdf_path"); // Get PDF path
                tableModel.addRow(new Object[]{id, title, subject, content, date, pdfPath, "Download"});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(connection); // Ensure connection is closed
        }
    }

    // Method to add new material to the database
    public void addMaterial() {
        String title = titleField.getText();
        String subject = subjectField.getText(); // Get subject
        String content = contentArea.getText();

        if (title.isEmpty() || subject.isEmpty() || content.isEmpty() || uploadedFilePath == null) {
            JOptionPane.showMessageDialog(this, "Title, Subject, Content, and PDF cannot be empty!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection connection = DatabaseConnection.getConnection();
        try {
            Statement stmt = connection.createStatement();
            String query = String.format("INSERT INTO materials (title, subject, content, date, pdf_path) VALUES ('%s', '%s', '%s', CURDATE(), '%s')",
                    title, subject, content, uploadedFilePath);
            stmt.executeUpdate(query);
            JOptionPane.showMessageDialog(this, "Material added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            titleField.setText("");
            subjectField.setText(""); // Reset subject field
            contentArea.setText("");
            uploadedFilePath = null; // Reset the uploaded file path
            fileNameLabel.setText("No file selected"); // Reset file name label
            showMaterials(); // Refresh the table
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(connection); // Ensure connection is closed
        }
    }

    // Method to delete selected material from the database
    public void deleteSelectedMaterial() {
        int selectedRow = materialsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a material to delete!", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int materialId = (int) tableModel.getValueAt(selectedRow, 0); // Get the ID of the selected material

        Connection connection = DatabaseConnection.getConnection();
        try {
            Statement stmt = connection.createStatement();
            String query = String.format("DELETE FROM materials WHERE id = %d", materialId);
            stmt.executeUpdate(query);
            JOptionPane.showMessageDialog(this, "Material deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            showMaterials(); // Refresh the table after deletion
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(connection); // Ensure connection is closed
        }
    }

    // Method to search materials by subject
    public void searchMaterialsBySubject() {
        String searchSubject = searchField.getText(); // Get search text
        Connection connection = DatabaseConnection.getConnection();
        try {
            Statement stmt = connection.createStatement();
            String query = String.format("SELECT * FROM materials WHERE subject LIKE '%%%s%%'", searchSubject);
            ResultSet rs = stmt.executeQuery(query);

            // Clear the table before displaying new search results
            tableModel.setRowCount(0);

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String subject = rs.getString("subject");
                String content = rs.getString("content");
                String date = rs.getString("date");
                String pdfPath = rs.getString("pdf_path");
                tableModel.addRow(new Object[]{id, title, subject, content, date, pdfPath, "Download"});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection(connection); // Ensure connection is closed
        }
    }

    // Method to download the selected PDF file
    public void downloadPDF(String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);
            FileInputStream inputStream = new FileInputStream(pdfFile);
            FileOutputStream outputStream = new FileOutputStream("downloaded_" + pdfFile.getName());

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            JOptionPane.showMessageDialog(this, "PDF downloaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error downloading PDF!", "Download Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new StudyMaterialApp(); // Create the application
            }
        });
    }
}
