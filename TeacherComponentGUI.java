import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class TeacherComponentGUI extends JFrame {
    private JComboBox<String> assignmentComboBox;
    private JButton btnEnterGrade;
    private JButton btnViewResults;
    private JButton btnViewReport; 
    private JButton btnGenerateQR; 
    private JButton btnFeedback;

    private Connection connection;
    private PreparedStatement statement;
    private String selectedAssignment;

    public TeacherComponentGUI() {
        setTitle("Teacher Component");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLayout(null);

        assignmentComboBox = new JComboBox<>(new String[]{"Assignment 1", "Assignment 2", "Assignment 3", "Assignment 4"});
        assignmentComboBox.setBounds(30, 30, 220, 25);
        getContentPane().add(assignmentComboBox);

        btnEnterGrade = new JButton("Enter Grade");
        btnEnterGrade.setBounds(30, 70, 125, 25);
        btnEnterGrade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showGradeDialog();
            }
        });
        getContentPane().add(btnEnterGrade);

        btnViewResults = new JButton("View Results");
        btnViewResults.setBounds(165, 70, 125, 25);
        btnViewResults.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewResults();
            }
        });
        getContentPane().add(btnViewResults);

        btnViewReport = new JButton("View Report"); 
        btnViewReport.setBounds(30, 110, 125, 25);
        btnViewReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showReportDialog();
            }
        });
        getContentPane().add(btnViewReport);

        btnGenerateQR = new JButton("Generate QR"); 
        btnGenerateQR.setBounds(165, 110, 125, 25);
        getContentPane().add(btnGenerateQR);

        assignmentComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedAssignment = (String) assignmentComboBox.getSelectedItem();
            }
        });
        btnFeedback = new JButton("Feedback");
        btnFeedback.setBounds(165, 150, 125, 25);
        btnFeedback.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showFeedback();
            }
        });
        getContentPane().add(btnFeedback);
        
        selectedAssignment = (String) assignmentComboBox.getSelectedItem();

        btnGenerateQR.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String str = selectedAssignment;
                    String path = "F:\\OneDrive - Amrita Vishwa Vidyapeetham\\sem2\\java\\QRcode\\QRCode.png";
                    String charset = "UTF-8";
                    Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                    GenerateQRCode.generateQRcode(str, path, charset, hashMap, 200, 200);
                } catch (WriterException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void showGradeDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2, 5, 5));

        JTextField txtStudentID = new JTextField();
        JTextField txtGrade = new JTextField();

        panel.add(new JLabel("Student ID:"));
        panel.add(txtStudentID);
        panel.add(new JLabel("Grade:"));
        panel.add(txtGrade);

        int result = JOptionPane.showConfirmDialog(null, panel, "Enter Student ID and Grade", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String studentID = txtStudentID.getText();
            String grade = txtGrade.getText();

            if (!studentID.isEmpty() && !grade.isEmpty()) {
                try {
                 
                    connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + selectedAssignment, "root", "");

                    
                    if (isStudentIDExists(studentID)) {
                       
                        String sql = "UPDATE grade SET grade = ? WHERE StudentId = ?";
                        statement = connection.prepareStatement(sql);
                        statement.setString(1, grade);
                        statement.setString(2, studentID);
                    } else {
                      
                        String sql = "INSERT INTO grade (StudentId, grade) VALUES (?, ?)";
                        statement = connection.prepareStatement(sql);
                        statement.setString(1, studentID);
                        statement.setString(2, grade);
                    }

         
                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Grade submitted successfully.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to submit grade.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                  
                    try {
                        if (statement != null)
                            statement.close();
                        if (connection != null)
                            connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please enter student ID and grade.");
            }
        }
    }

    private void showFeedback() {
        try {
           
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+selectedAssignment, "root", "");
    
          
            String sql = "SELECT * FROM feedback";
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
    
           
            StringBuilder feedbackBuilder = new StringBuilder();
    
        
            while (resultSet.next()) {
                String feedback = resultSet.getString("feedback");
                feedbackBuilder.append(feedback).append("\n");
            }
    
            if (feedbackBuilder.length() > 0) {
                JOptionPane.showMessageDialog(null, feedbackBuilder.toString(), "Feedback for " + selectedAssignment, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No feedback found for " + selectedAssignment);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            
            try {
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
     
    
    private void viewResults() {
        try {
            
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + selectedAssignment, "root", "");

           
            String sql = "SELECT * FROM grade";
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

          
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("Student ID");
            tableModel.addColumn("Grade");

            while (resultSet.next()) {
                String studentID = resultSet.getString("StudentId");
                String grade = resultSet.getString("grade");
                tableModel.addRow(new Object[]{studentID, grade});
            }

            if (tableModel.getRowCount() > 0) {
                JTable table = new JTable(tableModel);
                JScrollPane scrollPane = new JScrollPane(table);
                JOptionPane.showMessageDialog(null, scrollPane, "Results for " + selectedAssignment, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No grades found for " + selectedAssignment);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            
            try {
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showReportDialog() {
        String studentID = JOptionPane.showInputDialog(null, "Enter Student ID:");
        if (studentID != null && !studentID.isEmpty()) {
            try {
                
                DefaultTableModel tableModel = new DefaultTableModel();
                tableModel.addColumn("Assignment");
                tableModel.addColumn("Grade");

                int numFailures = 0; 

               
                for (int i = 1; i <= 4; i++) {
                    String assignmentDBName = "assignment " + i;

                 
                    connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + assignmentDBName, "root", "");

                    
                    String sql = "SELECT * FROM grade WHERE StudentId = ?";
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, studentID);
                    ResultSet resultSet = statement.executeQuery();

                  
                    String assignmentName = "Assignment " + i;

             
                    if (resultSet.next()) {
                        String grade = resultSet.getString("grade");
                        tableModel.addRow(new Object[]{assignmentName, grade});

                     
                        if (!grade.equals("A") && !grade.equals("B") && !grade.equals("C")) {
                            numFailures++;
                        }
                    } else {
                       
                        numFailures++;
                    }

                   
                    resultSet.close();
                    statement.close();
                    connection.close();
                }

                if (tableModel.getRowCount() > 0) {
                    JTable table = new JTable(tableModel);
                    JScrollPane scrollPane = new JScrollPane(table);
                    JOptionPane.showMessageDialog(null, scrollPane, "Report for Student ID: " + studentID, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "No grades found for Student ID: " + studentID);
                }

              
                if (numFailures > 0) {
                    JOptionPane.showMessageDialog(null, "Result: Fail");
                } else {
                    JOptionPane.showMessageDialog(null, "Result: Pass");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
             
                try {
                    if (statement != null)
                        statement.close();
                    if (connection != null)
                        connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private boolean isStudentIDExists(String studentID) throws SQLException {
        String sql = "SELECT * FROM grade WHERE StudentId = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, studentID);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TeacherComponentGUI().setVisible(true);
            }
        });
    }
}
