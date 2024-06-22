import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class User {
    String accountNumber;
    String pinCode;
    double balance;

    User(String accountNumber, String pinCode, double balance) {
        this.accountNumber = accountNumber;
        this.pinCode = pinCode;
        this.balance = balance;
    }
}

public class ATM_GUI extends JFrame {
    private static final User[] USERS = {
            new User("123456", "1234", 10000.0),
            new User("654321", "4321", 20000.0),
            new User("112233", "2233", 30000.0),
            new User("334455", "4455", 40000.0)
    };

    private static final double PHP_TO_EUR_RATE = 0.016; // Conversion rate from PHP to EUR
    private static final double PHP_TO_JPY_RATE = 2.69; // Conversion rate from PHP to JPY
    private static final double PHP_TO_GBP_RATE = 0.013; // Conversion rate from PHP to GBP
    private static final double PHP_TO_USD_RATE = 0.017; // Conversion rate from PHP to USD

    private static final String BANK_NAME = "Vaulty";

    private User currentUser;

    private JPanel panel;
    private JButton withdrawButton, depositButton, convertButton, checkBalanceButton, logoutButton;
    private JTextArea displayArea;
    private Font font = new Font("Arial", Font.BOLD, 20);

    public ATM_GUI() {
        setTitle(BANK_NAME + " ATM");
        setSize(900, 700);  // Set fixed size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // Center the window

        setupMainPanel();
        showLoginDialog(); // Start by showing the login dialog
    }

    private void setupMainPanel() {
        panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(450, 350)); // Set preferred size for main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10); // Add padding

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(font);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.6;
        panel.add(scrollPane, gbc);

        withdrawButton = createButton("Withdraw Money", e -> withdraw());
        depositButton = createButton("Deposit Money", e -> deposit());
        convertButton = createButton("Convert Currency", e -> convertCurrency());
        checkBalanceButton = createButton("Check Balance", e -> checkBalance());
        logoutButton = createButton("Logout", e -> logout());

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.weighty = 0.1;
        gbc.weightx = 0.5;
        gbc.gridx = 0;
        panel.add(withdrawButton, gbc);

        gbc.gridx = 1;
        panel.add(depositButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(convertButton, gbc);

        gbc.gridx = 1;
        panel.add(checkBalanceButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(logoutButton, gbc);

        // Add the main panel to the frame, but set it to not visible initially
        add(panel);
        panel.setVisible(false);
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        button.setBackground(Color.BLUE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(200, 50));
        return button;
    }

    private void showLoginDialog() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setPreferredSize(new Dimension(450, 350));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel accountLabel = new JLabel("Account Number:");
        accountLabel.setFont(font.deriveFont(Font.PLAIN, 16));
        loginPanel.add(accountLabel, gbc);

        gbc.gridx = 1;
        JTextField accountField = new JTextField(15);
        accountField.setFont(font.deriveFont(Font.PLAIN, 16));
        accountField.setPreferredSize(new Dimension(200, 30));
        loginPanel.add(accountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel pinLabel = new JLabel("PIN Code:");
        pinLabel.setFont(font.deriveFont(Font.PLAIN, 16));
        loginPanel.add(pinLabel, gbc);

        gbc.gridx = 1;
        JPasswordField pinField = new JPasswordField(15);
        pinField.setFont(font.deriveFont(Font.PLAIN, 16));
        pinField.setPreferredSize(new Dimension(200, 30));
        loginPanel.add(pinField, gbc);

        JPanel emptyPanel = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0.2;
        loginPanel.add(emptyPanel, gbc);

        int option = JOptionPane.showConfirmDialog(this, loginPanel, "Enter Account Number and PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String inputAccountNumber = accountField.getText().trim();
            String inputPinCode = new String(pinField.getPassword());

            boolean validCredentials = false;
            for (User user : USERS) {
                if (user.accountNumber.equals(inputAccountNumber) && user.pinCode.equals(inputPinCode)) {
                    currentUser = user;
                    validCredentials = true;
                    break;
                }
            }

            if (!validCredentials) {
                JOptionPane.showMessageDialog(this, "Incorrect account number or PIN code. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                showLoginDialog(); // Retry login
            } else {
                panel.setVisible(true); // Make the main panel visible
                displayArea.setText(""); // Clear the display area
            }
        } else {
            System.exit(0); // Exit if login canceled
        }
    }

    private void withdraw() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter the amount to withdraw (₱): ");
        processTransaction(amountStr, "Withdraw", amount -> {
            if (amount <= currentUser.balance) {
                currentUser.balance -= amount;
                displayArea.append("Withdrawal successful: ₱" + amount + "\n");
                askForReceipt("Withdrawal", amount);
            } else {
                displayArea.append("Insufficient funds to withdraw ₱" + amount + ". Please enter a different amount.\n");
                withdraw(); // Allow the user to re-enter the amount
            }
        });
    }

    private void deposit() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter the amount to deposit (₱): ");
        processTransaction(amountStr, "Deposit", amount -> {
            currentUser.balance += amount;
            displayArea.append("Deposit successful: ₱" + amount + "\n");
        });
    }

    private void convertCurrency() {
        String amountStr = JOptionPane.showInputDialog(this, "Enter the amount to convert (₱): ");
        try {
            double originalAmount = Double.parseDouble(amountStr);
            String[] options = {"Euro (€)", "Japanese Yen (¥)", "British Pound (£)", "US Dollar ($)"};
            int currencyChoice = JOptionPane.showOptionDialog(this, "Select currency to convert to:", "Currency Conversion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            double convertedAmount = 0;
            String currencySymbol = "";
            switch (currencyChoice) {
                case 0:
                    convertedAmount = originalAmount * PHP_TO_EUR_RATE;
                    currencySymbol = "€";
                    break;
                case 1:
                    convertedAmount = originalAmount * PHP_TO_JPY_RATE;
                    currencySymbol = "¥";
                    break;
                case 2:
                    convertedAmount = originalAmount * PHP_TO_GBP_RATE;
                    currencySymbol = "£";
                    break;
                case 3:
                    convertedAmount = originalAmount * PHP_TO_USD_RATE;
                    currencySymbol = "$";
                    break;
                default:
                    displayArea.append("Invalid currency choice.\n");
                    return;
            }

            displayArea.append("Converted amount: " + currencySymbol + " " + convertedAmount + "\n");

            if (convertedAmount <= currentUser.balance) {
                int withdrawChoice = JOptionPane.showConfirmDialog(this, "Do you want to withdraw the converted amount?", "Withdraw Converted Amount", JOptionPane.YES_NO_OPTION);
                if (withdrawChoice == JOptionPane.YES_OPTION) {
                    withdrawConvertedAmount(originalAmount, convertedAmount, currencySymbol);
                } else {
                    askForReceipt("Conversion", originalAmount, convertedAmount, currencySymbol);
                }
            } else {
                displayArea.append("Insufficient funds to convert the amount.\n");
            }
        } catch (NumberFormatException e) {
            displayArea.append("Invalid input. Please enter a valid number.\n");
        }
    }

    private void withdrawConvertedAmount(double originalAmount, double convertedAmount, String currencySymbol) {
        if (convertedAmount <= currentUser.balance) {
            currentUser.balance -= convertedAmount;
            displayArea.append("Withdrawal successful: " + currencySymbol + " " + convertedAmount + "\n");
            askForReceipt("Withdrawal (Converted)", originalAmount, convertedAmount, currencySymbol);
        } else {
            displayArea.append("Insufficient funds to withdraw the converted amount.\n");
        }
    }

    private void checkBalance() {
        displayArea.append("Current balance: ₱" + currentUser.balance + "\n");
    }

    private void processTransaction(String amountStr, String transactionType, java.util.function.DoubleConsumer action) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount > 0) {
                action.accept(amount);
                askForReceipt(transactionType, amount);
            } else {
                displayArea.append("Invalid amount.\n");
            }
        } catch (NumberFormatException e) {
            displayArea.append("Invalid input. Please enter a valid number.\n");
        }
    }

    private void askForReceipt(String transactionType, double... amounts) {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to receive a receipt?", "Receipt", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            printReceipt(transactionType, amounts);
        }
    }

    private void askForReceipt(String transactionType, double originalAmount, double convertedAmount, String currencySymbol) {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to receive a receipt?", "Receipt", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            printReceipt(transactionType, originalAmount, convertedAmount, currencySymbol);
        }
    }

    private void printReceipt(String transactionType, double... amounts) {
        StringBuilder receipt = new StringBuilder();
        receipt.append(BANK_NAME + " Bank Receipt:\n");
        receipt.append("Transaction Type: ").append(transactionType).append("\n");

        for (double amount : amounts) {
            receipt.append("Amount: ₱").append(amount).append("\n");
        }

        JOptionPane.showMessageDialog(this, receipt.toString(), "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void printReceipt(String transactionType, double originalAmount, double convertedAmount, String currencySymbol) {
        StringBuilder receipt = new StringBuilder();
        receipt.append(BANK_NAME + " Bank Receipt:\n");
        receipt.append("Transaction Type: ").append(transactionType).append("\n");
        receipt.append("Original Amount: ₱").append(originalAmount).append("\n");
        receipt.append("Converted Amount: ").append(currencySymbol).append(" ").append(convertedAmount).append("\n");

        JOptionPane.showMessageDialog(this, receipt.toString(), "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        currentUser = null; // Clear current user session
        displayArea.setText(""); // Clear display area immediately
        panel.setVisible(false); // Hide the main panel
    
        // Dispose the JFrame to release resources
        dispose();
    
        // Show the login dialog again
        showLoginDialog();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ATM_GUI().setVisible(true));
    }
}
