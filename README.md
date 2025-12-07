Stark ERP System 🎓
A Comprehensive Online Examination & Academic Management Solution

📖 Overview
Stark ERP System is a robust desktop application designed to digitize and streamline the academic processes of educational institutions. It replaces manual paperwork and disparate software tools with a single, unified platform.

The system integrates Online Examinations, Student Information, Fee Management, Attendance Tracking, and Faculty Administration. Built with JavaFX for a modern UI and MySQL for secure data storage, it features a unique Anti-Cheating Mechanism that ensures exam integrity by detecting window focus loss.

✨ Key Features
👨‍🎓 Student Module
Secure Online Exams: Take objective exams in a distraction-free environment.

Anti-Cheating System: Auto-terminates exam if the user switches tabs (Focus Loss Detection).

Instant Results: View scores immediately after submission.

Dashboard: Access profile, fee status, and exam history.

👨‍🏫 Faculty (Teacher) Module
Exam Scheduling: Create and schedule exams for different subjects.

Question Bank: Upload, edit, and manage questions.

Automated Grading: No manual correction required for objective tests.

Class Reports: View and export student performance data.

👮‍♂️ Admin Module
User Management: Add/Remove Students and Teachers.

Fee Management: Track paid/pending fees and block exam access for defaulters.

System Reports: Generate attendance and fee reports.

Global Notices: Post announcements for all users.

🛠️ Technology Stack
Programming Language: Java (JDK 17+)

Frontend: JavaFX (FXML)

Backend Database: MySQL 8.0

Connectivity: JDBC

Build Tool: Maven

IDE: IntelliJ IDEA

🚀 Installation & Setup
Prerequisites
Java JDK 17 or higher installed.

MySQL Server installed and running.

Maven (optional, if not using IntelliJ).

Step 1: Database Setup
Open MySQL Workbench.

Create a new schema named exam_system.

Run the SQL script provided in database_schema.sql to create tables.

sql
CREATE DATABASE exam_system;
USE exam_system;
-- (Paste content from database_schema.sql)
Step 2: Configure Connection
Open src/main/java/com/stark/exam/db/DBConnection.java and update your MySQL credentials:

java
Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/exam_system", "root", "YOUR_PASSWORD");
Step 3: Run the Application
Option A: Using IntelliJ IDEA

Open the project in IntelliJ.

Reload Maven Project (Right-click pom.xml > Maven > Reload Project).

Run src/main/java/com/stark/exam/AppLauncher.java.

Option B: Using Demo Launcher (For Testing)
Run src/main/java/com/stark/exam/DemoLauncher.java to instantly open 3 windows (Student, Teacher, Admin) side-by-side.


🛡️ Security Features
The system implements a 3-Strike Rule for online exams:

Warning 1: User switches tab -> "Security Alert" popup.

Warning 2: User minimizes window -> "Final Warning".

Warning 3: Exam Auto-Terminated & Submitted.

👥 Contributors
K. Sri Chaitanya – Lead Developer & UI/UX Designer
S. Dhanu – Database Design & Optimization


📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

🎉 Acknowledgements
Special thanks to our project guide R. Laxmana Rao and the Department of Computer Science at Centurion University for their support.
