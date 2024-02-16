# SQLite Persistence Manager

## Overview
The SQLite Persistence Manager project provides a simple yet powerful implementation for managing persistence of data 
models in a SQLite database. It specifically caters to applications requiring efficient data storage and retrieval 
mechanisms for Employee and Department objects. Through implementing the PersistenceManager interface, this project
demonstrates a practical approach to object-relational mapping (ORM) without relying on heavy ORM frameworks.

## Features
* **Generic Persistence Interface:** Utilizes a generic interface `PersistenceManager<T>` to define common CRUD 
operations, making it adaptable for different data models.
* **Model-Specific Managers:** Includes `EmployeeManager` and `DepartmentManager` classes, each implementing the 
`PersistenceManager` interface for handling operations specific to `Employee` and `Department` objects.
* **Support for Basic CRUD Operations:** Enables creating, retrieving (by id and all records), updating, and saving 
entities into the SQLite database.
* **Exception Handling:** Integrates custom exception handling through `PersistenceException` to manage errors related 
to database operations and data integrity.
* **In-Memory Database Testing:** Demonstrates how to set up an in-memory SQLite database for running unit tests, 
* ensuring that the persistence logic works as expected.

## Getting Started

### Prerequisites
* Java JDK 17 or higher
* SQLite JDBC Driver (included in the project dependencies)

### Installation
Clone this repository to your local machine to get started with the SQLite Persistence Manager project:
```commandline
git clone https://github.com/dostavic/SQLiteORMManager.git
cd sqlite-persistence-manager
```

### Running the Tests
The project comes with a suite of unit tests to verify the functionality of both `EmployeeManager` and 
`DepartmentManager`. To run these tests, execute:
```commandline
./gradlew test
```
This command compiles the project and runs all tests, outputting the results to the console.

### Usage
The project is designed to be used as a library for managing persistence of `Employee` and `Department` entities in a 
SQLite database. Here's a quick example of how to use the `DepartmentManager`:
```java
import sk.tuke.meta.motivation.DepartmentManager;
import sk.tuke.meta.motivation.Department;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:path_to_your_database.db")) {
            DepartmentManager departmentManager = new DepartmentManager(connection);

            // Create and save a new department
            Department newDepartment = new Department("Research", "R&D");
            departmentManager.save(newDepartment);

            // Retrieve a department by ID
            Department department = departmentManager.get(newDepartment.getId())
                                                     .orElseThrow(() -> new RuntimeException("Department not found"));
            
            System.out.println("Department: " + department.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
```