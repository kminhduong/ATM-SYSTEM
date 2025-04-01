package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class SignUpUI extends JFrame {
    private JLabel l1, l2, l3, l4, l5, l6, l7, l8;
    private JTextField t1, t2, t3, t4, t5;
    private JButton b1, b2;
    private JComboBox<String> accountTypeCombo, statusCombo;
    private long applicationNumber;

    public SignUpUI() {
        applicationNumber = generateApplicationNumber();
        setTitle("NEW ACCOUNT APPLICATION FORM");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private long generateApplicationNumber() {
        Random ran = new Random();
        return Math.abs((ran.nextLong() % 9000L) + 1000L);
    }

    private void initializeComponents() {
        l1 = new JLabel("APPLICATION FORM NO. " + applicationNumber);
        l1.setFont(new Font("Raleway", Font.BOLD, 32));

        l2 = new JLabel("Full Name:");
        l3 = new JLabel("Account Number:");
        l4 = new JLabel("Username:");
        l5 = new JLabel("Password:");
        l6 = new JLabel("Account Type:");
        l7 = new JLabel("Status:");
        l8 = new JLabel("Initial Balance:");

        JLabel[] labels = {l2, l3, l4, l5, l6, l7, l8};
        for (JLabel label : labels) {
            label.setFont(new Font("Raleway", Font.BOLD, 20));
        }

        t1 = new JTextField();
        t2 = new JTextField();
        t3 = new JTextField();
        t4 = new JTextField();
        t5 = new JPasswordField();

        JTextField[] textFields = {t1, t2, t3, t4, t5};
        for (JTextField textField : textFields) {
            textField.setFont(new Font("Raleway", Font.BOLD, 20));
        }

        accountTypeCombo = new JComboBox<>(new String[]{"SAVINGS", "CURRENT", "FIXED"});
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});

        b1 = new JButton("Submit");
        b2 = new JButton("Sign In");
        JButton[] buttons = {b1, b2};
        for (JButton button : buttons) {
            button.setFont(new Font("Raleway", Font.BOLD, 16));
            button.setBackground(Color.BLACK);
            button.setForeground(Color.BLACK);
        }
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(140, 20, 600, 40);
        add(l1);

        addComponent(l2, 100, 100, t1, 300, 100);
        addComponent(l3, 100, 170, t2, 300, 170);
        addComponent(l4, 100, 240, t3, 300, 240);
        addComponent(l5, 100, 310, t4, 300, 310);
        addComponent(l6, 100, 380, accountTypeCombo, 300, 380);
        addComponent(l7, 100, 450, statusCombo, 300, 450);
        addComponent(l8, 100, 520, t5, 300, 520);

        b1.setBounds(500, 600, 100, 50);
        b2.setBounds(620, 600, 100, 50);
        add(b1);
        add(b2);
    }

    private void addComponent(JLabel label, int lx, int ly, Component field, int fx, int fy) {
        label.setBounds(lx, ly, 200, 30);
        field.setBounds(fx, fy, 400, 30);
        add(label);
        add(field);
    }

    private void addActionListeners() {
        b1.addActionListener(e -> processFormSubmission());
        b2.addActionListener(e -> navigateToLogin());
    }

    private void processFormSubmission() {
        String fullName = t1.getText();
        String accountNumber = t2.getText();
        String username = t3.getText();
        String password = t4.getText();
        String accountType = (String) accountTypeCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();
        String balance = t5.getText();

        if (fullName.isEmpty() || accountNumber.isEmpty() || username.isEmpty() || password.isEmpty() || balance.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill all fields");
        } else {
            JOptionPane.showMessageDialog(null, "Account has been created successfully!");
        }
    }

    private void navigateToLogin() {
        new LoginUI().setVisible(true);
        dispose();
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(850, 800);
        setLocation(250, 0);
        setVisible(true);
    }

    public static void main(String[] args) {
        new SignUpUI().setVisible(true);
    }
}
