# FamilyShield 2.6 — Java Swing + MySQL

## Included files
- `run-windows.bat` — build and launch on Windows
- `pom.xml` — Maven project and MySQL JDBC dependency
- `config/db.properties` — local database settings
- `database/schema.sql` — creates the `familyshield` database and `scam_reports` table
- `src/main/java/com/familyshield/FamilyShieldApp.java` — application source, including screenshot OCR/risk analysis

## Before first run
1. Install Java JDK 8+ and Maven 3.8+ and make sure `java -version` and `mvn -version` work in Command Prompt.
2. Open MySQL Workbench and execute `database/schema.sql`.
3. Open `config/db.properties`. Set `db.password` to your own MySQL password. The default URL uses port `3307`; if your MySQL server uses `3306`, change `localhost:3307` to `localhost:3306`.
4. Double-click `run-windows.bat`. It builds the app and starts it. The first run needs internet access so Maven can download dependencies.

## Run from Command Prompt
Open Command Prompt in this folder and run:
```bat
run-windows.bat
```

## Screenshot analysis test
Open Screenshot Scan in the app, choose a screenshot, extract its text, then analyze it. OCR quality affects results. This is an educational rule-based tool and cannot guarantee detection of every scam.

## Troubleshooting
- `mvn is not recognized`: install Maven and add its `bin` directory to PATH, then reopen Command Prompt.
- `Access denied for user root`: correct `db.user` / `db.password` in `config/db.properties`.
- MySQL port mismatch: use the port configured in your MySQL server.
