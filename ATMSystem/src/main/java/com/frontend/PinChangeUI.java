package com.frontend;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class PinChangeUI extends JFrame {
    JLabel l1,l2,l3,l4;
    public JTextField tf1, tf2,tf3;
    public JButton b1, b2;
    public PinChangeUI() {
        l1 = new JLabel("ENTER THE AMOUNT TO DEPOSIT");
        l1.setFont(new Font("Osward", Font.BOLD, 30));
        l1.setBounds(175, 75, 800, 40);
        add(l1);
        l2 = new JLabel("Enter current PIN:");
        l2.setFont(new Font("Raleway", Font.BOLD, 24));
        l2.setBounds(200, 150, 450, 40);
        add(l2);
        tf1 = new JTextField(15);
        tf1.setBounds(250, 200, 400, 40);
        tf1.setFont(new Font("Arial", Font.BOLD, 24));
        add(tf1);
        l3 = new JLabel("Enter new PIN:");
        l3.setFont(new Font("Raleway", Font.BOLD, 22));
        l3.setBounds(200, 250, 300, 30);
        add(l3);
        tf2 = new JTextField(15);
        tf2.setBounds(250, 300, 400, 40);
        tf2.setFont(new Font("Arial", Font.BOLD, 24));
        add(tf2);
        l4 = new JLabel("Confirm new PIN:");
        l4.setFont(new Font("Raleway", Font.BOLD, 22));
        l4.setBounds(200, 350, 300, 30);
        add(l4);
        tf3 = new JTextField(15);
        tf3.setBounds(250, 400, 400, 40);
        tf3.setFont(new Font("Arial", Font.BOLD, 24));
        add(tf3);
        b1 = new JButton("Exit");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);

        b2 = new JButton("Change");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        setLayout(null);
        b1.setFont(new Font("Arial", Font.BOLD, 24));
        b1.setBounds(350, 500, 200, 60);
        add(b1);

        b2.setFont(new Font("Arial", Font.BOLD, 24));
        b2.setBounds(600, 500, 200, 60);
        add(b2);
        getContentPane().setBackground(Color.WHITE);

        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new TransactionsUI().setVisible(true);
                dispose();
            }
        });
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
    }
    public static void main(String[] args) {
        new PinChangeUI().setVisible(true);
    }
}
