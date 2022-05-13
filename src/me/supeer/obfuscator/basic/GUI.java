package me.supeer.obfuscator.basic;

import me.supeer.obfuscator.basic.obf.Transformer;

import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class GUI extends JFrame {

    private JPanel contentPane;
    private JTextField txtInput;
    private JTextField textWatermark;


    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI frame = new GUI();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public GUI() {
        setResizable(false);
        setTitle("Java String Obfuscator - supeer1 - " + Main.version);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 802, 460);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JButton btnEncrypt = new JButton("Obfuscate");
        btnEncrypt.setFont(new Font("Tahoma", Font.PLAIN, 15));
        btnEncrypt.setBounds(312, 308, 472, 104);
        contentPane.add(btnEncrypt);

        JLabel lblNbls = new JLabel("Java String Obfuscator - supeer1");
        lblNbls.setHorizontalAlignment(SwingConstants.CENTER);
        lblNbls.setBounds(12, 13, 772, 16);
        contentPane.add(lblNbls);



        txtInput = new JTextField();
        txtInput.setToolTipText("input");
        txtInput.setBounds(12, 118, 772, 22);
        contentPane.add(txtInput);
        txtInput.setColumns(10);

        JLabel lblInput = new JLabel("Input :");
        lblInput.setBounds(12, 89, 56, 16);
        contentPane.add(lblInput);

        JLabel lblS = new JLabel("");
        lblS.setHorizontalAlignment(SwingConstants.CENTER);
        lblS.setBounds(12, 244, 772, 35);
        contentPane.add(lblS);

        JLabel lblWatermarkClassName = new JLabel("Watermark class name :");
        lblWatermarkClassName.setBounds(12, 169, 249, 16);
        contentPane.add(lblWatermarkClassName);

        textWatermark = new JTextField();
        textWatermark.setBounds(12, 198, 772, 22);
        contentPane.add(textWatermark);
        textWatermark.setColumns(10);

        JCheckBox chckbxImageEncrypt = new JCheckBox("String Encryption(Image)");
        chckbxImageEncrypt.setBounds(12, 329, 175, 25);
        contentPane.add(chckbxImageEncrypt);

        JCheckBox chckbxStringEncryption = new JCheckBox("String Encryption");
        chckbxStringEncryption.setBounds(12, 359, 175, 25);
        contentPane.add(chckbxStringEncryption);

        btnEncrypt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String inputPath = txtInput.getText();
                boolean enabledIE = chckbxImageEncrypt.isSelected();
                boolean enabledSE = chckbxStringEncryption.isSelected();
                File input = new File(inputPath);
                Transformer transformer = new Transformer(input, textWatermark.getText(), lblS, enabledSE, enabledIE);
                transformer.load();
                transformer.transform();
                transformer.save();
            }
        });

    }
}
