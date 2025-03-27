package com.frontend;


import com.atm.model.Transaction;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

    public class TransactionsUI extends JFrame {

        private JLabel l1;
        private JButton b1, b2, b3, b4, b5, b6, b7;
        private Transaction backend;

        public TransactionsUI() {

            l1 = new JLabel("Please Select Your Transaction");
            l1.setForeground(Color.BLACK);
            l1.setFont(new Font("System", Font.BOLD, 16));

            b1 = new JButton("Cash Withdrawl");
            b2 = new JButton("View Balance");
            b3 = new JButton("Transfer");
            b4 = new JButton("Deposit");
            b5 = new JButton("Pin Change");
            b6 = new JButton("Bill Top Bar");
            b7 = new JButton("Exit");

            setLayout(null);
            l1.setFont(new Font("Osward", Font.BOLD, 38));
            l1.setBounds(150, 50, 700, 50);
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
            b7.setFont(new Font("Arial", Font.BOLD, 20));
            b7.setBounds(500, 550, 300, 50);
            add(b7);

            b1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    new CashWithdrawl_SignInUI().setVisible(true);
                    dispose();
                }
            });
//            b2.addActionListener(this);
//            b3.addActionListener(this);
//            b4.addActionListener(this);
//            b5.addActionListener(this);
//            b6.addActionListener(this);
            b7.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    new LoginUI().setVisible(true);
                    dispose();
                }
            });

            setSize(850, 800);
            setLocation(250, 0);
            getContentPane().setBackground(Color.WHITE);
            setVisible(true);
        }
        public static void main(String[] args) {
            new TransactionsUI().setVisible(true);
        }
    }

