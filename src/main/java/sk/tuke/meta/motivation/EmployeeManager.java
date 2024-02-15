package sk.tuke.meta.motivation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeManager implements PersistenceManager<Employee> {
    private final Connection connection;

    public EmployeeManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Employee> get(long id) {
        String sql = "SELECT e.*, d.id AS departmentId, d.name AS departmentName, d.code AS departmentCode " +
                "FROM Employee e LEFT JOIN Department d ON e.department = d.id WHERE e.id = ?";

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Employee employee = new Employee();
                    employee.setId(resultSet.getLong("id"));
                    employee.setName(resultSet.getString("name"));
                    employee.setSurname(resultSet.getString("surname"));
                    employee.setSalary(resultSet.getInt("salary"));

                    Department department = new Department();
                    department.setId(resultSet.getLong("departmentId"));
                    department.setName(resultSet.getString("departmentName"));
                    department.setCode(resultSet.getString("departmentCode"));

                    employee.setDepartment(department);

                    return Optional.of(employee);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public List<Employee> getAll() {
        String sql = "SELECT e.*, d.id AS departmentId, d.name AS departmentName, d.code AS departmentCode " +
                "FROM Employee e LEFT JOIN Department d on e.department = d.id";
        List<Employee> employeeList = new ArrayList<>();

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Employee employee = new Employee();
                    employee.setId(resultSet.getLong("id"));
                    employee.setName(resultSet.getString("name"));
                    employee.setSurname(resultSet.getString("surname"));
                    employee.setSalary(resultSet.getInt("salary"));

                    long departmentId = resultSet.getLong("departmentId");
                    if (resultSet.wasNull()) {
                        employee.setDepartment(null);
                    } else {
                        Department department = new Department();
                        department.setId(departmentId);
                        department.setName(resultSet.getString("departmentName"));
                        department.setCode(resultSet.getString("departmentCode"));

                        employee.setDepartment(department);
                    }

                    employeeList.add(employee);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return employeeList;
    }

    @Override
    public void save(Employee obj) {
        if (obj.getId() == 0) {
            String sql = "INSERT INTO Employee (name, surname, salary, department) VALUES (?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, obj.getName());
                preparedStatement.setString(2, obj.getSurname());
                preparedStatement.setInt(3, obj.getSalary());

                if (obj.getDepartment() != null && !departmentExist(obj.getDepartment().getId())) {
                    throw new PersistenceException("Department must be saved before assigning to an employee.");
                } else if (obj.getDepartment() != null) {
                    preparedStatement.setLong(4, obj.getDepartment().getId());
                } else {
                    preparedStatement.setNull(4, Types.INTEGER);
                }

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            obj.setId(generatedKeys.getLong(1));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            String sql = "UPDATE Employee SET name = ?, surname = ?, salary = ?, department = ? WHERE id = ?";

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
                preparedStatement.setString(1, obj.getName());
                preparedStatement.setString(2, obj.getSurname());
                preparedStatement.setInt(3, obj.getSalary());

                if (obj.getDepartment() != null && !departmentExist(obj.getDepartment().getId())) {
                    throw new PersistenceException("Department must be saved before assigning to an employee.");
                } else if (obj.getDepartment() != null) {
                    preparedStatement.setLong(4, obj.getDepartment().getId());
                } else {
                    preparedStatement.setNull(4, Types.INTEGER);
                }

                preparedStatement.setLong(5, obj.getId());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean departmentExist(long departmentId) {
        String sql = "SELECT COUNT(id) FROM Department WHERE id = ?";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, departmentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
