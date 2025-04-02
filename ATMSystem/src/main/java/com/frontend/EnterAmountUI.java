package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EnterAmountUI extends JFrame {
    private JLabel l1, l2;
    private JTextField tf1;
    private JButton b1, b2;
    private String accountNumber;
    private String authToken;

    public EnterAmountUI(String accountNumber, String authToken) {
        this.accountNumber = accountNumber;
        this.authToken = authToken;
        setTitle("ATM - Enter Amount");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("ENTER THE AMOUNT TO WITHDRAW");
        l1.setFont(new Font("Osward", Font.BOLD, 32));

        tf1 = new JTextField(15);
        tf1.setFont(new Font("Arial", Font.BOLD, 24));

        l2 = new JLabel("Enter multiples of 50.000");
        l2.setFont(new Font("Arial", Font.ITALIC, 18));

        b1 = new JButton("Exit");
        b1.setFont(new Font("Arial", Font.BOLD, 24));

        b2 = new JButton("Withdraw");
        b2.setFont(new Font("Arial", Font.BOLD, 24));
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(125, 150, 800, 40);
        add(l1);

        tf1.setBounds(175, 250, 520, 60);
        add(tf1);

        l2.setBounds(250, 320, 200, 30);
        add(l2);

        b1.setBounds(350, 450, 150, 50);
        add(b1);

        b2.setBounds(600, 450, 150, 50);
        add(b2);
    }

    private void addActionListeners() {
        b1.addActionListener(ae -> {
            new CashWithdrawlUI(accountNumber, authToken).setVisible(true);
            dispose();
        });

        b2.addActionListener(ae -> {
            String inputAmount = tf1.getText().trim();
            try {
                double amount = Double.parseDouble(inputAmount);
                if (amount > 0 && amount % 50000 == 0) {
                    new TransactionConfirmationUI(accountNumber, amount, authToken).setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Amount must be a positive number and a multiple of 50.000!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input! Please enter a valid number.");
            }
        });
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);
    }
}