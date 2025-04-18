// Main class that runs the Student Management System with a menu interface
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class StudentManagementSystem {
    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentDAO studentDAO = new StudentDAO();

    public static void main(String[] args) {
        // Initialize the database
        initializeDatabase();

        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    addNewStudent();
                    break;
                case 2:
                    displayAllStudents();
                    break;
                case 3:
                    findStudentById();
                    break;
                case 4:
                    updateStudentInfo();
                    break;
                case 5:
                    deleteStudentRecord();
                    break;
                case 6:
                    exportToFile();
                    break;
                case 7:
                    importFromFile();
                    break;
                case 8:
                    filterByGrade();
                    break;
                case 9:
                    sortStudentsAlphabetically();
                    break;
                case 10:
                    processConcurrently();
                    break;
                case 11:
                    System.out.println("Thank you for using the Student Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            // Add a pause before showing the menu again
            if (running) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }

        // Close the scanner when done
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n========== STUDENT MANAGEMENT SYSTEM ==========");
        System.out.println("1. Add New Student");
        System.out.println("2. Display All Students");
        System.out.println("3. Find Student by ID");
        System.out.println("4. Update Student Information");
        System.out.println("5. Delete Student Record");
        System.out.println("6. Export Students to File");
        System.out.println("7. Import Students from File");
        System.out.println("8. Filter Students by Grade");
        System.out.println("9. Sort Students Alphabetically");
        System.out.println("10. Process Students Concurrently (Thread Demo)");
        System.out.println("11. Exit");
        System.out.print("Enter your choice (1-11): ");
    }

    private static int getUserChoice() {
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            return choice;
        } catch (NumberFormatException e) {
            return -1; // Invalid choice
        }
    }

    private static void addNewStudent() {
        System.out.println("\n----- ADD NEW STUDENT -----");

        System.out.print("Enter student ID: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
            return;
        }

        // Check if ID already exists
        if (studentDAO.getStudentById(id) != null) {
            System.out.println("A student with this ID already exists.");
            return;
        }

        System.out.print("Enter student name: ");
        String name = scanner.nextLine();

        System.out.print("Enter student email: ");
        String email = scanner.nextLine();

        System.out.print("Enter student grade: ");
        int grade;
        try {
            grade = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid grade format. Please enter a number.");
            return;
        }

        Student student = new Student(id, name, email, grade);
        studentDAO.addStudent(student);
        System.out.println("Student added successfully!");
    }

    private static void displayAllStudents() {
        System.out.println("\n----- ALL STUDENTS -----");
        List<Student> students = studentDAO.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students found in the database.");
        } else {
            for (Student student : students) {
                System.out.println(student);
            }
        }
    }

    private static void findStudentById() {
        System.out.println("\n----- FIND STUDENT BY ID -----");
        System.out.print("Enter student ID: ");

        try {
            int id = Integer.parseInt(scanner.nextLine());
            Student student = studentDAO.getStudentById(id);

            if (student != null) {
                System.out.println("Student found:");
                System.out.println(student);
            } else {
                System.out.println("No student found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        }
    }

    private static void updateStudentInfo() {
        System.out.println("\n----- UPDATE STUDENT INFORMATION -----");
        System.out.print("Enter student ID to update: ");

        try {
            int id = Integer.parseInt(scanner.nextLine());
            Student student = studentDAO.getStudentById(id);

            if (student != null) {
                System.out.println("Current student information:");
                System.out.println(student);

                System.out.print("Enter new name (or press Enter to keep current): ");
                String name = scanner.nextLine();
                name = name.isEmpty() ? student.getName() : name;

                System.out.print("Enter new email (or press Enter to keep current): ");
                String email = scanner.nextLine();
                email = email.isEmpty() ? student.getEmail() : email;

                System.out.print("Enter new grade (or press Enter to keep current): ");
                String gradeStr = scanner.nextLine();
                int grade = gradeStr.isEmpty() ? student.getGrade() : Integer.parseInt(gradeStr);

                Student updatedStudent = new Student(id, name, email, grade);
                studentDAO.updateStudent(updatedStudent);
                System.out.println("Student information updated successfully!");
            } else {
                System.out.println("No student found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input format. Please enter valid numbers.");
        }
    }

    private static void deleteStudentRecord() {
        System.out.println("\n----- DELETE STUDENT RECORD -----");
        System.out.print("Enter student ID to delete: ");

        try {
            int id = Integer.parseInt(scanner.nextLine());
            Student student = studentDAO.getStudentById(id);

            if (student != null) {
                System.out.println("Student to delete:");
                System.out.println(student);

                System.out.print("Are you sure you want to delete this student? (y/n): ");
                String confirmation = scanner.nextLine().toLowerCase();

                if (confirmation.equals("y") || confirmation.equals("yes")) {
                    studentDAO.deleteStudent(id);
                    System.out.println("Student deleted successfully!");
                } else {
                    System.out.println("Deletion cancelled.");
                }
            } else {
                System.out.println("No student found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        }
    }

    private static void exportToFile() {
        System.out.println("\n----- EXPORT STUDENTS TO FILE -----");
        System.out.print("Enter filename (default: students.txt): ");
        String filename = scanner.nextLine();

        if (filename.isEmpty()) {
            filename = "students.txt";
        }

        List<Student> students = studentDAO.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students to export.");
            return;
        }

        exportStudentsToFile(students, filename);
        System.out.println("Students exported to " + filename + " successfully!");
    }

    private static void importFromFile() {
        System.out.println("\n----- IMPORT STUDENTS FROM FILE -----");
        System.out.print("Enter filename to import from (default: students.txt): ");
        String filename = scanner.nextLine();

        if (filename.isEmpty()) {
            filename = "students.txt";
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found: " + filename);
            return;
        }

        List<Student> importedStudents = importStudentsFromFile(filename);
        System.out.println("Imported " + importedStudents.size() + " students from file.");

        System.out.print("Would you like to add these students to the database? (y/n): ");
        String confirmation = scanner.nextLine().toLowerCase();

        if (confirmation.equals("y") || confirmation.equals("yes")) {
            for (Student student : importedStudents) {
                if (studentDAO.getStudentById(student.getId()) == null) {
                    studentDAO.addStudent(student);
                    System.out.println("Added: " + student);
                } else {
                    System.out.println("Skipped (ID exists): " + student);
                }
            }
            System.out.println("Import completed!");
        } else {
            System.out.println("Import cancelled.");
        }
    }

    private static void filterByGrade() {
        System.out.println("\n----- FILTER STUDENTS BY GRADE -----");
        System.out.print("Enter grade to filter by: ");

        try {
            int grade = Integer.parseInt(scanner.nextLine());

            List<Student> filteredStudents = studentDAO.getAllStudents().stream()
                    .filter(s -> s.getGrade() == grade)
                    .collect(Collectors.toList());

            long count = filteredStudents.size();

            System.out.println("\nStudents in grade " + grade + " (" + count + " students):");
            if (filteredStudents.isEmpty()) {
                System.out.println("No students found in grade " + grade);
            } else {
                filteredStudents.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid grade format. Please enter a number.");
        }
    }

    private static void sortStudentsAlphabetically() {
        System.out.println("\n----- STUDENTS SORTED ALPHABETICALLY -----");

        List<Student> sortedStudents = studentDAO.getAllStudents().stream()
                .sorted(Comparator.comparing(Student::getName))
                .collect(Collectors.toList());

        if (sortedStudents.isEmpty()) {
            System.out.println("No students to sort.");
        } else {
            sortedStudents.forEach(System.out::println);
        }
    }

    private static void processConcurrently() {
        System.out.println("\n----- PROCESSING STUDENTS CONCURRENTLY -----");

        List<Student> students = studentDAO.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students to process.");
            return;
        }

        processStudentsConcurrently(students);
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