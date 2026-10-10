import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;
import com.sun.net.httpserver.*;

// --- 1. Classes (OOP Concept) ---

// Book Class: Book ki details store karega
class Book {
    private int id;
    private String title;
    private String author;
    private boolean isAvailable;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        String status = isAvailable ? "Available" : "Issued";
        return String.format("[ID: %d] '%s' by %s - %s", id, title, author, status);
    }
}

// Student Class: Student ki details aur issued books store karega
class Student {
    private int id;
    private String name;
    private List<Integer> issuedBooks; // List of Book IDs

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
        this.issuedBooks = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public List<Integer> getIssuedBooks() { return issuedBooks; }

    @Override
    public String toString() {
        String books = issuedBooks.isEmpty() ? "None" : issuedBooks.toString();
        return String.format("[ID: %d] %s - Issued Books: %s", id, name, books);
    }
}

// --- 2. Library Management System (Main Logic) ---

class LibraryManagementSystem {
    private List<Book> books;
    private List<Student> students;
    private Scanner scanner;
    
    // File names for persistence
    private static final String BOOK_FILE = "books.txt";
    private static final String STUDENT_FILE = "students.txt";

    public LibraryManagementSystem() {
        books = new ArrayList<>();
        students = new ArrayList<>();
        scanner = new Scanner(System.in);
        loadData(); // Load data on startup
    }

    // --- File Handling (Save Data) ---
    private void saveData() {
        try {
            // Save Books
            PrintWriter bookWriter = new PrintWriter(new FileWriter(BOOK_FILE));
            for (Book book : books) {
                bookWriter.println(book.getId() + "," + encodeField(book.getTitle()) + "," + encodeField(book.getAuthor()) + "," + (book.isAvailable() ? "1" : "0"));
            }
            bookWriter.close();

            // Save Students
            PrintWriter studentWriter = new PrintWriter(new FileWriter(STUDENT_FILE));
            for (Student student : students) {
                String bookIds = String.join(",", student.getIssuedBooks().stream().map(String::valueOf).toArray(String[]::new));
                studentWriter.println(student.getId() + "," + encodeField(student.getName()) + "," + bookIds);
            }
            studentWriter.close();
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // --- File Handling (Load Data) ---
    private void loadData() {
        // Load Books
        if (new File(BOOK_FILE).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(BOOK_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length == 4) {
                        int id = Integer.parseInt(parts[0]);
                        String title = decodeField(parts[1]);
                        String author = decodeField(parts[2]);
                        boolean available = parts[3].equals("1");
                        Book book = new Book(id, title, author);
                        book.setAvailable(available);
                        books.add(book);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading books: " + e.getMessage());
            }
        }

        // Load Students
        if (new File(STUDENT_FILE).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 3) {
                        int id = Integer.parseInt(parts[0]);
                        String name = decodeField(parts[1]);
                        Student student = new Student(id, name);
                        if (!parts[2].isEmpty()) {
                            String[] bookIds = parts[2].split(",");
                            for (String idStr : bookIds) {
                                student.getIssuedBooks().add(Integer.parseInt(idStr));
                            }
                        }
                        students.add(student);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading students: " + e.getMessage());
            }
        }
    }

    // --- Feature: Add Book ---
    public void addBook() {
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author Name: ");
        String author = scanner.nextLine();

        int newId = 1;
        if (!books.isEmpty()) {
            newId = books.get(books.size() - 1).getId() + 1;
        }

        Book newBook = new Book(newId, title, author);
        books.add(newBook);
        saveData();
        System.out.println("Book Added Successfully! ID: " + newId);
    }

    // --- Feature: Add Student ---
    public void addStudent() {
        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine();

        int newId = 1;
        if (!students.isEmpty()) {
            newId = students.get(students.size() - 1).getId() + 1;
        }

        Student newStudent = new Student(newId, name);
        students.add(newStudent);
        saveData();
        System.out.println("Student Added Successfully! ID: " + newId);
    }

    // --- Feature: Issue Book ---
    public void issueBook() {
        System.out.print("Enter Book ID to Issue: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter Student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Book book = findBook(bookId);
        Student student = findStudent(studentId);

        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        if (student == null) {
            System.out.println("Student not found!");
            return;
        }
        if (!book.isAvailable()) {
            System.out.println("Book is already issued!");
            return;
        }

        book.setAvailable(false);
        student.getIssuedBooks().add(bookId);
        saveData();
        System.out.println("Book '" + book.getTitle() + "' issued to " + student.getName());
    }

    // --- Feature: Return Book ---
    public void returnBook() {
        System.out.print("Enter Book ID to Return: ");
        int bookId = scanner.nextInt();
        System.out.print("Enter Student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Book book = findBook(bookId);
        Student student = findStudent(studentId);

        if (book == null) {
            System.out.println("Book not found!");
            return;
        }
        if (student == null) {
            System.out.println("Student not found!");
            return;
        }
        if (!student.getIssuedBooks().contains(bookId)) {
            System.out.println("This student does not have this book!");
            return;
        }

        book.setAvailable(true);
        student.getIssuedBooks().remove(Integer.valueOf(bookId));
        saveData();
        System.out.println("Book '" + book.getTitle() + "' returned successfully.");
    }

    private static String encodeField(String value) {
        return "b64:" + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeField(String value) {
        if (!value.startsWith("b64:")) return value;
        try { return new String(Base64.getDecoder().decode(value.substring(4)), StandardCharsets.UTF_8); }
        catch (IllegalArgumentException e) { return value; }
    }

    public synchronized List<Book> getBooks() { return new ArrayList<>(books); }
    public synchronized List<Student> getStudents() { return new ArrayList<>(students); }

    public synchronized Book addBookRecord(String title, String author) {
        if (title == null || title.trim().isEmpty() || author == null || author.trim().isEmpty())
            throw new IllegalArgumentException("Title and author are required.");
        int id = books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
        Book book = new Book(id, title.trim(), author.trim());
        books.add(book);
        saveData();
        return book;
    }

    public synchronized Student addStudentRecord(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Student name is required.");
        int id = students.stream().mapToInt(Student::getId).max().orElse(0) + 1;
        Student student = new Student(id, name.trim());
        students.add(student);
        saveData();
        return student;
    }

    public synchronized void issueBookRecord(int bookId, int studentId) {
        Book book = findBook(bookId);
        Student student = findStudent(studentId);
        if (book == null) throw new IllegalArgumentException("Book not found.");
        if (student == null) throw new IllegalArgumentException("Student not found.");
        if (!book.isAvailable()) throw new IllegalArgumentException("Book is already on loan.");
        book.setAvailable(false);
        student.getIssuedBooks().add(bookId);
        saveData();
    }

    public synchronized void returnBookRecord(int bookId, int studentId) {
        Book book = findBook(bookId);
        Student student = findStudent(studentId);
        if (book == null) throw new IllegalArgumentException("Book not found.");
        if (student == null) throw new IllegalArgumentException("Student not found.");
        if (!student.getIssuedBooks().contains(bookId))
            throw new IllegalArgumentException("This student does not have this book.");
        book.setAvailable(true);
        student.getIssuedBooks().remove(Integer.valueOf(bookId));
        saveData();
    }

    private static String jsonEscape(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default: if (c < 32) out.append(String.format("\\u%04x", (int)c)); else out.append(c);
            }
        }
        return out.append('"').toString();
    }

    private static String booksJson(LibraryManagementSystem lib) {
        StringBuilder out = new StringBuilder("[");
        List<Book> all = lib.getBooks();
        for (int i = 0; i < all.size(); i++) {
            Book b = all.get(i);
            if (i > 0) out.append(',');
            int studentId = 0;
            for (Student st : lib.getStudents()) if (st.getIssuedBooks().contains(b.getId())) { studentId = st.getId(); break; }
            out.append("{\"id\":").append(b.getId()).append(",\"title\":").append(jsonEscape(b.getTitle()))
               .append(",\"author\":").append(jsonEscape(b.getAuthor())).append(",\"available\":").append(b.isAvailable())
               .append(",\"studentId\":").append(studentId == 0 ? "null" : Integer.toString(studentId)).append('}');
        }
        return out.append(']').toString();
    }

    private static String loansJson(LibraryManagementSystem lib) {
        StringBuilder out = new StringBuilder("[");
        boolean first = true;
        for (Book b : lib.getBooks()) {
            if (b.isAvailable()) continue;
            if (!first) out.append(',');
            first = false;
            int studentId = 0;
            for (Student st : lib.getStudents()) if (st.getIssuedBooks().contains(b.getId())) { studentId = st.getId(); break; }
            out.append("{\"id\":").append(b.getId()).append(",\"title\":").append(jsonEscape(b.getTitle()))
               .append(",\"author\":").append(jsonEscape(b.getAuthor())).append(",\"available\":false")
               .append(",\"studentId\":").append(studentId == 0 ? "null" : Integer.toString(studentId)).append('}');
        }
        return out.append(']').toString();
    }

    private static String studentsJson(LibraryManagementSystem lib) {
        StringBuilder out = new StringBuilder("[");
        List<Student> all = lib.getStudents();
        for (int i = 0; i < all.size(); i++) {
            Student st = all.get(i);
            if (i > 0) out.append(',');
            out.append("{\"id\":").append(st.getId()).append(",\"name\":").append(jsonEscape(st.getName())).append(",\"issuedBooks\":[");
            for (int j = 0; j < st.getIssuedBooks().size(); j++) { if (j > 0) out.append(','); out.append(st.getIssuedBooks().get(j)); }
            out.append("]}");
        }
        return out.append(']').toString();
    }

    private static String jsonStringField(String json, String key) {
        Matcher m = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"").matcher(json);
        if (!m.find()) throw new IllegalArgumentException("Missing field: " + key);
        String raw = m.group(1);
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c != '\\' || i + 1 >= raw.length()) { value.append(c); continue; }
            char e = raw.charAt(++i);
            switch (e) {
                case '"': value.append('"'); break; case '\\': value.append('\\'); break; case '/': value.append('/'); break;
                case 'b': value.append('\b'); break; case 'f': value.append('\f'); break; case 'n': value.append('\n'); break;
                case 'r': value.append('\r'); break; case 't': value.append('\t'); break;
                case 'u': if (i + 4 >= raw.length()) throw new IllegalArgumentException("Invalid JSON escape.");
                    value.append((char)Integer.parseInt(raw.substring(i + 1, i + 5), 16)); i += 4; break;
                default: throw new IllegalArgumentException("Invalid JSON escape.");
            }
        }
        return value.toString();
    }

    private static int jsonIntField(String json, String key) {
        Matcher m = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (!m.find()) throw new IllegalArgumentException("Missing field: " + key);
        return Integer.parseInt(m.group(1));
    }

    private static String requestBody(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = exchange.getRequestBody().read(buf)) != -1) {
            if (out.size() + n > 65536) throw new IllegalArgumentException("Request is too large.");
            out.write(buf, 0, n);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange exchange, int code, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(code, body.length);
        try (OutputStream out = exchange.getResponseBody()) { out.write(body); }
    }

    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        send(exchange, code, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    private static void handleApi(LibraryManagementSystem lib, HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        try {
            if (method.equals("GET") && path.equals("/api/books")) { sendJson(exchange, 200, booksJson(lib)); return; }
            if (method.equals("GET") && path.equals("/api/students")) { sendJson(exchange, 200, studentsJson(lib)); return; }
            if (method.equals("GET") && path.equals("/api/loans")) { sendJson(exchange, 200, loansJson(lib)); return; }
            if (method.equals("POST")) {
                String body = requestBody(exchange);
                if (path.equals("/api/books")) { Book b = lib.addBookRecord(jsonStringField(body, "title"), jsonStringField(body, "author")); sendJson(exchange, 201, "{\"id\":" + b.getId() + "}"); return; }
                if (path.equals("/api/students")) { Student st = lib.addStudentRecord(jsonStringField(body, "name")); sendJson(exchange, 201, "{\"id\":" + st.getId() + "}"); return; }
                if (path.equals("/api/loans")) { lib.issueBookRecord(jsonIntField(body, "bookId"), jsonIntField(body, "studentId")); sendJson(exchange, 200, "{\"ok\":true}"); return; }
                if (path.equals("/api/returns")) { lib.returnBookRecord(jsonIntField(body, "bookId"), jsonIntField(body, "studentId")); sendJson(exchange, 200, "{\"ok\":true}"); return; }
            }
            sendJson(exchange, 404, "{\"error\":\"API route not found.\"}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":" + jsonEscape(e.getMessage()) + "}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"The server could not complete this request.\"}");
        }
    }

    private void startWebServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/api/", exchange -> handleApi(this, exchange));
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (!(path.equals("/") || path.equals("/index.html"))) {
                send(exchange, 404, "text/plain; charset=utf-8", "Not found".getBytes(StandardCharsets.UTF_8)); return;
            }
            try {
                byte[] html = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("index.html"));
                send(exchange, 200, "text/html; charset=utf-8", html);
            } catch (IOException e) {
                send(exchange, 500, "text/plain; charset=utf-8", "Could not read index.html".getBytes(StandardCharsets.UTF_8));
            }
        });
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Library is ready at http://127.0.0.1:8080");
    }

    // Helper Methods
    private Book findBook(int id) {
        for (Book b : books) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    private Student findStudent(int id) {
        for (Student s : students) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public void displayBooks() {
        System.out.println("\n--- All Books ---");
        if (books.isEmpty()) System.out.println("No books available.");
        for (Book b : books) System.out.println(b);
    }

    public void displayStudents() {
        System.out.println("\n--- All Students ---");
        if (students.isEmpty()) System.out.println("No students registered.");
        for (Student s : students) System.out.println(s);
    }

    // --- Main Menu ---
    public void start() {
        while (true) {
            System.out.println("\n=== Library Management System ===");
            System.out.println("1. Add Book");
            System.out.println("2. Add Student");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. View All Books");
            System.out.println("6. View All Students");
            System.out.println("7. Exit");
            System.out.print("Enter Choice (1-7): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: addBook(); break;
                case 2: addStudent(); break;
                case 3: issueBook(); break;
                case 4: returnBook(); break;
                case 5: displayBooks(); break;
                case 6: displayStudents(); break;
                case 7: 
                    System.out.println("Exiting System...");
                    return;
                default: System.out.println("Invalid Choice!");
            }
        }
    }

    // Entry Point
    public static void main(String[] args) {
        LibraryManagementSystem library = new LibraryManagementSystem();
        try {
            if (args.length > 0 && args[0].equals("--console")) library.start();
            else library.startWebServer();
        } catch (IOException e) {
            System.err.println("Could not start the library server: " + e.getMessage());
        }
    }
}