//package com.frontend;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import org.json.JSONObject;
//
//public class ViewAccountUI extends JFrame {
//    JLabel title, accNumberLabel, accNameLabel, balanceLabel;
//    public JTextField accNumberField, accNameField, balanceField;
//    public JButton backButton, logoutButton, refreshButton;
//
//    private String accountNumber; // Số tài khoản của khách hàng
//
//    // Constructor
//    public ViewAccountUI(String accountNumber) {
//        this.accountNumber = accountNumber;
//
//        // Cài đặt giao diện JFrame
//        setTitle("ATM - View Account Information");
//        setSize(600, 400);
//        setLocation(300, 150);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(null);
//
//        // Title label
//        title = new JLabel("VIEW ACCOUNT INFORMATION");
//        title.setFont(new Font("Osward", Font.BOLD, 24));
//        title.setBounds(100, 30, 400, 30);
//        add(title);
//
//        // Account Number label and field
//        accNumberLabel = new JLabel("Account Number:");
//        accNumberLabel.setFont(new Font("Arial", Font.PLAIN, 18));
//        accNumberLabel.setBounds(100, 100, 150, 30);
//        add(accNumberLabel);
//
//        accNumberField = new JTextField();
//        accNumberField.setBounds(300, 100, 200, 30);
//        accNumberField.setEditable(false);  // Chỉ hiển thị
//        add(accNumberField);
//
//        // Account Name label and field
//        accNameLabel = new JLabel("Account Holder Name:");
//        accNameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
//        accNameLabel.setBounds(100, 150, 200, 30);
//        add(accNameLabel);
//
//        accNameField = new JTextField();
//        accNameField.setBounds(300, 150, 200, 30);
//        accNameField.setEditable(false);  // Chỉ hiển thị
//        add(accNameField);
//
//        // Balance label and field
//        balanceLabel = new JLabel("Current Balance:");
//        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 18));
//        balanceLabel.setBounds(100, 200, 150, 30);
//        add(balanceLabel);
//
//        balanceField = new JTextField();
//        balanceField.setBounds(300, 200, 200, 30);
//        balanceField.setEditable(false);  // Chỉ hiển thị
//        add(balanceField);
//
//        // Back button
//        backButton = new JButton("Back");
//        backButton.setBounds(100, 300, 100, 30);
//        add(backButton);
//
//        // Refresh button
//        refreshButton = new JButton("Refresh");
//        refreshButton.setBounds(250, 300, 100, 30);
//        add(refreshButton);
//
//        // Logout button
//        logoutButton = new JButton("Logout");
//        logoutButton.setBounds(400, 300, 100, 30);
//        add(logoutButton);
//
//        // Action Listeners
//        backButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                dispose();  // Quay lại màn hình trước đó
//            }
//        });
//
//        logoutButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                System.exit(0);  // Đăng xuất
//            }
//        });
//
//        refreshButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                loadAccountDetails();  // Tải lại thông tin tài khoản từ backend
//            }
//        });
//
//        // Tải thông tin tài khoản khi tạo UI
//        loadAccountDetails();
//
//        setVisible(true);
//    }
//
//    // Hàm lấy thông tin tài khoản từ backend qua HTTP request
//    public void loadAccountDetails() {
//        try {
//            // Tạo URL đến API backend
//            URL url = new URL("http://localhost:8080/api/accounts/" + accountNumber);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Accept", "application/json");
//
//            // Kiểm tra phản hồi từ server
//            if (conn.getResponseCode() != 200) {
//                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
//            }
//
//            // Đọc dữ liệu từ phản hồi
//            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
//            String output;
//            StringBuilder response = new StringBuilder();
//            while ((output = br.readLine()) != null) {
//                response.append(output);
//            }
//
//            // Đóng kết nối
//            conn.disconnect();
//
//            // Phân tích JSON từ phản hồi
//            JSONObject accountData = new JSONObject(response.toString());
//            String accountHolderName = accountData.getString("accountHolderName");
//            double balance = accountData.getDouble("balance");
//
//            // Hiển thị thông tin lên UI
//            accNumberField.setText(accountNumber);
//            accNameField.setText(accountHolderName);
//            balanceField.setText(String.format("$%.2f", balance));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Error loading account information!", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    public static void main(String[] args) {
//        // Giả sử số tài khoản là "123456789"
//        new ViewAccountUI("123456789");
//    }
//}
