package com.frontend;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class SignUpUI extends JFrame implements ActionListener {

    JLabel l1, l2, l3, l4, l5, l6, l7, l8;
    JTextField t1, t2, t3, t4, t5, t6;
    JButton b1,b2;
    JComboBox<String> accountTypeCombo, statusCombo;

    Random ran = new Random();
    long first4 = Math.abs((ran.nextLong() % 9000L) + 1000L);

    public SignUpUI() {
        setTitle("NEW ACCOUNT APPLICATION FORM");

        l1 = new JLabel("APPLICATION FORM NO. " + first4);
        l1.setFont(new Font("Raleway", Font.BOLD, 38));

        l2 = new JLabel("Full Name:");
        l2.setFont(new Font("Raleway", Font.BOLD, 20));

        l3 = new JLabel("Account Number:");
        l3.setFont(new Font("Raleway", Font.BOLD, 20));

        l4 = new JLabel("Username:");
        l4.setFont(new Font("Raleway", Font.BOLD, 20));

        l5 = new JLabel("Password:");
        l5.setFont(new Font("Raleway", Font.BOLD, 20));

        l6 = new JLabel("Account Type:");
        l6.setFont(new Font("Raleway", Font.BOLD, 20));

        l7 = new JLabel("Status:");
        l7.setFont(new Font("Raleway", Font.BOLD, 20));

        l8 = new JLabel("Initial Balance:");
        l8.setFont(new Font("Raleway", Font.BOLD, 20));

        t1 = new JTextField();
        t1.setFont(new Font("Raleway", Font.BOLD, 20));

        t2 = new JTextField();
        t2.setFont(new Font("Raleway", Font.BOLD, 20));

        t3 = new JTextField();
        t3.setFont(new Font("Raleway", Font.BOLD, 20));

        t4 = new JTextField();
        t4.setFont(new Font("Raleway", Font.BOLD, 20));

        t5 = new JPasswordField();
        t5.setFont(new Font("Raleway", Font.BOLD, 20));

        t6 = new JTextField();
        t6.setFont(new Font("Raleway", Font.BOLD, 20));

        // Tạo combo box cho Account Type
        String[] accountTypes = {"SAVINGS", "CURRENT", "FIXED"};
        accountTypeCombo = new JComboBox<>(accountTypes);
        accountTypeCombo.setFont(new Font("Raleway", Font.BOLD, 16));

        // Tạo combo box cho Status
        String[] statusOptions = {"ACTIVE", "INACTIVE"};
        statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setFont(new Font("Raleway", Font.BOLD, 16));

        b1 = new JButton("Submit");
        b1.setFont(new Font("Raleway", Font.BOLD, 16));
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);
        b1.addActionListener(this);
        b2 = new JButton("Sign In");
        b2.setFont(new Font("Raleway", Font.BOLD, 16));
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        b2.addActionListener(this);
        // Tạo layout và sắp xếp các thành phần
        setLayout(null);

        l1.setBounds(140, 20, 600, 40);
        add(l1);

        l2.setBounds(100, 100, 200, 30);
        add(l2);
        t1.setBounds(300, 100, 400, 30);
        add(t1);
        l3.setBounds(100, 170, 200, 30);
        add(l3);
        t2.setBounds(300, 170, 400, 30);
        add(t2);

        l4.setBounds(100, 240, 200, 30);
        add(l4);
        t3.setBounds(300, 240, 400, 30);
        add(t3);
        l5.setBounds(100, 310, 200, 30);
        add(l5);
        t4.setBounds(300, 310, 400, 30);
        add(t4);
        l6.setBounds(100, 380, 200, 30);
        add(l6);
        accountTypeCombo.setBounds(300, 380, 400, 50);
        add(accountTypeCombo);
        l7.setBounds(100, 450, 200, 30);
        add(l7);
        statusCombo.setBounds(300, 450, 400, 50);
        add(statusCombo);
        l8.setBounds(100, 520, 200, 30);
        add(l8);
        t5.setBounds(300, 520, 400, 30);
        add(t5);
        b1.setBounds(500, 600, 100, 50);
        add(b1);
        b2.setBounds(620, 600, 100, 50);
        add(b2);
        getContentPane().setBackground(Color.WHITE);
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                // Mở giao diện SignUpUI
                new LoginUI().setVisible(true);
                dispose();
            }
        });
        setSize(850, 800);
        setLocation(250, 0);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        String fullName = t1.getText();
        String accountNumber = t2.getText();
        String username = t3.getText();
        String password = t4.getText();
        String accountType = (String) accountTypeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();
        String balance = t5.getText();

        // Kiểm tra nếu các trường còn trống
        if (fullName.equals("") || accountNumber.equals("") || username.equals("") || password.equals("") || balance.equals("")) {
            JOptionPane.showMessageDialog(null, "Please fill all fields");
        } else {
            JOptionPane.showMessageDialog(null, "Account has been created successfully!");
            // Lưu dữ liệu vào cơ sở dữ liệu (hoặc xử lý tiếp)
        }
    }

    public static void main(String[] args) {
        new SignUpUI().setVisible(true);
    }
}
