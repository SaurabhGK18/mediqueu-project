# How to Open This Project in VS Code

## Method 1: Open Folder Directly

1. **Open VS Code**
2. **File → Open Folder** (or `Cmd+O` on Mac, `Ctrl+O` on Windows/Linux)
3. **Navigate to**: `/Users/saurabh/opd-queueing-model`
4. **Click "Open"**

## Method 2: Using Command Line

1. **Open Terminal**
2. **Navigate to project directory**:
   ```bash
   cd /Users/saurabh/opd-queueing-model
   ```
3. **Open in VS Code**:
   ```bash
   code .
   ```
   (If `code` command doesn't work, install VS Code command line tools)

## Method 3: Drag and Drop

1. **Open Finder** (Mac) or **File Explorer** (Windows)
2. **Navigate to**: `/Users/saurabh/opd-queueing-model`
3. **Drag the folder** onto VS Code icon in your dock/taskbar

## After Opening in VS Code

### Install Recommended Extensions

VS Code will prompt you to install:
- **Extension Pack for Java** (by Microsoft)
- **Language Support for Java** (by Red Hat)

### Setup MongoDB Driver

1. The MongoDB driver JARs should already be in the `lib/` folder
2. If not, download them (see `MONGODB_SETUP.md`)

### Run the Project

**Option 1: Using VS Code Run Button**
1. Open `Main.java`
2. Click the "Run" button above the `main` method
3. Or press `F5` to debug

**Option 2: Using Terminal in VS Code**
1. Open integrated terminal: `` Ctrl+` `` (backtick)
2. Run:
   ```bash
   javac -cp "lib/*" -d out src/main/java/**/*.java
   java -cp "lib/*:out" Main
   ```

**Option 3: Using Tasks**
1. Press `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type "Tasks: Run Task"
3. Select "compile" to compile, or "run" to compile and run

## Project Structure in VS Code

```
opd-queueing-model/
├── .vscode/              # VS Code configuration
│   ├── settings.json    # Java settings
│   ├── launch.json      # Debug configuration
│   └── tasks.json       # Build tasks
├── src/main/java/       # Java source code
│   ├── db/              # Database connection
│   ├── auth/            # Authentication
│   ├── service/         # Business logic
│   ├── web/             # Web server
│   └── Main.java        # Entry point
├── lib/                 # MongoDB driver JARs
├── out/                 # Compiled classes
├── pom.xml              # Maven configuration
└── README.md            # Documentation
```

## Troubleshooting

**Issue: Java extension not working**
- Install "Extension Pack for Java" from VS Code marketplace
- Reload VS Code window

**Issue: Cannot find MongoDB classes**
- Ensure `lib/` folder contains MongoDB JAR files
- Check `.vscode/settings.json` has correct library paths

**Issue: Port 8080 already in use**
- Stop any running server instances
- Or change port in `Main.java`

## Quick Start Commands

```bash
# Open in VS Code
cd /Users/saurabh/opd-queueing-model
code .

# Compile
javac -cp "lib/*" -d out src/main/java/**/*.java

# Run
java -cp "lib/*:out" Main

# Or use Maven
mvn compile
mvn exec:java -Dexec.mainClass="Main"
```
