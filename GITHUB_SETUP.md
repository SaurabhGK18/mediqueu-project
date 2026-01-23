# Push to GitHub - Instructions

Your code is ready to be pushed to GitHub! Follow these steps:

## Step 1: Create a GitHub Repository

1. Go to https://github.com/new
2. Repository name: `opd-queueing-model` (or any name you prefer)
3. Description: "Hospital Management System with OPD Queueing Model - Java"
4. Choose **Public** or **Private**
5. **DO NOT** initialize with README, .gitignore, or license (we already have these)
6. Click **Create repository**

## Step 2: Push Your Code

After creating the repository, GitHub will show you commands. Use these:

```bash
cd /Users/saurabh/opd-queueing-model

# Add your GitHub repository as remote (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/opd-queueing-model.git

# Push to GitHub
git push -u origin main
```

## Alternative: Using SSH (if you have SSH keys set up)

```bash
git remote add origin git@github.com:YOUR_USERNAME/opd-queueing-model.git
git push -u origin main
```

## Step 3: Verify

After pushing, visit your repository on GitHub:
`https://github.com/YOUR_USERNAME/opd-queueing-model`

---

## Quick Commands Reference

```bash
# Check status
git status

# See what will be pushed
git log --oneline

# Push updates (after making changes)
git add .
git commit -m "Your commit message"
git push
```

## What's Included

✅ All source code (Java files)
✅ README.md with documentation
✅ pom.xml for Maven
✅ Setup scripts
✅ .gitignore (excludes compiled files, logs, etc.)

## What's Excluded (via .gitignore)

❌ Compiled .class files
❌ MongoDB driver JARs (users need to download)
❌ Log files
❌ IDE configuration files
❌ Temporary files

---

**Note:** Make sure you have Git configured with your name and email:
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```
