import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Main extends JFrame {

    private static final Path DATA_FILE = Paths.get("students.txt");
    private static final Color APP_BACKGROUND = new Color(16, 20, 27);
    private static final Color CARD_BACKGROUND = new Color(26, 32, 41);
    private static final Color CARD_BACKGROUND_ALT = new Color(31, 38, 48);
    private static final Color INPUT_BACKGROUND = new Color(19, 24, 31);
    private static final Color BORDER_COLOR = new Color(67, 78, 94);
    private static final Color GRID_COLOR = new Color(56, 67, 82);
    private static final Color TEXT_PRIMARY = new Color(238, 242, 247);
    private static final Color TEXT_SECONDARY = new Color(160, 172, 188);
    private static final Color TABLE_SELECTION = new Color(45, 99, 182);
    private static final Color TABLE_SELECTION_TEXT = Color.WHITE;
    private static final String[] COURSES = {
            "BCA", "BTech", "MCA", "MBA", "AI", "Data Science"
    };
    private static final Map<String, String[]> COURSE_BRANCHES = createCourseBranches();

    private final List<Student> students = new ArrayList<>();
    private final StudentTableModel tableModel = new StudentTableModel(students);
    private final JTable studentTable = new JTable(tableModel);
    private final TableRowSorter<StudentTableModel> rowSorter = new TableRowSorter<>(tableModel);

    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField ageField = new JTextField();
    private final JComboBox<String> courseBox = new JComboBox<>(COURSES);
    private final JComboBox<String> branchBox = new JComboBox<String>();
    private final JTextField searchField = new JTextField();
    private final JLabel statusLabel = new JLabel("Ready.");

    public Main() {
        super("Student Management System");
        buildUi();
        loadStudents();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            Main app = new Main();
            app.setVisible(true);
        });
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(APP_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        configureTable();
        configureSearch();
        configureCourseBranchSelection();
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(APP_BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(14, 16, 0, 16));

        JLabel title = new JLabel("Student Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage records, search instantly, and save data automatically.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JComponent createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(14, 14));
        mainPanel.setBackground(APP_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        mainPanel.add(createLeftPanel(), BorderLayout.WEST);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(12, 12));
        leftPanel.setBackground(APP_BACKGROUND);
        leftPanel.setPreferredSize(new Dimension(290, 0));

        leftPanel.add(createFormPanel(), BorderLayout.NORTH);
        leftPanel.add(createButtonPanel(), BorderLayout.CENTER);

        return leftPanel;
    }

    private JComponent createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BACKGROUND);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 14, 0);

        JLabel formTitle = new JLabel("Student Details");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_PRIMARY);
        formPanel.add(formTitle, gbc);

        addFormRow(formPanel, gbc, "ID", idField);
        addFormRow(formPanel, gbc, "Name", nameField);
        addFormRow(formPanel, gbc, "Age", ageField);
        addFormRow(formPanel, gbc, "Course", courseBox);
        addFormRow(formPanel, gbc, "Branch", branchBox);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(14, 0, 0, 0);

        JLabel hint = new JLabel("Tip: Click a table row to load it into the form.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(TEXT_SECONDARY);
        formPanel.add(hint, gbc);

        return formPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 10);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_SECONDARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(160, 30));
        styleInputComponent(field);
        panel.add(field, gbc);
    }

    private JComponent createButtonPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(APP_BACKGROUND);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        buttonPanel.setBackground(APP_BACKGROUND);

        JButton addButton = createButton("Add Student", new Color(39, 110, 241));
        JButton updateButton = createButton("Update Student", new Color(24, 145, 102));
        JButton deleteButton = createButton("Delete Student", new Color(206, 70, 64));
        JButton clearButton = createButton("Clear Form", new Color(102, 117, 138));

        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JLabel footerNote = new JLabel("Changes are saved automatically to students.txt");
        footerNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerNote.setForeground(TEXT_SECONDARY);

        wrapper.add(buttonPanel, BorderLayout.NORTH);
        wrapper.add(footerNote, BorderLayout.SOUTH);
        return wrapper;
    }

    private JComponent createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(CARD_BACKGROUND);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel tableTitle = new JLabel("Student Records");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(TEXT_PRIMARY);

        JLabel tableHint = new JLabel("Use the search box to filter by name or course. Click column headers to sort.");
        tableHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableHint.setForeground(TEXT_SECONDARY);

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(tableTitle, BorderLayout.NORTH);
        topSection.add(tableHint, BorderLayout.CENTER);
        topSection.add(createSearchPanel(), BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND_ALT);

        tablePanel.add(topSection, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JComponent createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);

        JButton resetButton = createButton("Reset Search", new Color(102, 117, 138));
        resetButton.setPreferredSize(new Dimension(130, 34));
        resetButton.addActionListener(e -> {
            searchField.setText("");
            setStatus("Search cleared.");
        });

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(100, 34));
        searchField.setToolTipText("Search by name or course");
        styleInputComponent(searchField);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(resetButton, BorderLayout.EAST);
        return searchPanel;
    }

    private JComponent createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(CARD_BACKGROUND);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));

        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        return statusPanel;
    }

    private void configureTable() {
        studentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        studentTable.setRowHeight(40);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setFillsViewportHeight(true);
        studentTable.setBackground(CARD_BACKGROUND_ALT);
        studentTable.setForeground(TEXT_PRIMARY);
        studentTable.setGridColor(GRID_COLOR);
        studentTable.setIntercellSpacing(new Dimension(0, 1));
        studentTable.setShowVerticalLines(false);
        studentTable.setShowHorizontalLines(true);
        studentTable.setSelectionBackground(TABLE_SELECTION);
        studentTable.setSelectionForeground(TABLE_SELECTION_TEXT);
        studentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        studentTable.getTableHeader().setBackground(CARD_BACKGROUND);
        studentTable.getTableHeader().setForeground(TEXT_PRIMARY);
        studentTable.getTableHeader().setPreferredSize(new Dimension(0, 42));
        studentTable.getTableHeader().setReorderingAllowed(false);
        studentTable.getTableHeader().setDefaultRenderer(new TableHeaderRenderer());
        studentTable.setRowSorter(rowSorter);

        studentTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(220);

        studentTable.setDefaultRenderer(Object.class, new StudentTableCellRenderer());
        studentTable.setDefaultRenderer(Integer.class, new StudentTableCellRenderer());

        rowSorter.setSortsOnUpdates(true);

        studentTable.getSelectionModel().addListSelectionListener(this::handleRowSelection);
    }

    private void configureSearch() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });
    }

    private void configureCourseBranchSelection() {
        courseBox.addActionListener(e -> updateBranchOptions());
        updateBranchOptions();
    }

    private void updateBranchOptions() {
        String selectedCourse = (String) courseBox.getSelectedItem();
        String previousBranch = (String) branchBox.getSelectedItem();

        branchBox.removeAllItems();

        String[] branches = COURSE_BRANCHES.get(selectedCourse);
        if (branches == null || branches.length == 0) {
            branchBox.addItem("General");
        } else {
            for (String branch : branches) {
                branchBox.addItem(branch);
            }
        }

        if (previousBranch != null) {
            branchBox.setSelectedItem(previousBranch);
        }

        if (branchBox.getSelectedItem() == null && branchBox.getItemCount() > 0) {
            branchBox.setSelectedIndex(0);
        }
    }

    private void styleInputComponent(JComponent component) {
        component.setBackground(INPUT_BACKGROUND);
        component.setForeground(TEXT_PRIMARY);
        component.setOpaque(true);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)
        ));

        if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setCaretColor(TEXT_PRIMARY);
            textField.setSelectedTextColor(TEXT_PRIMARY);
            textField.setSelectionColor(TABLE_SELECTION);
        }

        if (component instanceof JComboBox) {
            JComboBox<?> comboBox = (JComboBox<?>) component;
            comboBox.setFocusable(false);
            comboBox.setBackground(INPUT_BACKGROUND);
            comboBox.setForeground(TEXT_PRIMARY);
            comboBox.setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton arrowButton = new BasicArrowButton(
                            BasicArrowButton.SOUTH,
                            INPUT_BACKGROUND,
                            INPUT_BACKGROUND,
                            TEXT_SECONDARY,
                            INPUT_BACKGROUND
                    );
                    arrowButton.setBorder(BorderFactory.createEmptyBorder());
                    return arrowButton;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    g.setColor(INPUT_BACKGROUND);
                    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
            });
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus
                ) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus
                    );
                    label.setBorder(new EmptyBorder(6, 10, 6, 10));
                    label.setOpaque(true);
                    label.setBackground(isSelected ? TABLE_SELECTION : INPUT_BACKGROUND);
                    label.setForeground(TEXT_PRIMARY);
                    return label;
                }
            });
        }
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(130, 38));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color hoverColor = lighten(background, 18);
        Color pressedColor = darken(background, 22);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(background);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(pressedColor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(button.contains(e.getPoint()) ? hoverColor : background);
                }
            }
        });

        return button;
    }

    private Color lighten(Color color, int amount) {
        return new Color(
                Math.min(255, color.getRed() + amount),
                Math.min(255, color.getGreen() + amount),
                Math.min(255, color.getBlue() + amount)
        );
    }

    private Color darken(Color color, int amount) {
        return new Color(
                Math.max(0, color.getRed() - amount),
                Math.max(0, color.getGreen() - amount),
                Math.max(0, color.getBlue() - amount)
        );
    }

    private void addStudent() {
        Student newStudent = readStudentFromForm(-1);
        if (newStudent == null) {
            return;
        }

        students.add(newStudent);
        sortStudents();
        tableModel.fireTableDataChanged();
        saveStudents();
        clearForm();
        setStatus("Student added successfully.");
    }

    private void updateStudent() {
        int modelRow = getSelectedModelRow();
        if (modelRow < 0) {
            showError("Please select a student to update.");
            return;
        }

        Student updatedStudent = readStudentFromForm(modelRow);
        if (updatedStudent == null) {
            return;
        }

        students.set(modelRow, updatedStudent);
        sortStudents();
        tableModel.fireTableDataChanged();
        saveStudents();
        selectStudentById(updatedStudent.getId());
        setStatus("Student updated successfully.");
    }

    private void deleteStudent() {
        int modelRow = getSelectedModelRow();
        if (modelRow < 0) {
            showError("Please select a student to delete.");
            return;
        }

        Student student = students.get(modelRow);
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete student \"" + student.getName() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        students.remove(modelRow);
        tableModel.fireTableDataChanged();
        saveStudents();
        clearForm();
        setStatus("Student deleted successfully.");
    }

    private Student readStudentFromForm(int ignoredRow) {
        String idText = idField.getText().trim();
        String nameText = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String courseText = (String) courseBox.getSelectedItem();
        String branchText = (String) branchBox.getSelectedItem();

        if (idText.isEmpty() || nameText.isEmpty() || ageText.isEmpty() || courseText == null || branchText == null) {
            showError("All fields are required.");
            return null;
        }

        int id;
        int age;

        try {
            id = Integer.parseInt(idText);
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException ex) {
            showError("ID and Age must be valid numbers.");
            return null;
        }

        if (id <= 0) {
            showError("ID must be greater than 0.");
            return null;
        }

        if (age < 16 || age > 100) {
            showError("Age must be between 16 and 100.");
            return null;
        }

        if (nameText.length() < 2) {
            showError("Name must contain at least 2 characters.");
            return null;
        }

        if (isDuplicateId(id, ignoredRow)) {
            showError("A student with this ID already exists.");
            return null;
        }

        return new Student(id, nameText, age, courseText, branchText);
    }

    private boolean isDuplicateId(int id, int ignoredRow) {
        for (int i = 0; i < students.size(); i++) {
            if (i == ignoredRow) {
                continue;
            }

            if (students.get(i).getId() == id) {
                return true;
            }
        }

        return false;
    }

    private void handleRowSelection(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            return;
        }

        int modelRow = getSelectedModelRow();
        if (modelRow < 0 || modelRow >= students.size()) {
            return;
        }

        Student student = students.get(modelRow);
        idField.setText(String.valueOf(student.getId()));
        nameField.setText(student.getName());
        ageField.setText(String.valueOf(student.getAge()));
        courseBox.setSelectedItem(student.getCourse());
        updateBranchOptions();
        branchBox.setSelectedItem(student.getBranch());
        setStatus("Loaded student into the form.");
    }

    private void clearForm() {
        studentTable.clearSelection();
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
        courseBox.setSelectedIndex(0);
        updateBranchOptions();
        setStatus("Form cleared.");
    }

    private int getSelectedModelRow() {
        int viewRow = studentTable.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }

        return studentTable.convertRowIndexToModel(viewRow);
    }

    private void applySearchFilter() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
            setStatus("Showing all students.");
            return;
        }

        String safePattern = "(?i)" + Pattern.quote(keyword);
        rowSorter.setRowFilter(RowFilter.regexFilter(safePattern, 1, 3, 4));
        setStatus("Filter applied.");
    }

    private void sortStudents() {
        students.sort(
                Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparingInt(Student::getId)
        );
    }

    private void selectStudentById(int id) {
        for (int modelRow = 0; modelRow < students.size(); modelRow++) {
            if (students.get(modelRow).getId() != id) {
                continue;
            }

            int viewRow = studentTable.convertRowIndexToView(modelRow);
            if (viewRow >= 0) {
                studentTable.setRowSelectionInterval(viewRow, viewRow);
                studentTable.scrollRectToVisible(studentTable.getCellRect(viewRow, 0, true));
            }
            return;
        }
    }

    private void saveStudents() {
        List<String> lines = new ArrayList<>();
        for (Student student : students) {
            lines.add(student.toFileLine());
        }

        try {
            Files.write(
                    DATA_FILE,
                    lines,
                    StandardCharsets.UTF_8
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save data: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
            setStatus("Save failed.");
        }
    }

    private void loadStudents() {
        students.clear();

        if (!Files.exists(DATA_FILE)) {
            tableModel.fireTableDataChanged();
            setStatus("No saved file found. Start by adding a student.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                Student student = Student.fromFileLine(line);
                students.add(student);
            }

            sortStudents();
            tableModel.fireTableDataChanged();
            setStatus("Loaded " + students.size() + " student record(s).");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load data: " + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
            setStatus("Load failed.");
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message + " Total records: " + students.size() + ", Visible: " + studentTable.getRowCount());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private static Map<String, String[]> createCourseBranches() {
        Map<String, String[]> branches = new LinkedHashMap<String, String[]>();
        branches.put("BCA", new String[]{
                "Software Development", "Web Development", "Cyber Security", "Data Analytics", "Cloud Computing"
        });
        branches.put("BTech", new String[]{
                "CSE", "IT", "Mechanical", "Civil", "Electrical", "Electronics", "AI & ML", "Data Science"
        });
        branches.put("MCA", new String[]{
                "Full Stack Development", "AI & ML", "Cyber Security", "Cloud Computing", "Data Engineering"
        });
        branches.put("MBA", new String[]{
                "Finance", "Marketing", "Human Resources", "Operations", "Business Analytics"
        });
        branches.put("AI", new String[]{
                "Machine Learning", "Computer Vision", "Natural Language Processing", "Robotics", "Expert Systems"
        });
        branches.put("Data Science", new String[]{
                "Data Analytics", "Machine Learning", "Big Data", "Data Engineering", "Business Intelligence"
        });
        return branches;
    }

    private static class Student {
        private final int id;
        private final String name;
        private final int age;
        private final String course;
        private final String branch;

        Student(int id, String name, int age, String course, String branch) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.course = course;
            this.branch = branch;
        }

        int getId() {
            return id;
        }

        String getName() {
            return name;
        }

        int getAge() {
            return age;
        }

        String getCourse() {
            return course;
        }

        String getBranch() {
            return branch;
        }

        String toFileLine() {
            return id + "\t" + escape(name) + "\t" + age + "\t" + escape(course) + "\t" + escape(branch);
        }

        static Student fromFileLine(String line) {
            String[] parts = line.contains("\t") ? line.split("\t", -1) : parseLegacyCsvLine(line);
            if (parts.length != 4 && parts.length != 5) {
                throw new IllegalArgumentException("Invalid record format: " + line);
            }

            String rawCourse = unescape(parts[3].trim());
            String course = parts.length == 5 ? rawCourse : normalizeLegacyCourse(rawCourse);
            String branch = parts.length == 5
                    ? unescape(parts[4].trim())
                    : inferLegacyBranch(rawCourse);

            return new Student(
                    Integer.parseInt(parts[0].trim()),
                    unescape(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()),
                    course,
                    branch
            );
        }

        private static String escape(String value) {
            return value.replace("\\", "\\\\").replace("\t", "\\t");
        }

        private static String unescape(String value) {
            StringBuilder builder = new StringBuilder();
            boolean escaping = false;

            for (char ch : value.toCharArray()) {
                if (escaping) {
                    if (ch == 't') {
                        builder.append('\t');
                    } else {
                        builder.append(ch);
                    }
                    escaping = false;
                } else if (ch == '\\') {
                    escaping = true;
                } else {
                    builder.append(ch);
                }
            }

            if (escaping) {
                builder.append('\\');
            }

            return builder.toString();
        }

        private static String[] parseLegacyCsvLine(String line) {
            String[] simpleParts = line.split(",", -1);
            if (simpleParts.length == 5) {
                return simpleParts;
            }

            int firstComma = line.indexOf(',');
            int lastComma = line.lastIndexOf(',');
            int secondLastComma = line.lastIndexOf(',', lastComma - 1);

            if (firstComma < 0 || secondLastComma < 0 || lastComma < 0) {
                throw new IllegalArgumentException("Invalid legacy record format: " + line);
            }

            return new String[]{
                    line.substring(0, firstComma),
                    line.substring(firstComma + 1, secondLastComma),
                    line.substring(secondLastComma + 1, lastComma),
                    line.substring(lastComma + 1)
            };
        }

        private static String inferLegacyBranch(String legacyCourseValue) {
            String normalized = legacyCourseValue.trim();

            if (normalized.equalsIgnoreCase("BTech-CSE")) {
                return "CSE";
            }
            if (normalized.equalsIgnoreCase("BTech-IT")) {
                return "IT";
            }
            if (normalized.equalsIgnoreCase("BTech-Mechanical")) {
                return "Mechanical";
            }
            if (normalized.equalsIgnoreCase("BTech-Civil")) {
                return "Civil";
            }
            if (normalized.equalsIgnoreCase("BTech-Electrical")) {
                return "Electrical";
            }
            if (normalized.equalsIgnoreCase("BTech-ECE")) {
                return "Electronics";
            }
            if (normalized.equalsIgnoreCase("MBA-Finance")) {
                return "Finance";
            }
            if (normalized.equalsIgnoreCase("MBA-Marketing")) {
                return "Marketing";
            }
            if (normalized.equalsIgnoreCase("MCA-AI")) {
                return "AI & ML";
            }

            String[] branches = COURSE_BRANCHES.get(normalized);
            return branches != null && branches.length > 0 ? branches[0] : "General";
        }

        private static String normalizeLegacyCourse(String legacyCourseValue) {
            String normalized = legacyCourseValue.trim();

            if (normalized.equalsIgnoreCase("BTech-CSE")
                    || normalized.equalsIgnoreCase("BTech-IT")
                    || normalized.equalsIgnoreCase("BTech-Mechanical")
                    || normalized.equalsIgnoreCase("BTech-Civil")
                    || normalized.equalsIgnoreCase("BTech-Electrical")
                    || normalized.equalsIgnoreCase("BTech-ECE")) {
                return "BTech";
            }
            if (normalized.equalsIgnoreCase("MBA-Finance")
                    || normalized.equalsIgnoreCase("MBA-Marketing")) {
                return "MBA";
            }
            if (normalized.equalsIgnoreCase("MCA-AI")) {
                return "MCA";
            }

            return normalized;
        }
    }

    private static class StudentTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID", "Name", "Age", "Course", "Branch"};
        private final List<Student> students;

        StudentTableModel(List<Student> students) {
            this.students = students;
        }

        @Override
        public int getRowCount() {
            return students.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Student student = students.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return student.getId();
                case 1:
                    return student.getName();
                case 2:
                    return student.getAge();
                case 3:
                    return student.getCourse();
                case 4:
                    return student.getBranch();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 || columnIndex == 2 ? Integer.class : String.class;
        }
    }

    private static class TableHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, false, false, row, column
            );

            label.setOpaque(true);
            label.setBackground(CARD_BACKGROUND);
            label.setForeground(TEXT_PRIMARY);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_COLOR),
                    new EmptyBorder(0, 12, 0, 12)
            ));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            return label;
        }
    }

    private static class StudentTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            label.setBorder(new EmptyBorder(0, 12, 0, 12));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (!isSelected) {
                label.setBackground(row % 2 == 0 ? CARD_BACKGROUND_ALT : CARD_BACKGROUND);
                label.setForeground(TEXT_PRIMARY);
            } else {
                label.setForeground(TABLE_SELECTION_TEXT);
            }

            if (column == 0 || column == 2) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            return label;
        }
    }
}
