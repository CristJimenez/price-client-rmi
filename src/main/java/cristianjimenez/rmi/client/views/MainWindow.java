package cristianjimenez.rmi.client.views;

import java.awt.*;
import javax.swing.*;
import java.io.IOException;

import cristianjimenez.rmi.lib.IRemoteCalculatePrice;
import cristianjimenez.rmi.lib.PriceData;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;

public class MainWindow extends JFrame {

    private CallHandler remoteInvoker;
    private String serverIp = "localhost";
    private int port = 9007;
    private IRemoteCalculatePrice calculatePriceRemote;
    private Client client;

    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JLabel statusLabel;

    private JTextField priceField;
    private JTextField quantityField;
    private JButton calculateButton;
    private JLabel resultLabel;
    private JLabel messageLabel;

    public MainWindow() {
        initComponents();
    }

    private void initComponents() {
        setTitle("PRICE CLIENT");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("PRICE CLIENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 24));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("CONNECTION", buildConnectionPanel());
        tabbedPane.addTab("CALCULATE PRICE", buildCalculatePanel());

        setLayout(new BorderLayout());
        add(titleLabel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        ipField = new JTextField("localhost", 15);
        portField = new JTextField("9007", 15);
        statusLabel = new JLabel("Disconnected");
        statusLabel.setForeground(Color.RED);
        connectButton = new JButton("Connect");
        connectButton.setForeground(new Color(0, 153, 51));
        connectButton.addActionListener(this::connectButtonActionPerformed);

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("SERVER IP:"), c);
        c.gridx = 1; panel.add(ipField, c);

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("PORT:"), c);
        c.gridx = 1; panel.add(portField, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("STATUS:"), c);
        c.gridx = 1; panel.add(statusLabel, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(connectButton, c);

        return panel;
    }

    private JPanel buildCalculatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        priceField = new JTextField(15);
        quantityField = new JTextField(15);
        resultLabel = new JLabel("0.0");
        resultLabel.setForeground(Color.RED);
        resultLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        messageLabel = new JLabel("");
        calculateButton = new JButton("CALCULATE");
        calculateButton.setForeground(new Color(0, 153, 51));
        calculateButton.addActionListener(this::calculateButtonActionPerformed);

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("PRICE:"), c);
        c.gridx = 1; panel.add(priceField, c);

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("QUANTITY:"), c);
        c.gridx = 1; panel.add(quantityField, c);

        c.gridx = 2; c.gridy = 0; c.gridheight = 2;
        panel.add(calculateButton, c);

        c.gridheight = 1; c.gridwidth = 1;
        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("UNIT PRICE:"), c);
        c.gridx = 1; panel.add(resultLabel, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 3;
        panel.add(messageLabel, c);

        return panel;
    }

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if (connectButton.getText().equalsIgnoreCase("Connect")) {
                port = Integer.parseInt(portField.getText());
                serverIp = ipField.getText();
                remoteInvoker = new CallHandler();
                client = new Client(serverIp, port, remoteInvoker);
                calculatePriceRemote = (IRemoteCalculatePrice)
                        client.getGlobal(IRemoteCalculatePrice.class);

                connectButton.setText("Disconnect");
                connectButton.setForeground(Color.RED);
                statusLabel.setText("Connected");
                statusLabel.setForeground(Color.GREEN);
            } else {
                client.close();
                connectButton.setText("Connect");
                statusLabel.setText("Disconnected");
                connectButton.setForeground(new Color(0, 153, 51));
                statusLabel.setForeground(Color.RED);
            }
        } catch (IOException ex) {
            System.out.println("ERROR CONNECTING");
            ex.printStackTrace();
        }
    }

    private void calculateButtonActionPerformed(java.awt.event.ActionEvent evt) {
        float price = Float.parseFloat(priceField.getText());
        float quantity = Float.parseFloat(quantityField.getText());

        Thread thread = new Thread(() -> {
            try {
                PriceData data = new PriceData();
                data.setPrice(price);
                data.setQuantity(quantity);
                data = calculatePriceRemote.calculateUnitPrice(data);

                resultLabel.setText(data.getResult() + "");
                messageLabel.setText(data.getInterpretation());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        "ERROR with the client: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        thread.start();
    }
}