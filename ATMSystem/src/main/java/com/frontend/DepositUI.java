package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DepositUI extends JFrame {
    private JLabel l1;
    private JTextField tf1;
    private JButton b1, b2;
    private String accountNumber;

    public DepositUI(String accountNumber) {
        this.accountNumber = accountNumber;
        setTitle("Deposit");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("ENTER THE AMOUNT TO DEPOSIT");
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

        l1.setBounds(150, 150, 800, 40);
        add(l1);

        tf1.setBounds(150, 250, 520, 60);
        add(tf1);

        b1.setBounds(350, 450, 150, 50);
        add(b1);

        b2.setBounds(600, 450, 150, 50);
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
                processDeposit();
            }
        });
    }

    private void processDeposit() {
        String amountStr = tf1.getText();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                         JOptionPane.showMessageDialog(null, "Deposit Successful! Amount: " + amount);
                navigateToTransactions();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid amount");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Amount field cannot be empty");
        }
    }

    private void navigateToTransactions() {
        new TransactionsUI(accountNumber).setVisible(true);  // Quay về màn hình giao dịch
        dispose();
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);
    }
}
