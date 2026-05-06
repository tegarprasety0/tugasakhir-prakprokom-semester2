import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;

public class LoginForm extends JFrame {

    private boolean isLoginMode = true;

    public LoginForm() {
        setTitle("Aplikasi Produktivitas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 660);
        setLocationRelativeTo(null);

        UIComponents.ModernGradientVerPanel mainBackground = new UIComponents.ModernGradientVerPanel(
            0, 
            new Color(212,187,193),
            new Color(101,77,100)
        );
        mainBackground.setLayout(new GridBagLayout()); 
        setContentPane(mainBackground);

        UIComponents.ModernShadowPanel loginCard = new UIComponents.ModernShadowPanel(30, 15, new Color(212,187,193));
        loginCard.setPreferredSize(new Dimension(350, 450));
        loginCard.setLayout(null);


        UIComponents.ModernLabel lblTitle = new UIComponents.ModernLabel("LOGIN", 45, true, new Color(44, 62, 80));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(0, 40, 350, 40);
        loginCard.add(lblTitle);


        UIComponents.ModernTextField txtUsername = new UIComponents.ModernTextField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(Color.GRAY);
                    int padding = (getHeight() - getFontMetrics(getFont()).getHeight()) / 2;
                    g2.drawString("Username", getInsets().left, getHeight() - padding - getFontMetrics(getFont()).getDescent());
                    g2.dispose();
                }
            }
        };
        txtUsername.setBounds(40, 140, 270, 40);
        loginCard.add(txtUsername);


        UIComponents.ModernPasswordField txtPassword = new UIComponents.ModernPasswordField(15) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (String.valueOf(getPassword()).isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setFont(getFont());
                    g2.setColor(Color.GRAY);
                    int padding = (getHeight() - getFontMetrics(getFont()).getHeight()) / 2;
                    g2.drawString("Password", getInsets().left, getHeight() - padding - getFontMetrics(getFont()).getDescent());
                    g2.dispose();
                }
            }
        };
        txtPassword.setBounds(40, 190, 270, 40);
        loginCard.add(txtPassword);

        JCheckBox chkShowPassword = new JCheckBox("Tampilkan Password");
        chkShowPassword.setBounds(40, 235, 150, 20);
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setForeground(Color.BLACK);
        loginCard.add(chkShowPassword);

        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('\u2022');
            }
        });


        UIComponents.ModernButton btnUtama = new UIComponents.ModernButton(
            "MASUK", 20, 
            new Color(101, 77, 100), new Color(111, 87, 110), new Color(90, 67, 90)
        );
        btnUtama.setBounds(40, 265, 270, 45);
        loginCard.add(btnUtama);

        UIComponents.ModernButton btnSwitch = new UIComponents.ModernButton(
            "Buat Akun", 10,
            new Color(127, 140, 141), new Color(149, 165, 166), new Color(110, 120, 120)
        );
        btnSwitch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSwitch.setBounds(210, 380, 100, 30);
        loginCard.add(btnSwitch);


        btnSwitch.addActionListener(e -> {
            if (isLoginMode) {
                // Berubah ke mode Register
                isLoginMode = false;
                lblTitle.setText("REGISTER");
                btnUtama.setText("BUAT AKUN");
                btnSwitch.setText("Login");
                btnUtama.setBackground(new Color(58, 161, 61));
                btnUtama.setColorHover(new Color(50, 210, 119));
                btnUtama.setColorNormal(new Color(58, 161, 61));
            } else {
                isLoginMode = true;
                lblTitle.setText("LOGIN");
                btnUtama.setText("MASUK");
                btnSwitch.setText("Buat Akun");
                btnUtama.setBackground(new Color(101, 77, 100));
                btnUtama.setColorHover(new Color(111, 87, 110));
                btnUtama.setColorNormal(new Color(101, 77, 100));
            }

            txtUsername.setText("");
            txtPassword.setText("");
            chkShowPassword.setSelected(false);
            txtPassword.setEchoChar('\u2022');
        });

        btnUtama.addActionListener(e -> {
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username dan Password tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isLoginMode) {
                if (cekLoginData(user, pass)) {
                    JOptionPane.showMessageDialog(this, "Login Berhasil! Selamat Datang, " + user + ".");
                    new MainForm(user).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Username atau Password salah! (Silakan 'Buat Akun' terlebih dahulu)", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (cekRegisData(user)) {
                    JOptionPane.showMessageDialog(this, "Username sudah terdaftar! Silakan pilih username lain.", "Registrasi Gagal", JOptionPane.ERROR_MESSAGE);
                    return;
                    
                } else {
                    simpanKeFile(user, pass);
                    JOptionPane.showMessageDialog(this, "Akun berhasil dibuat! Silakan masuk menggunakan akun tersebut.");

                    btnSwitch.doClick(); 
                }
            }
        });

        mainBackground.add(loginCard);
    }

    private boolean cekRegisData(String inputUser){
         File file = new File("akun.txt");

        if (!file.exists()) {
            return false; 
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); 
                if (data.length == 2) {
                    String savedUser = data[0];
                    if (savedUser.equals(inputUser)) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return false;
    }
    private void simpanKeFile(String user, String pass) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("akun.txt", true))) {
            writer.write(user + "," + pass);
            writer.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menyimpan data!", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean cekLoginData(String inputUser, String inputPass) {
        File file = new File("akun.txt");

        if (!file.exists()) {
            return false; 
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(","); 
                if (data.length == 2) {
                    String savedUser = data[0];
                    String savedPass = data[1];
                    
                    if (savedUser.equals(inputUser) && savedPass.equals(inputPass)) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return false;
    }

    public static void main(String[] args) {
        // Menerapkan UI System bawaan OS agar font dan window terlihat rapi
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}