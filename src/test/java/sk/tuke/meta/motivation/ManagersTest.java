package sk.tuke.meta.motivation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    private Connection connection;
    private EmployeeManager employeeManager;
    private DepartmentManager departmentManager;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        executeSqlScript(ClassLoader.getSystemResourceAsStream("sk/tuke/meta/motivation/fixture.sql"));
        employeeManager = new EmployeeManager(connection);
        departmentManager = new DepartmentManager(connection);
    }

    @Test
    void getDepartment() {
        Optional<Department> result = departmentManager.get(1);
        assertTrue(result.isPresent());
        Department devDepartment = result.get();
        assertDepartmentValue(devDepartment, 1, "Development", "DEV");
    }

    @Test
    void missingDepartment() {
        Optional<Department> result = departmentManager.get(404);
        assertFalse(result.isPresent());
    }

    @Test
    void getAllDepartments() {
        List<Department> departments = departmentManager.getAll();
        assertEquals(2, departments.size());
        assertDepartmentValue(departments.get(0), 1, "Development", "DEV");
        assertDepartmentValue(departments.get(1), 2, "Operations", "OPS");
    }

    @Test
    void getNoDepartments() throws SQLException {
        connection.prepareStatement("delete from Department").executeUpdate();
        List<Department> departments = departmentManager.getAll();
        assertEquals(List.of(), departments);
    }

    @Test
    void saveNewDepartment() throws SQLException {
        var department = new Department("Marketing", "MRK");
        departmentManager.save(department);
        assertSqlHasResult("select * from Department where id=3 and name='Marketing' and code='MRK'");
    }

    @Test
    void saveNewDepartmentSetsId() {
        var department = new Department("Marketing", "MRK");
        departmentManager.save(department);
        assertEquals(3, department.getId());
    }

    @Test
    void saveUpdatedDepartment() throws SQLException {
        Optional<Department> result = departmentManager.get(1);
        var department = result.get();
        department.setName("Engineering");
        department.setCode("ENG");
        departmentManager.save(department);
        assertSqlHasResult("select * from Department where id=1 and name='Engineering' and code='ENG'");
    }

    @Test
    void getEmployee() {
        Optional<Employee> result = employeeManager.get(1);
        assertTrue(result.isPresent());
        Employee employee = result.get();
        assertEmployeeValue(employee, 1, "Janko", "Hrasko", 1000);
    }

    @Test
    void missingEmployee() {
        Optional<Employee> result = employeeManager.get(404);
        assertFalse(result.isPresent());
    }

    @Test
    void getEmployeeDepartment() {
        Optional<Employee> result = employeeManager.get(1);
        Employee employee = result.get();
        assertNotNull(employee.getDepartment());
        assertDepartmentValue(employee.getDepartment(),
                1, "Development", "DEV");
    }

    @Test
    void getAllEmployees() {
        List<Employee> employees = employeeManager.getAll();
        assertEquals(2, employees.size());
        assertEmployeeValue(employees.get(0), 1, "Janko", "Hrasko", 1000);
        assertEmployeeValue(employees.get(1), 2, "Jozko", "Mrkvicka", 1200);
    }

    @Test
    void getNoEmployees() throws SQLException {
        connection.prepareStatement("delete from Employee").executeUpdate();
        List<Employee> employees = employeeManager.getAll();
        assertEquals(List.of(), employees);
    }

    @Test
    void getAllEmployeesLoadsDepartments() {
        List<Employee> employees = employeeManager.getAll();
        assertDepartmentValue(employees.get(0).getDepartment(),
                1, "Development", "DEV");
        assertNull(employees.get(1).getDepartment());
    }

    @Test
    void saveNewEmployee() throws SQLException {
        var employee = new Employee("Ferko", "Kapustka", 2000);
        employee.setDepartment(departmentManager.get(1).get());
        employeeManager.save(employee);
        String sql = "select * from Employee where id=3" +
                " and name='Ferko' and surname='Kapustka' and salary=2000 and department=1";
        assertSqlHasResult(sql);
    }

    @Test
    void saveNewEmployeeSetsId() {
        var employee = new Employee("Ferko", "Kapustka", 2000);
        employee.setDepartment(departmentManager.get(1).get());
        employeeManager.save(employee);
        assertEquals(3, employee.getId());
    }

    @Test
    void saveNewEmployeeWithNewDepartment() throws SQLException {
        var employee = new Employee("Ferko", "Kapustka", 2000);
        var department = new Department("Marketing", "MRK");
        employee.setDepartment(department);
        departmentManager.save(department);
        employeeManager.save(employee);
        assertSqlHasResult("select * from Employee where id=3 and" +
                " name='Ferko' and surname='Kapustka' and salary=2000 and department=3");
        assertSqlHasResult("select * from Department where id=3 " +
                "and name='Marketing' and code='MRK'");
    }

    @Test
    void saveNewEmployeeWithUnsavedDepartmentThrowsException() throws SQLException {
        var employee = new Employee("Ferko", "Kapustka", 2000);
        employee.setDepartment(new Department("Marketing", "MRK"));
        assertThrows(PersistenceException.class, () -> employeeManager.save(employee));
    }

    @Test
    void saveNewEmployeeWithoutDepartment() throws SQLException {
        var employee = new Employee("Ferko", "Kapustka", 2000);
        employeeManager.save(employee);
        String sql = "select * from Employee where id=3" +
                " and name='Ferko' and surname='Kapustka' and salary=2000 and department is null";
        assertSqlHasResult(sql);
    }

    @Test
    void saveUpdatedEmployee() throws SQLException {
        var employee = employeeManager.get(1).get();
        employee.setSalary(1500);
        employeeManager.save(employee);
        String sql = "select * from Employee where id=1" +
                " and name='Janko' and surname='Hrasko' and salary=1500 and department=1";
        assertSqlHasResult(sql);
    }

    private void assertDepartmentValue(
            Department devDepartment, int id, String name, String code) {
        assertEquals(id, devDepartment.getId());
        assertEquals(name, devDepartment.getName());
        assertEquals(code, devDepartment.getCode());
    }

    private void assertEmployeeValue(
            Employee employee, int id, String name, String surname, int salary) {
        assertEquals(id, employee.getId());
        assertEquals(name, employee.getName());
        assertEquals(surname, employee.getSurname());
        assertEquals(salary, employee.getSalary());
    }

    private void assertSqlHasResult(String sql) throws SQLException {
        var statement = connection.prepareStatement(sql);
        assertTrue(statement.executeQuery().next());
    }

    private void executeSqlScript(InputStream stream) throws SQLException {
        var statement = connection.createStatement();
        var scanner = new Scanner(stream);
        scanner.useDelimiter(";");
        while (scanner.hasNext()) {
            String statementString = scanner.next().trim();
            if (!statementString.isEmpty()) {
                statement.addBatch(statementString);
            }
        }
        statement.executeBatch();
    }
}
