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

    private JLabel l1, l2, amountLabel, l3, balanceLabel;
    private JButton exitButton, confirmButton;
    private String accountNumber;
    private double amountToWithdraw;
    private String authToken;

    public TransactionConfirmationUI(String accountNumber, double amountToWithdraw, String authToken) {
        this.accountNumber = accountNumber;
        this.amountToWithdraw = amountToWithdraw;
        this.authToken = authToken;

        setTitle("ATM - Transaction Confirmation");
        setSize(850, 800);
        setLocation(250, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        initializeComponents();
        addComponentsToFrame();
        addActionListeners();

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

    private void initializeComponents() {
        l1 = new JLabel("TRANSACTION CONFIRMATION");
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(150, 50, 600, 30);

        l2 = new JLabel("Amount to Withdraw:");
        l2.setFont(new Font("Arial", Font.PLAIN, 20));
        l2.setBounds(250, 200, 200, 30);

        amountLabel = new JLabel(String.format("%.2f VND", amountToWithdraw));
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        amountLabel.setBounds(450, 200, 200, 30);

        l3 = new JLabel("Remaining Balance:");
        l3.setFont(new Font("Arial", Font.PLAIN, 20));
        l3.setBounds(250, 300, 200, 30);

        balanceLabel = new JLabel("Loading...");
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        balanceLabel.setBounds(450, 300, 200, 30);

        confirmButton = new JButton("Confirm");
        confirmButton.setBounds(650, 400, 150, 50);

        exitButton = new JButton("Exit");
        exitButton.setBounds(500, 400, 150, 50);
    }

    private void addComponentsToFrame() {
        add(l1);
        add(l2);
        add(amountLabel);
        add(l3);
        add(balanceLabel);
        add(confirmButton);
        add(exitButton);
    }

    private void addActionListeners() {
        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processTransaction();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new TransactionsUI(accountNumber,authToken).setVisible(true);
                dispose();
            }
        });
    }

    private void processTransaction() {
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
