import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.text.SimpleDateFormat;

// -------------------- MODEL CLASSES --------------------
class Student {
    private int id;
    private String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}

class Subject {
    private int id;
    private String name;

    public Subject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}

class AttendanceLog {
    private int studentId;
    private int subjectId;
    private Date date;
    private boolean present;

    public AttendanceLog(int studentId, int subjectId, Date date, boolean present) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.date = date;
        this.present = present;
    }

    public int getStudentId() { return studentId; }
    public int getSubjectId() { return subjectId; }
    public Date getDate() { return date; }
    public boolean isPresent() { return present; }
}


// -------------------- VIEW: CHART --------------------
class AttendanceChartView extends JPanel {
    private Map<Date, Boolean> attendanceData = new TreeMap<>();

    public void setAttendanceData(Map<Date, Boolean> data) {
        attendanceData = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int margin = 50;
        int chartHeight = height - 120;

        // GRID BACKGROUND
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(margin - 20, 30, width - (margin * 2) + 40, chartHeight + 20);

        // grid lines
        g2d.setColor(new Color(210, 210, 210));
        for (int i = 0; i < 5; i++) {
            int y = 40 + (chartHeight / 4) * i;
            g2d.drawLine(margin - 20, y, width - margin + 20, y);
        }

        if (attendanceData.isEmpty()) {
            g2d.setColor(Color.GRAY);
            g2d.drawString("No attendance records yet.", width / 2 - 60, height / 2);
            return;
        }

        int count = attendanceData.size();
        int barWidth = Math.max(50, (width - (margin * 2)) / count - 10);

        int x = margin;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");

        for (Map.Entry<Date, Boolean> entry : attendanceData.entrySet()) {
            boolean present = entry.getValue();

            // Gradient for better look
            Color topColor = present ? new Color(102, 255, 102) : new Color(255, 102, 102);
            Color bottomColor = present ? new Color(0, 180, 0) : new Color(200, 0, 0);

            GradientPaint gp = new GradientPaint(0, 40, topColor, 0, height, bottomColor);
            g2d.setPaint(gp);

            // draw rounded bar
            g2d.fillRoundRect(x, 40, barWidth, chartHeight - 10, 25, 25);

            // border
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRoundRect(x, 40, barWidth, chartHeight - 10, 25, 25);

            // date label
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(sdf.format(entry.getKey()), x + 10, height - 40);

            x += barWidth + 20;
        }
    }
}


// -------------------- VIEW: ALERTS --------------------
class AlertsView extends JTextArea {
    public AlertsView() {
        setEditable(false);
        setFont(new Font("Arial", Font.PLAIN, 15));
        setBackground(new Color(250, 250, 250));
        setForeground(Color.BLACK);
        setText("Alerts:\n");
    }

    public void clearAlerts() {
        setText("Alerts:\n");
    }

    public void addAlert(String alert) {
        append(alert + "\n");
    }
}


// -------------------- CONTROLLER --------------------
class AttendanceController {
    private java.util.List<Student> students = new ArrayList<>();
    private java.util.List<Subject> subjects = new ArrayList<>();
    private java.util.List<AttendanceLog> logs = new ArrayList<>();
    private AttendanceChartView chartView;
    private AlertsView alertsView;

    private int totalClassesInSemester = 100;

    public AttendanceController(AttendanceChartView chartView, AlertsView alertsView) {
        this.chartView = chartView;
        this.alertsView = alertsView;

        students.add(new Student(1, "Alice"));
        students.add(new Student(2, "Bob"));
        students.add(new Student(3, "Charlie"));

        subjects.add(new Subject(1, "Math"));
        subjects.add(new Subject(2, "Physics"));
        subjects.add(new Subject(3, "Chemistry"));
    }

    public java.util.List<Student> getStudents() { return students; }
    public java.util.List<Subject> getSubjects() { return subjects; }

    public void markAttendance(int studentId, int subjectId, Date date, boolean present) {
        logs.add(new AttendanceLog(studentId, subjectId, date, present));
        updateViews(studentId, subjectId);
    }

    private double calcPercentage(int studentId, int subjectId) {
        int total = 0, present = 0;
        for (AttendanceLog l : logs) {
            if (l.getStudentId() == studentId && l.getSubjectId() == subjectId) {
                total++;
                if (l.isPresent()) present++;
            }
        }
        return total == 0 ? 0 : (present * 100.0 / total);
    }

    private boolean canReach75(int studentId, int subjectId) {
        int total = 0, present = 0;

        for (AttendanceLog l : logs) {
            if (l.getStudentId() == studentId && l.getSubjectId() == subjectId) {
                total++;
                if (l.isPresent()) present++;
            }
        }

        int remaining = totalClassesInSemester - total;
        double required = (0.75 * totalClassesInSemester) - present;

        return required <= remaining;
    }

    public void updateViews(int studentId, int subjectId) {
        Map<Date, Boolean> data = new TreeMap<>();
        for (AttendanceLog l : logs) {
            if (l.getStudentId() == studentId && l.getSubjectId() == subjectId)
                data.put(l.getDate(), l.isPresent());
        }
        chartView.setAttendanceData(data);

        double pct = calcPercentage(studentId, subjectId);

        alertsView.clearAlerts();

        if (pct < 75) {
            alertsView.addAlert("Low attendance: " + String.format("%.2f", pct) + "%");
            if (canReach75(studentId, subjectId))
                alertsView.addAlert("You can still reach 75% by attending all remaining classes.");
            else
                alertsView.addAlert("You CANNOT reach 75% by the end of semester.");
        } else {
            alertsView.addAlert("Good attendance: " + String.format("%.2f", pct) + "%");
        }
    }
}


// -------------------- MAIN APPLICATION --------------------
public class CollegeAttendanceAnalyzer extends JFrame {

    private AttendanceController controller;
    private AttendanceChartView chartView;
    private AlertsView alertsView;
    private JComboBox<Student> studentCombo;
    private JComboBox<Subject> subjectCombo;

    private Calendar attendanceCalendar = Calendar.getInstance();

    public CollegeAttendanceAnalyzer() {
        setTitle("College Attendance Analyzer (MVC)");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chartView = new AttendanceChartView();
        alertsView = new AlertsView();
        controller = new AttendanceController(chartView, alertsView);

        JPanel top = new JPanel();
        top.setBackground(new Color(240, 240, 240));
        studentCombo = new JComboBox<>();
        subjectCombo = new JComboBox<>();

        controller.getStudents().forEach(studentCombo::addItem);
        controller.getSubjects().forEach(subjectCombo::addItem);

        JButton presentBtn = new JButton("Mark Present");
        JButton absentBtn = new JButton("Mark Absent");

        presentBtn.setBackground(new Color(102, 255, 102));
        absentBtn.setBackground(new Color(255, 102, 102));

        top.add(new JLabel("Student:"));
        top.add(studentCombo);
        top.add(new JLabel("Subject:"));
        top.add(subjectCombo);
        top.add(presentBtn);
        top.add(absentBtn);

        add(top, BorderLayout.NORTH);
        add(chartView, BorderLayout.CENTER);
        add(new JScrollPane(alertsView), BorderLayout.SOUTH);

        presentBtn.addActionListener(e -> markAttendance(true));
        absentBtn.addActionListener(e -> markAttendance(false));

        studentCombo.addActionListener(e -> refresh());
        subjectCombo.addActionListener(e -> refresh());

        refresh();
    }

    private void markAttendance(boolean present) {
        Student s = (Student) studentCombo.getSelectedItem();
        Subject sub = (Subject) subjectCombo.getSelectedItem();

        Date recordDate = attendanceCalendar.getTime();
        attendanceCalendar.add(Calendar.DAY_OF_MONTH, 1);

        controller.markAttendance(s.getId(), sub.getId(), recordDate, present);
    }

    private void refresh() {
        Student s = (Student) studentCombo.getSelectedItem();
        Subject sub = (Subject) subjectCombo.getSelectedItem();
        controller.updateViews(s.getId(), sub.getId());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CollegeAttendanceAnalyzer().setVisible(true));
    }
}
