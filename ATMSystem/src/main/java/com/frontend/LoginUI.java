package com.frontend;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LoginUI extends JFrame {
    private JLabel l1, l2, l3;
    private JTextField tf1;
    private JPasswordField pf2;
    private JButton b1, b2, b3;

    public LoginUI() {
        setTitle("LOGIN ACCOUNT");
        initializeComponents();
        addComponentsToFrame();
        addActionListeners();
        configureFrame();
    }

    private void initializeComponents() {
        l1 = new JLabel("WELCOME TO ATM");
        l1.setFont(new Font("Osward", Font.BOLD, 32));

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

        b3 = new JButton("Withdraw by OTP");
        b3.setBackground(Color.BLACK);
        b3.setForeground(Color.BLACK);
        b3.setFont(new Font("Arial", Font.BOLD, 20));
    }

    private void addComponentsToFrame() {
        setLayout(null);

        l1.setBounds(200, 50, 450, 40);
        add(l1);

        l2.setBounds(100, 175, 300, 30);
        add(l2);

        tf1.setBounds(375, 175, 230, 40);
        add(tf1);

        l3.setBounds(100, 270, 300, 30);
        add(l3);

        pf2.setBounds(375, 270, 230, 40);
        add(pf2);

        b1.setBounds(175, 350, 150, 50);
        add(b1);

        b2.setBounds(375, 350, 150, 50);
        add(b2);

        b3.setBounds(200, 420, 300, 50);
        add(b3);
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

        try {
            URL url = new URL("http://localhost:8080/api/transactions/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = String.format("{\"accountNumber\":\"%s\", \"pin\":\"%s\"}", accountNumber, pin);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }
                br.close();
                connection.disconnect();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String authToken = jsonResponse.getString("token");

                JOptionPane.showMessageDialog(null, "Login Successful!");

                new TransactionsUI(accountNumber, authToken).setVisible(true); // Truyền token
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Account Number or PIN", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearFields() {
        tf1.setText("");
        pf2.setText("");
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(700, 600);
        setLocation(250, 0);
        setVisible(true);
    }

    public static void main(String[] args) {
        new LoginUI();
    }
}
