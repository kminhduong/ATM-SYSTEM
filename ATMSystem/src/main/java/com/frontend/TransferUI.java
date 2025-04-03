package com.frontend;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class TransferUI extends JFrame {
    private JLabel l1, l2, l3;
    private JButton b1, b2;
    private JTextField tf1,tf2;
    private String accountNumber;
    private String authToken;
    public TransferUI(String accountNumber,String authToken) {
        this.accountNumber = accountNumber;
        this.authToken = authToken;
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }
    private void initializeComponents() {
        l1 = new JLabel("Enter The Information To Transfer");
        l1.setFont(new Font("Osward", Font.BOLD, 32));

        l2 = new JLabel("Account Number:");
        l2.setFont(new Font("Raleway", Font.BOLD, 22));

        tf1 = new JTextField(15);
        tf1.setFont(new Font("Arial", Font.BOLD, 24));

        l3 = new JLabel("Amount:");
        l3.setFont(new Font("Raleway", Font.BOLD, 22));

        tf2 = new JTextField(15);
        tf2.setFont(new Font("Arial", Font.BOLD, 24));

        b1 = new JButton("Exit");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);
        b1.setFont(new Font("Arial", Font.BOLD, 24));

        b2 = new JButton("Transfer");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        b2.setFont(new Font("Arial", Font.BOLD, 24));
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(50, 50, 800, 40);
        add(l1);

        l2.setBounds(100, 175, 300, 30);
        add(l2);

        tf1.setBounds(375, 175, 230, 40);
        add(tf1);

        l3.setBounds(100, 270, 300, 30);
        add(l3);

        tf2.setBounds(375, 270, 230, 40);
        add(tf2);

        b1.setBounds(300, 350, 150, 50);
        add(b1);

        b2.setBounds(500, 350, 150, 50);
        add(b2);
    }
    private void configureFrame() {
        setSize(700, 600);
        setLocation(250, 0);
        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }
    private void addActionListeners() {
        // Exit button functionality
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new TransactionsUI(accountNumber,authToken).setVisible(true);
                dispose();
            }
        });

        // Transfer button functionality
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String receiverAccountNumber = tf1.getText();
                String amount = tf2.getText();

                // Simple validation
                if (receiverAccountNumber.isEmpty() || amount.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double transferAmount = Double.parseDouble(amount);
                    if (transferAmount <= 0) {
                        JOptionPane.showMessageDialog(null, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Call the API (replace with actual API call)
                    boolean success = callTransferAPI(receiverAccountNumber, transferAmount);
                    if (success) {
                        JOptionPane.showMessageDialog(null, "Transfer successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Transfer failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid amount. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    private boolean callTransferAPI(String targetAccountNumber, double amount) {
        try {
            String apiUrl = "http://localhost:8080/api/transactions/transfer";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setDoOutput(true);

            String jsonPayload = String.format("{\"targetAccountNumber\": \"%s\", \"amount\": %.2f}", targetAccountNumber, amount);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes("utf-8"));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }
                System.out.println("Success Response: " + response.toString());
                JSONObject responseBody = new JSONObject(response.toString());
                String message = responseBody.getString("message");
                String balance = responseBody.getString("data");

                // Định dạng số liệu
                double balanceValue = Double.parseDouble(balance);
                DecimalFormat df = new DecimalFormat("#,##0.00");
                String formattedBalance = df.format(balanceValue);

                JOptionPane.showMessageDialog(null, message + " New Balance: " + formattedBalance, "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                // Xử lý lỗi từ backend
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
                JSONObject errorBody = new JSONObject(errorResponse.toString());
                String errorMessage = errorBody.getString("message");

                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error processing transfer! Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
