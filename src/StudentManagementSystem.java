// Main class that runs the Student Management System
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StudentManagementSystem {
    public static void main(String[] args) {
        // Initialize the database
        initializeDatabase();

        // Create sample students
        Student student1 = new Student(1, "John Smith", "john.smith@example.com", 10);
        Student student2 = new Student(2, "Alice Johnson", "alice.johnson@example.com", 11);
        Student student3 = new Student(3, "Robert Brown", "robert.brown@example.com", 10);
        Student student4 = new Student(4, "Emily Davis", "emily.davis@example.com", 12);
        Student student5 = new Student(5, "Michael Wilson", "michael.wilson@example.com", 11);
        Student student6 = new Student(6, "Sarah Anderson", "sarah.anderson@example.com", 10);

        // Add students to database
        StudentDAO studentDAO = new StudentDAO();
        studentDAO.addStudent(student1);
        studentDAO.addStudent(student2);
        studentDAO.addStudent(student3);
        studentDAO.addStudent(student4);
        studentDAO.addStudent(student5);
        studentDAO.addStudent(student6);

        System.out.println("All students in database:");
        studentDAO.getAllStudents().forEach(System.out::println);

        // Export students to file
        exportStudentsToFile(studentDAO.getAllStudents(), "students.txt");
        System.out.println("\nStudents exported to students.txt");

        // Import students from file and show them
        List<Student> importedStudents = importStudentsFromFile("students.txt");
        System.out.println("\nStudents imported from file:");
        importedStudents.forEach(System.out::println);

        // Using Streams API to filter, count and sort
        int selectedGrade = 10;
        System.out.println("\nStudents in grade " + selectedGrade + ":");
        List<Student> filteredStudents = studentDAO.getAllStudents().stream()
                .filter(s -> s.getGrade() == selectedGrade)
                .collect(Collectors.toList());
        filteredStudents.forEach(System.out::println);

        long count = studentDAO.getAllStudents().stream()
                .filter(s -> s.getGrade() == selectedGrade)
                .count();
        System.out.println("\nNumber of students in grade " + selectedGrade + ": " + count);

        System.out.println("\nStudents sorted alphabetically:");
        List<Student> sortedStudents = studentDAO.getAllStudents().stream()
                .sorted(Comparator.comparing(Student::getName))
                .collect(Collectors.toList());
        sortedStudents.forEach(System.out::println);

        // Process students concurrently using threads
        System.out.println("\nProcessing students with multiple threads:");
        List<Student> allStudents = studentDAO.getAllStudents();
        processStudentsConcurrently(allStudents);
    }

    private static void initializeDatabase() {
        try {
            DatabaseConnection.getInstance().getConnection().createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS students (" +
                            "id INT PRIMARY KEY, " +
                            "name VARCHAR(100), " +
                            "email VARCHAR(100), " +
                            "grade INT)");
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void exportStudentsToFile(List<Student> students, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Student student : students) {
                writer.println(student.getId() + "," + student.getName() + "," +
                        student.getEmail() + "," + student.getGrade());
            }
        } catch (IOException e) {
            System.err.println("Error exporting students to file: " + e.getMessage());
        }
    }

    private static List<Student> importStudentsFromFile(String filename) {
        List<Student> students = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String email = parts[2];
                    int grade = Integer.parseInt(parts[3]);
                    students.add(new Student(id, name, email, grade));
                }
            }
        } catch (IOException e) {
            System.err.println("Error importing students from file: " + e.getMessage());
        }
        return students;
    }

    private static void processStudentsConcurrently(List<Student> students) {
        int midpoint = students.size() / 2;
        List<Student> firstHalf = students.subList(0, midpoint);
        List<Student> secondHalf = students.subList(midpoint, students.size());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(() -> {
            System.out.println("\nThread 1 processing first half of students:");
            firstHalf.forEach(student ->
                    System.out.println("Thread 1: " + student));
            latch.countDown();
        });

        executor.submit(() -> {
            System.out.println("\nThread 2 processing second half of students:");
            secondHalf.forEach(student ->
                    System.out.println("Thread 2: " + student));
            latch.countDown();
        });

        try {
            latch.await(); // Wait for both threads to finish
            System.out.println("\nAll threads completed processing");
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}

// Student class to represent a student entity
class Student {
    private int id;
    private String name;
    private String email;
    private int grade;

    public Student(int id, String name, String email, int grade) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.grade = grade;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getGrade() {
        return grade;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', email='" + email + "', grade=" + grade + "}";
    }
}

// Singleton class for database connection
class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            // Load the H2 database driver
            Class.forName("org.h2.Driver");
            // Create an in-memory H2 database
            this.connection = DriverManager.getConnection("jdbc:h2:mem:studentdb", "sa", "");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}

// Data Access Object for Student entity
class StudentDAO {
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public void addStudent(Student student) {
        String sql = "INSERT INTO students (id, name, email, grade) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, student.getId());
            statement.setString(2, student.getName());
            statement.setString(3, student.getEmail());
            statement.setInt(4, student.getGrade());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
        }
    }

    public Student getStudentById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new Student(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getInt("grade")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student: " + e.getMessage());
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";
        try (Statement statement = getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                students.add(new Student(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getInt("grade")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
        }
        return students;
    }

    public void updateStudent(Student student) {
        String sql = "UPDATE students SET name = ?, email = ?, grade = ? WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, student.getName());
            statement.setString(2, student.getEmail());
            statement.setInt(3, student.getGrade());
            statement.setInt(4, student.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
        }
    }

    public void deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
        }
    }
}