import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signUpButton; 

    public LoginPage() {
        // Frame settings
        setTitle("Share-Note");
        setSize(300, 250); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new GridLayout(4, 2)); // 

        // Components
        JLabel sharenote = new JLabel("Sharenote", SwingConstants.CENTER);
        sharenote.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");
        signUpButton = new JButton("Sign Up"); // Initialize Sign Up button

        // Add components to frame
        add(sharenote); // Add title label
        add(new JLabel()); // Empty space
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(signUpButton); // Add Sign Up button

        // Login button action
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validateLogin();
            }
        });

        // Sign Up button action
        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openSignUpDialog(); // Open Sign Up dialog
            }
        });

        setVisible(true);
    }

    private void validateLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM users WHERE username=? AND password=?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                new StudyMaterialApp().setVisible(true); // Open Study Material Page
                dispose(); // Close Login Page
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSignUpDialog() {
        // Create Sign Up dialog
        JDialog signUpDialog = new JDialog(this, "Sign Up", true);
        signUpDialog.setSize(300, 200);
        signUpDialog.setLocationRelativeTo(this);
        signUpDialog.setLayout(new GridLayout(3, 2));

        // Sign Up components
        JLabel newUsernameLabel = new JLabel("New Username:");
        JTextField newUsernameField = new JTextField();
        JLabel newPasswordLabel = new JLabel("New Password:");
        JPasswordField newPasswordField = new JPasswordField();
        JButton signUpConfirmButton = new JButton("Sign Up");

        // Add components to dialog
        signUpDialog.add(newUsernameLabel);
        signUpDialog.add(newUsernameField);
        signUpDialog.add(newPasswordLabel);
        signUpDialog.add(newPasswordField);
        signUpDialog.add(new JLabel()); // Empty space
        signUpDialog.add(signUpConfirmButton);

        // Sign Up confirmation button action
        signUpConfirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newUsername = newUsernameField.getText();
                String newPassword = new String(newPasswordField.getPassword());
                if (newUsername.isEmpty() || newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(signUpDialog, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    saveNewUser(newUsername, newPassword);
                    signUpDialog.dispose(); // Close dialog after saving
                }
            }
        });

        signUpDialog.setVisible(true);
    }

    private void saveNewUser(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password) VALUES (?, ?)")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Sign Up Successful!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: Could not sign up", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginPage();
    }
}
