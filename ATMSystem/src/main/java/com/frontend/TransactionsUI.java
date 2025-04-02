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
        l1.setBounds(100, 50, 700, 50);

        b1 = createButton("Cash Withdrawal");
        b2 = createButton("View Balance");
        b3 = createButton("Transfer");
        b4 = createButton("Deposit");
        b5 = createButton("Pin Change");
        b6 = createButton("Log Out");

        setLayout(null);
        addButton(b1, 50, 175);
        addButton(b2, 50, 275);
        addButton(b3, 50, 375);
        addButton(b4, 400, 175);
        addButton(b5, 400, 275);
        addButton(b6, 400, 375);

        add(l1);

        setSize(700, 600);
        setLocation(250, 0);
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    private void addButton(JButton button, int x, int y) {
        button.setBounds(x, y, 250, 50);
        add(button);
    }
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        return button;
    }
    private void addEventListeners() {
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new CashWithdrawUI(accountNumber,authToken).setVisible(true);
                dispose();
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new ViewBalanceUI(accountNumber, authToken).setVisible(true);
                dispose();
            }
        });

        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new TransferUI(accountNumber,authToken).setVisible(true);  // Assuming TransferUI is implemented
                dispose();
            }
        });

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
