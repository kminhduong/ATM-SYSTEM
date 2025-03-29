package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CashWithdrawlUI extends JFrame {

    private JLabel l1;
    private JButton b1, b2, b3, b4, b5, b6, b7;

    private String accountNumber;  // Nhận accountNumber từ trang trước

    public CashWithdrawlUI(String accountNumber) {
        this.accountNumber = accountNumber;

        // Set title and layout
        setTitle("ATM - Cash Withdrawal");
        setLayout(null);

        // Add label
        l1 = new JLabel("Please Select Withdrawl Amount");
        l1.setForeground(Color.BLACK);
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(150, 50, 700, 50);
        add(l1);

        // Create buttons for fixed withdrawal amounts
        b1 = new JButton("500.000");
        b2 = new JButton("1.000.000");
        b3 = new JButton("2.000.000");
        b4 = new JButton("3.000.000");
        b5 = new JButton("5.000.000");
        b6 = new JButton("Enter Amount");  // Button for entering custom amount
        b7 = new JButton("Exit");

        // Set button fonts and positions
        b1.setFont(new Font("Arial", Font.BOLD, 20));
        b1.setBounds(50, 250, 300, 50);
        add(b1);

        b2.setFont(new Font("Arial", Font.BOLD, 20));
        b2.setBounds(50, 350, 300, 50);
        add(b2);

        b3.setFont(new Font("Arial", Font.BOLD, 20));
        b3.setBounds(50, 450, 300, 50);
        add(b3);

        b4.setFont(new Font("Arial", Font.BOLD, 20));
        b4.setBounds(500, 250, 300, 50);
        add(b4);

        b5.setFont(new Font("Arial", Font.BOLD, 20));
        b5.setBounds(500, 350, 300, 50);
        add(b5);

        b6.setFont(new Font("Arial", Font.BOLD, 20));
        b6.setBounds(500, 450, 300, 50);
        add(b6);

        b7.setFont(new Font("Arial", Font.BOLD, 20));
        b7.setBounds(500, 550, 300, 50);
        add(b7);

        // Button actions for fixed amounts
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                double amount = 500000;  // Set the amount to 500.000
                new TransactionConfirmationUI(accountNumber, amount).setVisible(true);
                dispose();
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                double amount = 1000000;  // Set the amount to 1.000.000
                new TransactionConfirmationUI(accountNumber, amount).setVisible(true);
                dispose();
            }
        });

        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                double amount = 2000000;  // Set the amount to 2.000.000
                new TransactionConfirmationUI(accountNumber, amount).setVisible(true);
                dispose();
            }
        });

        b4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                double amount = 3000000;  // Set the amount to 3.000.000
                new TransactionConfirmationUI(accountNumber, amount).setVisible(true);
                dispose();
            }
        });

        b5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                double amount = 5000000;
                new TransactionConfirmationUI(accountNumber, amount).setVisible(true);
                dispose();
            }
        });

        // Action for "Enter Amount" button
        b6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new EnterAmountUI(accountNumber).setVisible(true);  // Pass account number to EnterAmountUI
                dispose();
            }
        });

        // Action for "Exit" button
        b7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new TransactionsUI(accountNumber).setVisible(true);  // Return to main transaction screen
                dispose();
            }
        });

        // Set frame properties
        setSize(850, 800);
        setLocation(250, 0);
        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }


}
