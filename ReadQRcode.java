import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class ReadQRcode {
    private static String name;
    private static String rollNumber;

    public static String readQRCode(String path, String charset, Map<EncodeHintType, ErrorCorrectionLevel> hintMap)
            throws FileNotFoundException, IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(path)))));
        Result result = new MultiFormatReader().decode(binaryBitmap);
        return result.getText();
    }

    public static void main(String[] args) {
        String path = "QRCode.png";
        String charset = "UTF-8";
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        try {
            String encodedContent = readQRCode(path, charset, hintMap);
            System.out.println("Data stored in the QR Code is:\n" + encodedContent);

            JFrame frame = new JFrame("QR Code Reader");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 400);
            frame.setLayout(new BorderLayout());

          
            JPanel qrCodePanel = new JPanel();
            BufferedImage image = ImageIO.read(new File(path));
            JLabel qrCodeLabel = new JLabel(new ImageIcon(image));
            qrCodePanel.add(qrCodeLabel);

         
            JPanel contentPanel = new JPanel(new GridBagLayout());
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;

           
            JLabel contentLabel = new JLabel("<html><body><div style='text-align: center;'>" + encodedContent
                    + "</div></body></html>");
            contentLabel.setFont(new Font("Arial", Font.BOLD, 16));
            contentLabel.setHorizontalAlignment(SwingConstants.CENTER);
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 2;
            contentPanel.add(contentLabel, constraints);

           
            JButton feedbackButton = new JButton("Feedback");
            feedbackButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String feedback = JOptionPane.showInputDialog(frame, "Please enter your feedback (maximum 150 words):");
                    System.out.println("Feedback: " + feedback);
                    storeFeedbackInDatabase(feedback, encodedContent); // Store the feedback in the database
                }
            });
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            constraints.ipadx = 10;
            constraints.insets.set(10, 0, 0, 5);
            contentPanel.add(feedbackButton, constraints);

            // Take Assignment button
            JButton takeAssignmentButton = new JButton("Take Assignment");
            takeAssignmentButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // Create a new window for entering name and roll number
                    JFrame assignmentFrame = new JFrame("Assignment Details");
                    assignmentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    assignmentFrame.setSize(400, 200);
                    assignmentFrame.setLayout(new BorderLayout());

                    JPanel assignmentPanel = new JPanel(new GridBagLayout());
                    assignmentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    GridBagConstraints assignmentConstraints = new GridBagConstraints();
                    assignmentConstraints.fill = GridBagConstraints.HORIZONTAL;

                    JLabel nameLabel = new JLabel("Name:");
                    assignmentConstraints.gridx = 0;
                    assignmentConstraints.gridy = 0;
                    assignmentConstraints.anchor = GridBagConstraints.WEST;
                    assignmentPanel.add(nameLabel, assignmentConstraints);

                    JTextField nameTextField = new JTextField(20);
                    assignmentConstraints.gridx = 1;
                    assignmentConstraints.gridy = 0;
                    assignmentPanel.add(nameTextField, assignmentConstraints);

                    JLabel rollNumberLabel = new JLabel("Roll Number:");
                    assignmentConstraints.gridx = 0;
                    assignmentConstraints.gridy = 1;
                    assignmentPanel.add(rollNumberLabel, assignmentConstraints);

                    JTextField rollNumberTextField = new JTextField(20);
                    assignmentConstraints.gridx = 1;
                    assignmentConstraints.gridy = 1;
                    assignmentPanel.add(rollNumberTextField, assignmentConstraints);

                    JButton submitButton = new JButton("Submit");
                    submitButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            name = nameTextField.getText();
                            rollNumber = rollNumberTextField.getText();
                            System.out.println("Name: " + name);
                            System.out.println("Roll Number: " + rollNumber);

                            if (isRollNumberExists(rollNumber, encodedContent)) {
                                JOptionPane.showMessageDialog(frame, "Roll Number already exists! Please enter a different Roll Number.");
                            } else {
                                assignmentFrame.dispose();
                                updateAssignmentInDatabase(encodedContent); // Update the assignment in the database
                            }
                        }
                    });
                    assignmentConstraints.gridx = 0;
                    assignmentConstraints.gridy = 2;
                    assignmentConstraints.gridwidth = 2;
                    assignmentPanel.add(submitButton, assignmentConstraints);

                    assignmentFrame.add(assignmentPanel, BorderLayout.CENTER);
                    assignmentFrame.setVisible(true);
                }
            });
            constraints.gridx = 1;
            constraints.gridy = 1;
            constraints.gridwidth = 1;
            constraints.ipadx = 10;
            constraints.insets.set(10, 5, 0, 0);
            contentPanel.add(takeAssignmentButton, constraints);

            frame.add(qrCodePanel, BorderLayout.CENTER);
            frame.add(contentPanel, BorderLayout.SOUTH);

            frame.setVisible(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

   
    private static void storeFeedbackInDatabase(String feedback, String dbName) {
        // Modify the connection URL, username, and password according to your MySQL database configuration
        String url ="jdbc:mysql://localhost:3306/" + dbName;
        String username = "root";
        String password = "";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String query = "INSERT INTO feedback (feedback) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, feedback);

            statement.executeUpdate();
            System.out.println("Feedback stored in the database.");
        } catch (SQLException e) {
       
            System.err.println("Error storing feedback in the database: " + e.getMessage());
        }
    }

    
    private static void updateAssignmentInDatabase(String dbName) {
        
        String url = "jdbc:mysql://localhost:3306/" + dbName;
        String username = "root";
        String password = "";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String query = "INSERT INTO attendance (Name, Roll_number) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, rollNumber);

            statement.executeUpdate();
            System.out.println("Assignment updated in the database.");
        } catch (SQLException e) {
            
            System.err.println("Error updating assignment in the database: " + e.getMessage());
        }
    }

   
    private static boolean isRollNumberExists(String rollNumber, String dbName) {
       
        String url = "jdbc:mysql://localhost:3306/" + dbName;
        String username = "root";
        String password = "";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String query = "SELECT * FROM attendance WHERE Roll_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, rollNumber);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); 
        } catch (SQLException e) {
            
            System.err.println("Error checking roll number in the database: " + e.getMessage());
            return false;
        }
    }
}
