package com.frontend;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TransactionConfirmationUI extends JFrame {

    JLabel l1, l2, l3, amountLabel, balanceLabel;
    JButton exitButton, confirmButton;
    private String accountNumber;
    private double amountToWithdraw;

    public TransactionConfirmationUI(String accountNumber, double amountToWithdraw) {
        this.accountNumber = accountNumber;
        this.amountToWithdraw = amountToWithdraw;

        setTitle("ATM - Transaction Confirmation");
        setSize(850, 800);
        setLocation(250, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        l1 = new JLabel("TRANSACTION CONFIRMATION");
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(150, 50, 600, 30);
        add(l1);

        l2 = new JLabel("Amount to Withdraw:");
        l2.setFont(new Font("Arial", Font.PLAIN, 20));
        l2.setBounds(250, 200, 200, 30);
        add(l2);

        amountLabel = new JLabel(String.format("%.2f VND", amountToWithdraw));
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        amountLabel.setBounds(450, 200, 200, 30);
        add(amountLabel);

//        l3 = new JLabel("Remaining Balance:");
//        l3.setFont(new Font("Arial", Font.PLAIN, 20));
//        l3.setBounds(250, 300, 200, 30);
//        add(l3);
//
//        balanceLabel = new JLabel("Loading...");
//        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 20));
//        balanceLabel.setBounds(450, 300, 200, 30);
//        add(balanceLabel);

        confirmButton = new JButton("Confirm");
        confirmButton.setBounds(650, 400, 150, 50);
        add(confirmButton);

        exitButton = new JButton("Exit");
        exitButton.setBounds(500, 400, 150, 50);
        add(exitButton);

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);

        confirmButton.addActionListener(e -> processTransaction());

        exitButton.addActionListener(e -> {
            new TransactionsUI(accountNumber).setVisible(true);
            dispose();
        });
    }

    public void processTransaction() {
        try {
            URL url = new URL("http://localhost:8080/api/transactions/withdraw?accountNumber=" + accountNumber + "&amount=" + amountToWithdraw);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            conn.disconnect();

            JSONObject transactionData = new JSONObject(response);
            double remainingBalance = transactionData.getDouble("remainingBalance");

            balanceLabel.setText(String.format("%.2f VND", remainingBalance));

            JOptionPane.showMessageDialog(this, "Transaction Successful!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing transaction!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
