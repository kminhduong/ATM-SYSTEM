package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginUI extends JFrame {
    JLabel l1, l2, l3;
    public JTextField tf1;
    public JPasswordField pf2;
    public JButton b1, b2, b3;

    public LoginUI() {
        setTitle("AUTOMATED TELLER MACHINE");

        // Welcome text
        l1 = new JLabel("WELCOME TO ATM");
        l1.setFont(new Font("Osward", Font.BOLD, 38));
        l1.setBounds(250, 50, 450, 40);
        add(l1);

        // Account Number label and text field
        l2 = new JLabel("Account Number:");
        l2.setFont(new Font("Raleway", Font.BOLD, 22));
        l2.setBounds(50, 175, 300, 30);
        add(l2);

        tf1 = new JTextField(15);
        tf1.setBounds(300, 175, 230, 40);
        tf1.setFont(new Font("Arial", Font.BOLD, 14));
        add(tf1);

        // PIN label and password field
        l3 = new JLabel("PIN:");
        l3.setFont(new Font("Raleway", Font.BOLD, 22));
        l3.setBounds(50, 270, 300, 30);
        add(l3);

        pf2 = new JPasswordField(15);
        pf2.setFont(new Font("Arial", Font.BOLD, 14));
        pf2.setBounds(300, 270, 230, 40);
        add(pf2);

        // Buttons (SIGN IN, CLEAR, SIGN UP)
        b1 = new JButton("Sign In");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);

        b2 = new JButton("Clear");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);

        b3 = new JButton("Sign Up");
        b3.setBackground(Color.BLACK);
        b3.setForeground(Color.BLACK);

        setLayout(null);

        b1.setFont(new Font("Arial", Font.BOLD, 20));
        b1.setBounds(100, 370, 150, 50);
        add(b1);

        b2.setFont(new Font("Arial", Font.BOLD, 20));
        b2.setBounds(300, 370, 150, 50);
        add(b2);

        b3.setFont(new Font("Arial", Font.BOLD, 20));
        b3.setBounds(200, 450, 150, 50);
        add(b3);

        getContentPane().setBackground(Color.WHITE);

        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);

        // Action listeners for buttons
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String accountNumber = tf1.getText();
                String pin = new String(pf2.getPassword());

                // This is where you'd check credentials with a backend
                if (accountNumber.equals("123456") && pin.equals("1234")) {
                    JOptionPane.showMessageDialog(null, "Login Successful!");
                    new TransactionsUI(accountNumber).setVisible(true);  // Pass accountNumber to TransactionsUI
                    dispose();  // Close login screen
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Account Number or PIN");
                }
            }
        });

        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tf1.setText("");
                pf2.setText("");
            }
        });

        b3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // Open Sign Up screen
                new SignUpUI().setVisible(true);
                dispose();
            }
        });
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}
