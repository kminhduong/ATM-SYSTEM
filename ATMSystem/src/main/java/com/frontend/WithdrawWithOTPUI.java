package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WithdrawWithOTPUI extends JFrame {
    private JLabel l1, l2, l3, l4;
    private JButton b1, b2, b3;
    private JTextField tf1, tf2, tf3;

    public WithdrawWithOTPUI() {
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("Withdraw Money with OTP");
        l1.setFont(new Font("Osward", Font.BOLD, 32));

        l2 = new JLabel("Account Number:");
        l2.setFont(new Font("Raleway", Font.BOLD, 22));

        tf1 = new JTextField(15);
        tf1.setFont(new Font("Arial", Font.BOLD, 24));

        l3 = new JLabel("Amount:");
        l3.setFont(new Font("Raleway", Font.BOLD, 22));

        tf2 = new JTextField(15);
        tf2.setFont(new Font("Arial", Font.BOLD, 24));

        l4 = new JLabel(""); // Message will appear here
        l4.setFont(new Font("Raleway", Font.BOLD, 18));
        l4.setForeground(Color.RED);

        tf3 = new JTextField(6); // Field for OTP
        tf3.setFont(new Font("Arial", Font.BOLD, 24));
        tf3.setVisible(false); // Hidden initially

        b1 = new JButton("Exit");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.WHITE);
        b1.setFont(new Font("Arial", Font.BOLD, 24));

        b2 = new JButton("Send OTP");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.WHITE);
        b2.setFont(new Font("Arial", Font.BOLD, 24));

        b3 = new JButton("Submit OTP");
        b3.setBackground(Color.BLACK);
        b3.setForeground(Color.WHITE);
        b3.setFont(new Font("Arial", Font.BOLD, 24));
        b3.setVisible(false); // Hidden initially
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(50, 30, 800, 40);
        add(l1);

        l2.setBounds(100, 120, 300, 30);
        add(l2);

        tf1.setBounds(375, 120, 230, 40);
        add(tf1);

        l3.setBounds(100, 200, 300, 30);
        add(l3);

        tf2.setBounds(375, 200, 230, 40);
        add(tf2);

        l4.setBounds(100, 250, 500, 30);
        add(l4);

        tf3.setBounds(375, 250, 230, 40);
        add(tf3);

        b1.setBounds(150, 320, 150, 50);
        add(b1);

        b2.setBounds(350, 320, 150, 50);
        add(b2);

        b3.setBounds(550, 320, 150, 50);
        add(b3);
    }

    private void addActionListeners() {
        // Exit button
        b1.addActionListener(e -> System.exit(0));

        // Send OTP button
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String accountNumber = tf1.getText();
                String amount = tf2.getText();

                if (accountNumber.isEmpty() || amount.isEmpty()) {
                    l4.setText("Please fill in all fields.");
                    return;
                }

                try {
                    double withdrawAmount = Double.parseDouble(amount);
                    if (withdrawAmount <= 0) {
                        l4.setText("Amount must be positive.");
                        return;
                    }

                    // Call API to send OTP
                    boolean otpSent = sendOTP(accountNumber);
                    if (otpSent) {
                        JOptionPane.showMessageDialog(null, "OTP has been sent to your registered phone number.", "OTP Sent", JOptionPane.INFORMATION_MESSAGE);

                        // Show OTP field and Submit button
                        tf3.setVisible(true);
                        b3.setVisible(true);
                        l4.setText("Enter the OTP sent to your phone.");
                    } else {
                        l4.setText("Failed to send OTP. Please try again.");
                    }
                } catch (NumberFormatException ex) {
                    l4.setText("Invalid amount. Please enter a number.");
                }
            }
        });

        // Submit OTP button
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String enteredOTP = tf3.getText();

                if (enteredOTP.isEmpty()) {
                    l4.setText("Please enter the OTP.");
                    return;
                }

                boolean isOtpValid = validateOTP(enteredOTP);
                if (isOtpValid) {
                    JOptionPane.showMessageDialog(null, "Withdrawal successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Reset form
                    tf1.setText("");
                    tf2.setText("");
                    tf3.setText("");
                    tf3.setVisible(false);
                    b3.setVisible(false);
                    l4.setText("");
                } else {
                    l4.setText("Invalid OTP. Please try again.");
                }
            }
        });
    }

    private boolean sendOTP(String accountNumber) {
        try {
            // URL of the API
            String apiUrl = "http://localhost:8080/api/transactions/send-otp";

            // Create HttpURLConnection
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // JSON payload
            String jsonPayload = String.format("{\"accountNumber\": \"%s\"}", accountNumber);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check response code
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateOTP(String otp) {
        // Replace with actual API call to validate OTP
        return otp.equals("123456"); // Placeholder validation
    }

    private void configureFrame() {
        setSize(800, 500);
        setLocation(250, 100);
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new WithdrawWithOTPUI();
    }
}
