package com.frontend;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class CashWithdrawl_SignInUI extends JFrame {
    JLabel l1, l2, l3;
    public JTextField tf1;
    public JPasswordField pf2;
    public JButton b1, b2, b3;
    public CashWithdrawl_SignInUI(){
        l1 = new JLabel("WELCOME TO ATM");
        l1.setFont(new Font("Osward", Font.BOLD, 38));
        l1.setBounds(250, 50, 450, 40);
        add(l1);

        l2 = new JLabel("Account Number:");
        l2.setFont(new Font("Raleway", Font.BOLD, 22));
        l2.setBounds(50, 175, 300, 30);
        add(l2);

        tf1 = new JTextField(15);
        tf1.setBounds(300, 175, 230, 40);
        tf1.setFont(new Font("Arial", Font.BOLD, 14));
        add(tf1);

        // PIN label and password field
        l3 = new JLabel("Pin:");
        l3.setFont(new Font("Raleway", Font.BOLD, 22));
        l3.setBounds(50, 270, 300, 30);
        add(l3);
        pf2 = new JPasswordField(15);
        pf2.setFont(new Font("Arial", Font.BOLD, 14));
        pf2.setBounds(300, 270, 230, 40);
        add(pf2);
        b1 = new JButton("Continue");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);

        b2 = new JButton("Exit");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        setLayout(null);
        b1.setFont(new Font("Arial", Font.BOLD, 14));
        b1.setBounds(150, 370, 100, 50);
        add(b1);

        b2.setFont(new Font("Arial", Font.BOLD, 14));
        b2.setBounds(330, 370, 100, 50);
        add(b2);
        getContentPane().setBackground(Color.WHITE);

        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // Handle sign in logic
                String cardNo = tf1.getText();
                String pin = new String(pf2.getPassword());

                // This is where you'd normally check the credentials
                if (cardNo.equals("123456") && pin.equals("1234")) {
                    new CashWithdrawlUI().setVisible(true);
                    dispose();
                    // You can redirect to another UI (e.g., Transactions UI) here
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Card No or PIN");
                }
            }
        });
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                // Mở giao diện SignUpUI
                new TransactionsUI().setVisible(true);
                dispose();
            }
        });
    }
    public static void main(String[] args) {
        new CashWithdrawl_SignInUI();
    }
}

