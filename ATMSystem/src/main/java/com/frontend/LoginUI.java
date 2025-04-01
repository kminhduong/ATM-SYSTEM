package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JFrame {
    private JLabel l1, l2, l3;
    private JTextField tf1;
    private JPasswordField pf2;
    private JButton b1, b2;

    public LoginUI() {
        setTitle("AUTOMATED TELLER MACHINE");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("WELCOME TO ATM");
        l1.setFont(new Font("Osward", Font.BOLD, 38));

        l2 = new JLabel("Account Number:");
        l2.setFont(new Font("Raleway", Font.BOLD, 22));

        tf1 = new JTextField(15);
        tf1.setFont(new Font("Arial", Font.BOLD, 14));

        l3 = new JLabel("PIN:");
        l3.setFont(new Font("Raleway", Font.BOLD, 22));

        pf2 = new JPasswordField(15);
        pf2.setFont(new Font("Arial", Font.BOLD, 14));

        b1 = new JButton("Sign In");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);
        b1.setFont(new Font("Arial", Font.BOLD, 20));

        b2 = new JButton("Clear");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        b2.setFont(new Font("Arial", Font.BOLD, 20));
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(250, 50, 450, 40);
        add(l1);

        l2.setBounds(50, 175, 300, 30);
        add(l2);

        tf1.setBounds(300, 175, 230, 40);
        add(tf1);

        l3.setBounds(50, 270, 300, 30);
        add(l3);

        pf2.setBounds(300, 270, 230, 40);
        add(pf2);

        b1.setBounds(100, 370, 150, 50);
        add(b1);

        b2.setBounds(300, 370, 150, 50);
        add(b2);
    }

    private void addActionListeners() {
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                handleLogin();
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                clearFields();
            }
        });
    }

    private void handleLogin() {
        String accountNumber = tf1.getText();
        String pin = new String(pf2.getPassword());

        if (accountNumber.equals("123456") && pin.equals("1234")) {
            JOptionPane.showMessageDialog(null, "Login Successful!");
            new TransactionsUI(accountNumber).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid Account Number or PIN");
        }
    }

    private void clearFields() {
        tf1.setText("");
        pf2.setText("");
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}
