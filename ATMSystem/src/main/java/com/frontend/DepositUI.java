package com.frontend;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class DepositUI extends JFrame {
    private JLabel l1;
    private JTextField tf1;
    private JButton b1, b2;
    private String accountNumber;
    private String authToken;

    public DepositUI(String accountNumber, String authToken) {
        this.accountNumber = accountNumber;
        this.authToken = authToken;
        setTitle("ATM - Deposit");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("Enter The Amount To Deposit");
        l1.setFont(new Font("Osward", Font.BOLD, 32));

        tf1 = new JTextField(15);
        tf1.setFont(new Font("Arial", Font.BOLD, 24));

        b1 = new JButton("Exit");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);
        b1.setFont(new Font("Arial", Font.BOLD, 24));

        b2 = new JButton("Deposit");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        b2.setFont(new Font("Arial", Font.BOLD, 24));
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(100, 50, 800, 40);
        add(l1);

        tf1.setBounds(150, 200, 400, 60);
        add(tf1);

        b1.setBounds(300, 350, 150, 50);
        add(b1);

        b2.setBounds(500, 350, 150, 50);
        add(b2);
    }

    private void addActionListeners() {
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                navigateToTransactions();
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                processDeposit(authToken);
            }
        });
    }

    private void processDeposit(String authToken) {
        String amountStr = tf1.getText();

        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(null, "Please enter an amount greater than 0", "Input Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    URL url = new URL("http://localhost:8080/api/transactions/deposit");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    // Cấu hình yêu cầu HTTP POST
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + authToken);
                    conn.setDoOutput(true);

                    // Tạo JSON body cho yêu cầu
                    String jsonBody = String.format("{\"amount\": %.2f}", amount);
                    System.out.println("Sending JSON Body: " + jsonBody);
                    System.out.println("Authorization Token: " + authToken);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonBody.getBytes("UTF-8"));
                        os.flush();
                    }

                    // Kiểm tra phản hồi từ server
                    int responseCode = conn.getResponseCode();
                    System.out.println("Response Code: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String output;

                        while ((output = br.readLine()) != null) {
                            response.append(output);
                        }
                        br.close();

                        // Parse phản hồi từ backend
                        JSONObject responseBody = new JSONObject(response.toString());
                        String message = responseBody.getString("message");
                        String balance = responseBody.getString("data");

                        // Định dạng số dư
                        double balanceValue = Double.parseDouble(balance);
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        String formattedBalance = df.format(balanceValue);

                        // Hiển thị thông báo thành công
                        JOptionPane.showMessageDialog(null, message + " New Balance: " + formattedBalance, "Success", JOptionPane.INFORMATION_MESSAGE);
                        navigateToTransactions(); // Điều hướng sau khi nạp tiền thành công
                    } else {
                        handleErrorResponse(conn);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error processing deposit: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid amount", "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Amount field cannot be empty", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleErrorResponse(HttpURLConnection conn) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String output;

            while ((output = br.readLine()) != null) {
                errorResponse.append(output);
            }
            br.close();

            // Parse thông báo lỗi từ backend
            JSONObject errorResponseBody = new JSONObject(errorResponse.toString());
            String errorMessage = errorResponseBody.getString("message");

            JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to read error response from server. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void navigateToTransactions() {
        new TransactionsUI(accountNumber,authToken).setVisible(true);  // Quay về màn hình giao dịch
        dispose();
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(700, 600);
        setLocation(250, 0);
        setVisible(true);
    }

}
