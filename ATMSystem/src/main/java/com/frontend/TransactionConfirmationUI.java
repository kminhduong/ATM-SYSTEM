package com.frontend;

import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class TransactionConfirmationUI extends JFrame {
    private JLabel l1, l2, amountLabel;
    private JButton exitButton, confirmButton;
    private String accountNumber;
    private double amountToWithdraw;
    private String authToken;

    public TransactionConfirmationUI(String accountNumber, double amountToWithdraw, String authToken) {
        this.accountNumber = accountNumber;
        this.amountToWithdraw = amountToWithdraw;
        this.authToken = authToken;

        setTitle("ATM - Transaction Confirmation");
        setSize(700, 600);
        setLocation(250, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        initializeComponents();
        addComponentsToFrame();
        addActionListeners();

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

    private void initializeComponents() {
        l1 = new JLabel("Transaction Conirmation");
        l1.setFont(new Font("Osward", Font.BOLD, 32));
        l1.setBounds(125, 50, 600, 30);

        l2 = new JLabel("Amount to Withdraw:");
        l2.setFont(new Font("Arial", Font.PLAIN, 20));
        l2.setBounds(150, 200, 200, 30);

        amountLabel = new JLabel(String.format("%.2f VND", amountToWithdraw));
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        amountLabel.setBounds(400, 200, 200, 30);

        confirmButton = new JButton("Confirm");
        confirmButton.setBounds(500, 350, 150, 50);

        exitButton = new JButton("Exit");
        exitButton.setBounds(300, 350, 150, 50);
        JButton[] buttons = {confirmButton, exitButton};
        for (JButton button : buttons) {
            button.setFont(new Font("Arial", Font.BOLD, 24));
            button.setBackground(Color.BLACK);
            button.setForeground(Color.BLACK);
        }
    }

    private void addComponentsToFrame() {
        add(l1);
        add(l2);
        add(amountLabel);
        add(confirmButton);
        add(exitButton);
    }

    private void addActionListeners() {
        confirmButton.addActionListener(e -> processTransaction());

        exitButton.addActionListener(e -> {
            new TransactionsUI(accountNumber, authToken).setVisible(true);
            dispose();
        });
    }

    private void processTransaction() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(authToken);

            // Tạo JSON body cho yêu cầu
            JSONObject requestBody = new JSONObject();
            requestBody.put("accountNumber", accountNumber);
            requestBody.put("amount", amountToWithdraw);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Gửi yêu cầu POST đến backend
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8080/api/transactions/withdraw",
                    entity,
                    String.class
            );

            // Xử lý phản hồi từ backend
            if (response.getStatusCode() == HttpStatus.OK) {
                // Giao dịch thành công, hiển thị thông báo
                JSONObject responseBody = new JSONObject(response.getBody());
                String message = responseBody.getString("message");
                String balance = responseBody.getString("data");

                // Định dạng số dư thành dạng dễ đọc
                double balanceValue = Double.parseDouble(balance);
                DecimalFormat df = new DecimalFormat("#,##0.00");

                JOptionPane.showMessageDialog(
                        this,
                        message + " New Balance: " + df.format(balanceValue),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                // Xử lý các phản hồi không thành công
                handleErrorResponse(response);
            }
        } catch (HttpClientErrorException ex) {
            // Xử lý lỗi HttpClientErrorException khi nhận phản hồi lỗi từ backend
            handleHttpClientErrorException(ex);
        } catch (Exception ex) {
            // Xử lý các lỗi khác
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error processing transaction! Please try again later.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleErrorResponse(ResponseEntity<String> response) {
        try {
            // Xử lý lỗi từ phản hồi backend
            JSONObject errorResponse = new JSONObject(response.getBody());
            String errorMessage = errorResponse.getString("message");

            JOptionPane.showMessageDialog(
                    this,
                    errorMessage,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            // Lỗi khi parse thông báo lỗi từ backend
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to process transaction. Status Code: " + response.getStatusCode(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleHttpClientErrorException(HttpClientErrorException ex) {
        try {
            // Parse thông báo lỗi từ HttpClientErrorException
            JSONObject errorResponse = new JSONObject(ex.getResponseBodyAsString());
            String errorMessage = errorResponse.getString("message");

            JOptionPane.showMessageDialog(
                    this,
                    errorMessage,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception parseException) {
            // Xử lý lỗi khi không thể parse thông báo lỗi
            JOptionPane.showMessageDialog(
                    this,
                    "Unexpected error occurred: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}