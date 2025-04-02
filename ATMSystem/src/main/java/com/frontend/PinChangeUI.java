package com.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
        l1 = new JLabel("Change Your PIN");
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

        l1.setBounds(200, 50, 400, 40);
        add(l1);

        addComponent(l2, 100, 100, tf1, 150, 150);
        addComponent(l3, 100, 200, tf2, 150, 250);
        addComponent(l4, 100, 300, tf3, 150, 350);

        b1.setBounds(300, 460, 150, 50);
        b2.setBounds(500, 460, 150, 50);
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
        b2.addActionListener(e -> processPinChange(authToken));
    }

    private void navigateToTransactions() {
        new TransactionsUI(accountNumber,authToken).setVisible(true);
        dispose();
    }

    private void processPinChange(String authToken) {
        String currentPin = tf1.getText();  // Mã PIN cũ
        String newPin = tf2.getText();      // Mã PIN mới
        String confirmPin = tf3.getText(); // Xác nhận mã PIN mới

        // Kiểm tra đầu vào
        if (currentPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng điền đầy đủ thông tin");
            return;
        } else if (!newPin.equals(confirmPin)) {
            JOptionPane.showMessageDialog(null, "Mã PIN mới và xác nhận không khớp");
            return;
        }

        // Chuẩn bị dữ liệu JSON cho API
        String jsonInputString = String.format(
                "{ \"accountNumber\": \"%s\", \"oldPin\": \"%s\", \"newPin\": \"%s\", \"confirmNewPin\": \"%s\" }",
                "currentLoggedInAccount", currentPin, newPin, confirmPin
        );

        try {
            // Cấu hình HttpURLConnection
            URL url = new URL("http://localhost:8080/api/credential/change-pin"); // Địa chỉ API
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + authToken); // Truyền token qua header
            conn.setDoOutput(true);

            // Gửi dữ liệu JSON
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Kiểm tra phản hồi HTTP
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(null, "Đổi mã PIN thành công!");
            } else {
                // Đọc lỗi từ API
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JOptionPane.showMessageDialog(null, "Thất bại khi đổi mã PIN: " + response.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage());
        }
    }

    private void configureFrame() {
        getContentPane().setBackground(Color.WHITE);
        setSize(700, 600);
        setLocation(250, 0);
        setVisible(true);
    }

}
