package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CashWithdrawlUI extends JFrame {
    private JLabel l1;
    private JButton b1, b2, b3, b4, b5, b6, b7;
    private String accountNumber;
    private String authToken;

    public CashWithdrawlUI(String accountNumber, String authToken) {
        this.accountNumber = accountNumber;
        this.authToken = authToken;
        setTitle("ATM - Cash Withdrawal");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("Please Select Withdrawal Amount");
        l1.setForeground(Color.BLACK);
        l1.setFont(new Font("Osward", Font.BOLD, 32));

        b1 = createButton("500.000");
        b2 = createButton("1.000.000");
        b3 = createButton("2.000.000");
        b4 = createButton("3.000.000");
        b5 = createButton("5.000.000");
        b6 = createButton("Enter Amount");
        b7 = createButton("Exit");
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        return button;
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(150, 50, 700, 50);
        add(l1);

        addButton(b1, 50, 250);
        addButton(b2, 50, 350);
        addButton(b3, 50, 450);
        addButton(b4, 500, 250);
        addButton(b5, 500, 350);
        addButton(b6, 500, 450);
        addButton(b7, 500, 550);
    }

    private void addButton(JButton button, int x, int y) {
        button.setBounds(x, y, 300, 50);
        add(button);
    }

    private void addActionListeners() {
        b1.addActionListener(createWithdrawListener(500000));
        b2.addActionListener(createWithdrawListener(1000000));
        b3.addActionListener(createWithdrawListener(2000000));
        b4.addActionListener(createWithdrawListener(3000000));
        b5.addActionListener(createWithdrawListener(5000000));

        b6.addActionListener(ae -> {
            new EnterAmountUI(accountNumber, authToken).setVisible(true);
            dispose();
        });

        b7.addActionListener(ae -> {
            new TransactionsUI(accountNumber, authToken).setVisible(true);
            dispose();
        });
    }

    private ActionListener createWithdrawListener(double amount) {
        return ae -> {
            new TransactionConfirmationUI(accountNumber, amount, authToken).setVisible(true);
            dispose();
        };
    }

    private void configureFrame() {
        setSize(850, 800);
        setLocation(250, 0);
        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }
}