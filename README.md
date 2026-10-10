# Library Management System

A Java library management app with a responsive browser interface.

## Project layout

- `frontend/` - HTML, CSS, and JavaScript
- `backend/` - Java HTTP server and library logic
- `run.bat` - compiles and starts the app on Windows

## Run

Run `run.bat`, then open <http://127.0.0.1:8080> in your browser. A Java JDK is required.

The server stores books and students in `books.txt` and `students.txt` in the project root. These local data files and compiled output are excluded from Git.

## Features

- Add and search books and students
- Issue and return books
- Track current loans
- Persist records between runs
- Responsive dashboard and library views
