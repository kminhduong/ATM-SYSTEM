package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CashWithdrawUI extends JFrame {

    private JLabel l1;
    private JButton b1, b2, b3, b4, b5, b6, b7;
    private String accountNumber;
    private String authToken;

    public CashWithdrawUI(String accountNumber, String authToken) {
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

        l1.setBounds(75, 50, 700, 50);
        add(l1);

        addButton(b1, 50, 175);
        addButton(b2, 50, 275);
        addButton(b3, 50, 375);
        addButton(b4, 400, 175);
        addButton(b5, 400, 275);
        addButton(b6, 400, 375);
        addButton(b7, 400, 475);
    }

    private void addButton(JButton button, int x, int y) {
        button.setBounds(x, y, 250, 50);
        add(button);
    }

    private void addActionListeners() {
        b1.addActionListener(createWithdrawListener(500000));
        b2.addActionListener(createWithdrawListener(1000000));
        b3.addActionListener(createWithdrawListener(2000000));
        b4.addActionListener(createWithdrawListener(3000000));
        b5.addActionListener(createWithdrawListener(5000000));

        b6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new EnterAmountUI(accountNumber,authToken).setVisible(true);  // Pass account number to EnterAmountUI
                dispose();
            }
        });

        b7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new TransactionsUI(accountNumber,authToken).setVisible(true);  // Return to main transaction screen
                dispose();
            }
        });
    }

    private ActionListener createWithdrawListener(double amount) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new TransactionConfirmationUI(accountNumber, amount,authToken).setVisible(true);
                dispose();
            }
        };
    }

    private void configureFrame() {
        setSize(700, 600);
        setLocation(250, 0);
        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

}
