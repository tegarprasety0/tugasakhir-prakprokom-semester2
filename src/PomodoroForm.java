import java.awt.*;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class PomodoroForm extends JFrame {

    private final int        taskIndex;
    private final MainForm   mainFormRef;
    private final SoundManager soundManager;

    private final Timer timerSwing;
    private int     totalDetik = 0;
    private boolean isRunning  = false;

    private final UIComponents.ModernLabel lblTimerDisplay = new UIComponents.ModernLabel("00:00", 96, true, new Color(101, 77, 100));
    private final UIComponents.ModernLabel lblTaskName     = new UIComponents.ModernLabel("Nama tugas", 18, false, new Color(80, 60, 80));
    
    private final UIComponents.ModernTextField txtMenit    = new UIComponents.ModernTextField(12);
    private final UIComponents.ModernTextField txtDetik    = new UIComponents.ModernTextField(12);

    private final UIComponents.ModernButton btnStartPause = new UIComponents.ModernButton(
        "Start", 15,
        new Color(60, 150, 90), new Color(80, 180, 110), new Color(45, 125, 70)
    );
    private final UIComponents.ModernButton btnReset = new UIComponents.ModernButton(
        "Reset", 15,
        new Color(60, 120, 160), new Color(80, 150, 190), new Color(45, 100, 140)
    );
    private final UIComponents.ModernButton btnBack = new UIComponents.ModernButton(
        "Kembali", 15,
        new Color(180, 70, 70), new Color(210, 90, 90), new Color(150, 50, 50)
    );
    private final UIComponents.ModernButton btnSelesai = new UIComponents.ModernButton(
        "Selesai", 15,
        new Color(60, 40, 70), new Color(80, 60, 90), new Color(40, 27, 60)
    );


    public PomodoroForm(int taskIndex, MainForm mainFormRef) {
        this.taskIndex   = taskIndex;
        this.mainFormRef = mainFormRef;
        this.soundManager = mainFormRef.getSoundManager();

        setTitle("Pomodoro Timer — " + mainFormRef.getTitle().split(" — ")[1]);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        txtMenit.setText("25");
        txtDetik.setText("00");
        txtMenit.setHorizontalAlignment(JTextField.CENTER);
        txtDetik.setHorizontalAlignment(JTextField.CENTER);
        txtMenit.setPreferredSize(new Dimension(70, 40));
        txtDetik.setPreferredSize(new Dimension(70, 40));
        txtMenit.setBorderColor(new Color(180, 140, 180));
        txtDetik.setBorderColor(new Color(180, 140, 180));

        if (taskIndex != -1 && taskIndex < mainFormRef.getTaskManager().getSize()) {
            lblTaskName.setText(" Sedang mengerjakan: " + mainFormRef.getTaskManager().getTask(taskIndex));
        }

        timerSwing = new Timer(1000, e -> onTick());

        buildLayout();
        wireListeners();

        setSize(1024, 660);
        setLocationRelativeTo(null);
    }


    private void buildLayout() {
        UIComponents.ModernGradientVerPanel mainBackground = new UIComponents.ModernGradientVerPanel(
            0,
            new Color(212,187,193),
            new Color(101,77,100)
        );
        mainBackground.setLayout(new BorderLayout(0, 0));
        mainBackground.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        UIComponents.ModernGradientHorPanel headerPanel = new UIComponents.ModernGradientHorPanel(
            20,
            new Color(101, 77, 100),
            new Color(60, 40, 70)
        );
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        headerPanel.setPreferredSize(new Dimension(0, 70));

        UIComponents.ModernLabel titleLabel = new UIComponents.ModernLabel(
            " Pomodoro Timer", 26, true, Color.WHITE
        );
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        lblTaskName.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblTaskName);
        contentPanel.add(Box.createVerticalStrut(20));

        UIComponents.ModernShadowPanel timerCard = new UIComponents.ModernShadowPanel(
            24, 15, new Color(255, 250, 255)
        );
        timerCard.setLayout(new BorderLayout());
        timerCard.setPreferredSize(new Dimension(420, 240));
        timerCard.setMaximumSize(new Dimension(420, 240));
        
        lblTimerDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        timerCard.add(lblTimerDisplay, BorderLayout.CENTER);
        
        contentPanel.add(timerCard);
        contentPanel.add(Box.createVerticalStrut(30));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        inputPanel.setOpaque(false);
        
        UIComponents.ModernLabel colonLabel = new UIComponents.ModernLabel(":", 28, true, new Color(101, 77, 100));
        
        inputPanel.add(txtMenit);
        inputPanel.add(colonLabel);
        inputPanel.add(txtDetik);
        
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createVerticalStrut(25));

        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionBtnRow.setOpaque(false);
        
        btnStartPause.setPreferredSize(new Dimension(130, 48));
        btnReset.setPreferredSize(new Dimension(130, 48));
        
        actionBtnRow.add(btnStartPause);
        actionBtnRow.add(btnReset);
        
        contentPanel.add(actionBtnRow);
        contentPanel.add(Box.createVerticalGlue());

        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setOpaque(false);
        navBar.setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
        
        btnBack.setPreferredSize(new Dimension(110, 42));
        btnSelesai.setPreferredSize(new Dimension(110, 42));
        
        navBar.add(btnBack, BorderLayout.WEST);
        navBar.add(btnSelesai, BorderLayout.EAST);

        mainBackground.add(headerPanel, BorderLayout.NORTH);
        mainBackground.add(contentPanel, BorderLayout.CENTER);
        mainBackground.add(navBar, BorderLayout.SOUTH);

        setContentPane(mainBackground);
    }


    private void wireListeners() {
        btnStartPause.addActionListener(e -> onStartPause());
        btnReset.addActionListener(e      -> onReset());
        btnBack.addActionListener(e       -> onBack());
        btnSelesai.addActionListener(e    -> onSelesai());
    }


    private void onTick() {
        totalDetik--;
        updateTimerDisplay();

        if (totalDetik <= 0) {
            timerSwing.stop();
            isRunning = false;
            btnStartPause.setText("Start");
            lblTimerDisplay.setText("00:00");

            Clip alarm = soundManager.playAlarm();
            JOptionPane.showMessageDialog(getRootPane(), "Waktu Habis! Istirahat sejenak.");
            soundManager.stopAlarm(alarm);
        }
    }


    private void onStartPause() {
        if (isRunning) {
            timerSwing.stop();
            isRunning = false;
            btnStartPause.setText("Resume");
        } else {
            if ("Start".equals(btnStartPause.getText())) {
                Integer parsed = parseTimeInput();
                if (parsed == null) return;
                totalDetik = parsed;
            }

            if (totalDetik > 0) {
                timerSwing.start();
                isRunning = true;
                btnStartPause.setText("Pause");
            }
        }
    }

    private void onReset() {
        timerSwing.stop();
        isRunning = false;
        btnStartPause.setText("Start");

        Integer parsed = parseTimeInputSafe();
        totalDetik = (parsed != null) ? parsed : 0;
        updateTimerDisplay();
    }

    private void onBack() {
        timerSwing.stop();
        mainFormRef.setVisible(true);
        dispose();
    }

    private void onSelesai() {
        if (mainFormRef == null) {
            JOptionPane.showMessageDialog(this,
                    "Error: Referensi ke MainForm tidak ditemukan!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        timerSwing.stop();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tugas selesai! Hapus dari daftar?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            mainFormRef.getTaskManager().removeTask(taskIndex);
            mainFormRef.getFileManager().saveAll();

            mainFormRef.setVisible(true);
            dispose();
        }
    }


    private void updateTimerDisplay() {
        int menit = totalDetik / 60;
        int detik = totalDetik % 60;
        lblTimerDisplay.setText(String.format("%02d:%02d", menit, detik));
    }

    private Integer parseTimeInput() {
        try {
            int menit = Integer.parseInt(txtMenit.getText().trim());
            int detik = Integer.parseInt(txtDetik.getText().trim());
            int total = (menit * 60) + detik;

            if (total <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Input waktu harus lebih besar dari 0",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return total;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Input menit dan detik harus berupa angka!",
                    "Error Input", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private Integer parseTimeInputSafe() {
        try {
            String m = txtMenit.getText().isEmpty() ? "0" : txtMenit.getText().trim();
            String d = txtDetik.getText().isEmpty() ? "0" : txtDetik.getText().trim();
            return (Integer.parseInt(m) * 60) + Integer.parseInt(d);
        } catch (NumberFormatException e) {
            txtMenit.setText("25");
            txtDetik.setText("00");
            return 25 * 60;
        }
    }
}