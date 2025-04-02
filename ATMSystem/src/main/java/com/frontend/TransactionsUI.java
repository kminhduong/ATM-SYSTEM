package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TransactionsUI extends JFrame {

    private JLabel l1;
    private JButton b1, b2, b3, b4, b5, b6;
    private String accountNumber;
    private String authToken;

    public TransactionsUI(String accountNumber,String authToken) {
        this.accountNumber = accountNumber;
        this.authToken = authToken;
        initializeUI();
        addEventListeners();
    }

    private void initializeUI() {
        l1 = new JLabel("Please Select Your Transaction");
        l1.setForeground(Color.BLACK);
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(150, 100, 700, 50);

        b1 = new JButton("Cash Withdrawal");
        b2 = new JButton("View Balance");
        b3 = new JButton("Transfer");
        b4 = new JButton("Deposit");
        b5 = new JButton("Pin Change");
        b6 = new JButton("Log Out");

        setLayout(null);
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

        add(l1);

        setSize(850, 800);
        setLocation(250, 0);
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void addEventListeners() {
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new CashWithdrawlUI(accountNumber,authToken).setVisible(true);
                dispose();
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new ViewBalanceUI(accountNumber, authToken).setVisible(true);
                dispose();
            }
        });

//        b3.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent ae) {
//                new TransferUI(accountNumber).setVisible(true);  // Assuming TransferUI is implemented
//                dispose();
//            }
//        });

        b4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new DepositUI(accountNumber,authToken).setVisible(true);
                dispose();
            }
        });

        b5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new PinChangeUI(accountNumber,authToken).setVisible(true);
                dispose();
            }
        });

        b6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new LoginUI().setVisible(true);  // Log out and go back to login screen
                dispose();
            }
        });
    }

}
