package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TransactionsUI extends JFrame {

    private JLabel l1;
    private JButton b1, b2, b3, b4, b5, b6;
    private String accountNumber;  // Variable to store account number

    // Constructor to accept accountNumber from LoginUI
    public TransactionsUI(String accountNumber) {
        this.accountNumber = accountNumber;

        // Main UI
        l1 = new JLabel("Please Select Your Transaction");
        l1.setForeground(Color.BLACK);

        // Create buttons for different transactions
        b1 = new JButton("Cash Withdrawal");
        b2 = new JButton("View Balance");
        b3 = new JButton("Transfer");
        b4 = new JButton("Deposit");
        b5 = new JButton("Pin Change");
        b6 = new JButton("Log Out");

        // Set position and font for components
        setLayout(null);
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(150, 100, 700, 50);
        add(l1);

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

        // Event listeners for buttons
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new CashWithdrawlUI(accountNumber).setVisible(true);  // Pass accountNumber
                dispose();
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new ViewBalanceUI(accountNumber).setVisible(true);  // Pass accountNumber
                dispose();
            }
        });

//        b3.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent ae) {
//                new TransferUI(accountNumber).setVisible(true);  // Pass accountNumber
//                dispose();
//            }
//        });

        b4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new DepositUI(accountNumber).setVisible(true);  // Pass accountNumber
                dispose();
            }
        });

        b5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new PinChangeUI(accountNumber).setVisible(true);  // Pass accountNumber
                dispose();
            }
        });

        b6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new LoginUI().setVisible(true);  // Log out and go back to login screen
                dispose();
            }
        });

        // Set JFrame properties
        setSize(850, 800);
        setLocation(250, 0);
        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

//    public static void main(String[] args) {
//        new TransactionsUI().setVisible(true);  // Example account number
//    }
}
