# MongoDB Driver Setup

This Java project requires the MongoDB Java driver to connect to the database.

## Download MongoDB Java Driver

1. Download the MongoDB Java driver JAR file from:
   https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/

   Or use the latest version:
   https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.12.14/mongo-java-driver-3.12.14.jar

2. Also download the BSON library:
   https://repo1.maven.org/maven2/org/mongodb/bson/4.11.1/bson-4.11.1.jar

   And the MongoDB driver core:
   https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.11.1/mongodb-driver-core-4.11.1.jar

## Setup Instructions

### Option 1: Using Maven (Recommended)

Create a `pom.xml` file in the project root:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.hms</groupId>
    <artifactId>hospital-management-system</artifactId>
    <version>1.0</version>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>mongodb-driver-sync</artifactId>
            <version>4.11.1</version>
        </dependency>
    </dependencies>
</project>
```

Then compile with:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="Main"
```

### Option 2: Manual JAR Setup

1. Create a `lib` directory in the project root
2. Download the JAR files mentioned above and place them in `lib/`
3. Compile with classpath:

```bash
javac -cp "lib/*:out" -d out src/main/java/**/*.java
java -cp "lib/*:out" Main
```

### Option 3: Using Gradle

Create a `build.gradle` file:

```gradle
plugins {
    id 'java'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.mongodb:mongodb-driver-sync:4.11.1'
}
```

Then:
```bash
gradle build
gradle run
```

## Note

The MongoDB connection URI in the code connects to:
- URI: `mongodb+srv://manasranjanpradhan2004:root@hms.m7j9t.mongodb.net/`
- Database: `HMS`

Make sure you have network access to this MongoDB instance, or update the URI in `DatabaseConnection.java` to point to your own MongoDB instance.
