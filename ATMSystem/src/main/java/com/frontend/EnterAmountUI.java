package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EnterAmountUI extends JFrame {
    JLabel l1,l2;
    public JTextField tf1;
    public JButton b1, b2;
    private String accountNumber;  // Thêm biến accountNumber để truyền từ trang trước

    public EnterAmountUI(String accountNumber) {  // Nhận accountNumber từ CashWithdrawlUI
        this.accountNumber = accountNumber;

        setTitle("ATM - Enter Amount");

        // Welcome text
        l1 = new JLabel("ENTER THE AMOUNT TO WITHDRAW");
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(125, 150, 800, 40);
        add(l1);

        // Text field for inputting amount
        tf1 = new JTextField(15);
        tf1.setBounds(175, 250, 520, 60);
        tf1.setFont(new Font("Arial", Font.BOLD, 24));
        add(tf1);
        l2 = new JLabel("Enter multiples of 50.000");
        l2.setFont(new Font("Arial", Font.ITALIC, 18));
        l2.setBounds(250, 320, 200, 30);
        add(l2);

        // Exit button
        b1 = new JButton("Exit");
        b1.setBackground(Color.BLACK);
        b1.setForeground(Color.BLACK);
        b1.setFont(new Font("Arial", Font.BOLD, 24));
        b1.setBounds(350, 450, 150, 50);
        add(b1);

        // Withdraw button
        b2 = new JButton("Withdraw");
        b2.setBackground(Color.BLACK);
        b2.setForeground(Color.BLACK);
        b2.setFont(new Font("Arial", Font.BOLD, 24));
        b2.setBounds(600, 450, 150, 50);
        add(b2);

        // Set layout and background
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // Set JFrame size and visibility
        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);

        // Exit button action
        b1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new CashWithdrawlUI(accountNumber).setVisible(true);
                dispose();
            }
        });

        // Withdraw button action
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String inputAmount = tf1.getText().trim();

                try {
                    double amount = Double.parseDouble(inputAmount);  // Parse số tiền nhập
                    if (amount > 0) {
                        // Chuyển sang TransactionConfirmationUI với số tiền nhập
                        new TransactionConfirmationUI(accountNumber, amount).setVisible(true);
                        dispose();  // Đóng trang EnterAmountUI
                    } else {
                        JOptionPane.showMessageDialog(null, "Please enter a valid amount greater than zero!");
                    }
                } catch (NumberFormatException e) {
                    // Hiển thị lỗi nếu số tiền không hợp lệ
                    JOptionPane.showMessageDialog(null, "Invalid input! Please enter a valid number.");
                }
            }
        });
    }

//    public static void main(String[] args) {
//        new EnterAmountUI("0123789456");  // Truyền accountNumber mẫu
//    }
}
