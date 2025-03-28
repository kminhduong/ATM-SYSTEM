package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DepositUI extends JFrame {
    JLabel l1;
    public JTextField tf1;
    public JButton b1, b2;
    public DepositUI() {
        l1 = new JLabel("ENTER THE AMOUNT TO DEPOSIT");
        l1.setFont(new Font("Osward", Font.BOLD, 28));
        l1.setBounds(150, 150, 800, 40);
        add(l1);
        tf1 = new JTextField(15);
        tf1.setBounds(150, 250, 520, 60);
        tf1.setFont(new Font("Arial", Font.BOLD, 24));
        add(tf1);
        b1 = new JButton("Exit");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);

        b2 = new JButton("Deposit");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        setLayout(null);

        b1.setFont(new Font("Arial", Font.BOLD, 24));
        b1.setBounds(350, 450, 200, 60);
        add(b1);

        b2.setFont(new Font("Arial", Font.BOLD, 24));
        b2.setBounds(600, 450, 200, 60);
        add(b2);
        getContentPane().setBackground(Color.WHITE);

        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new TransactionsUI().setVisible(true);
                dispose();
            }
        });
    }
    public static void main(String[] args) {
        new DepositUI();

    }
}
