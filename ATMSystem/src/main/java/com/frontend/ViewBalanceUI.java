package com.frontend;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class ViewBalanceUI extends JFrame {
    JLabel l1, l2, l3, l4, accountNumberLabel, fullNameLabel, balanceLabel;
    JButton exitButton;
    private String accountNumber;

    public ViewBalanceUI(String accountNumber) {
        this.accountNumber = accountNumber;

        // Cài đặt JFrame
        setTitle("ATM - View Balance");
        setSize(600, 400);
        setLocation(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        l1 = new JLabel("VIEW BALANCE ACCOUNT");
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(200, 100, 600, 30);
        add(l1);

        // Account Number
        l2 = new JLabel("Account Number:");
        l2.setFont(new Font("Arial", Font.PLAIN, 20));
        l2.setBounds(250, 200, 200, 30);
        add(l2);

        accountNumberLabel = new JLabel(accountNumber);
        accountNumberLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        accountNumberLabel.setBounds(450, 200, 200, 30);
        add(accountNumberLabel);

        // Full Name
        l3 = new JLabel("Full Name:");
        l3.setFont(new Font("Arial", Font.PLAIN, 20));
        l3.setBounds(250, 300, 150, 30);
        add(l3);

        fullNameLabel = new JLabel("Loading...");
        fullNameLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        fullNameLabel.setBounds(450, 300, 200, 30);
        add(fullNameLabel);

        // Balance
        l4 = new JLabel("Current Balance:");
        l4.setFont(new Font("Arial", Font.PLAIN, 20));
        l4.setBounds(250, 400, 150, 30);
        add(l4);

        balanceLabel = new JLabel("Loading...");
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        balanceLabel.setBounds(450, 400, 200, 30);
        add(balanceLabel);

        // Exit Button
        exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 20));
        exitButton.setBounds(500, 500, 200, 50);
        add(exitButton);

        getContentPane().setBackground(Color.WHITE);
        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);

        // Button action
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new TransactionsUI(accountNumber).setVisible(true);
                dispose();
            }
        });

        // Gọi API để lấy thông tin tài khoản
        loadAccountDetails();
    }

    // Hàm lấy thông tin tài khoản từ backend
    public void loadAccountDetails() {
        try {
            // Sử dụng URL API bạn yêu cầu
            URL url = new URL("http://localhost:8080/accounts/" + accountNumber);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            StringBuilder response = new StringBuilder();
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            // Phân tích JSON
            JSONObject accountData = new JSONObject(response.toString());
            String accountHolderName = accountData.getString("accountHolderName");
            double balance = accountData.getDouble("balance");

            // Cập nhật UI
            fullNameLabel.setText(accountHolderName);
            balanceLabel.setText(String.format("$%.2f", balance));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading account information!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}
