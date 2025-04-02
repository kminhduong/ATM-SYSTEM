package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PinChangeUI extends JFrame {
    private JLabel l1, l2, l3, l4;
    private JTextField tf1, tf2, tf3;
    private JButton b1, b2;
    private String accountNumber;
    private String authToken;

    public PinChangeUI(String accountNumber, String authToken) {
        this.accountNumber = accountNumber;
        this.authToken = authToken;
        setTitle("ATM - PIN Change");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("CHANGE YOUR PIN");
        l1.setFont(new Font("Osward", Font.BOLD, 32));

        l2 = new JLabel("Enter current PIN:");
        l3 = new JLabel("Enter new PIN:");
        l4 = new JLabel("Confirm new PIN:");

        JLabel[] labels = {l2, l3, l4};
        for (JLabel label : labels) {
            label.setFont(new Font("Raleway", Font.BOLD, 24));
        }

        tf1 = new JTextField(15);
        tf2 = new JTextField(15);
        tf3 = new JTextField(15);

        JTextField[] textFields = {tf1, tf2, tf3};
        for (JTextField textField : textFields) {
            textField.setFont(new Font("Arial", Font.BOLD, 24));
        }

        b1 = new JButton("Exit");
        b2 = new JButton("Change");

        JButton[] buttons = {b1, b2};
        for (JButton button : buttons) {
            button.setFont(new Font("Arial", Font.BOLD, 24));
            button.setBackground(Color.BLACK);
            button.setForeground(Color.BLACK);
        }
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(250, 50, 400, 40);
        add(l1);

        addComponent(l2, 200, 150, tf1, 250, 200);
        addComponent(l3, 200, 250, tf2, 250, 300);
        addComponent(l4, 200, 350, tf3, 250, 400);

        b1.setBounds(200, 500, 200, 50);
        b2.setBounds(450, 500, 200, 50);
        add(b1);
        add(b2);
    }

    private void addComponent(JLabel label, int lx, int ly, Component field, int fx, int fy) {
        label.setBounds(lx, ly, 300, 30);
        field.setBounds(fx, fy, 400, 40);
        add(label);
        add(field);
    }

    private void addActionListeners() {
        b1.addActionListener(e -> navigateToTransactions());
        b2.addActionListener(e -> processPinChange());
    }

    private void navigateToTransactions() {
        new TransactionsUI(accountNumber,authToken).setVisible(true);
        dispose();
    }

    private void processPinChange() {
        String currentPin = tf1.getText();
        String newPin = tf2.getText();
        String confirmPin = tf3.getText();

        if (currentPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields");
        } else if (!newPin.equals(confirmPin)) {
            JOptionPane.showMessageDialog(null, "New PIN and confirmation do not match");
        } else {
            JOptionPane.showMessageDialog(null, "PIN changed successfully!");
        }
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(850, 800);
        setLocation(250, 200);
        setVisible(true);
    }
}
