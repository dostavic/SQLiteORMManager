package sk.tuke.meta.motivation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentManager implements PersistenceManager<Department> {
    private final Connection connection;

    public DepartmentManager(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Department> get(long id) {
        String sql = "SELECT * FROM Department WHERE id = ?";

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Department department = new Department();
                    department.setId(rs.getLong("id"));
                    department.setName(rs.getString("name"));
                    department.setCode(rs.getString("code"));
                    return Optional.of(department);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public List<Department> getAll() {
        String sql = "SELECT * FROM Department";
        List<Department> departmentList = new ArrayList<>();

        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            try (ResultSet rs = preparedStatement.executeQuery()){
                while (rs.next()){
                    Department department = new Department();
                    department.setId(rs.getLong("id"));
                    department.setName(rs.getString("name"));
                    department.setCode(rs.getString("code"));
                    departmentList.add(department);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return departmentList;
    }

    @Override
    public void save(Department obj) {
        if (obj.getId() == 0) {
            String sql = "INSERT INTO Department (name, code) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
                preparedStatement.setString(1, obj.getName());
                preparedStatement.setString(2, obj.getCode());
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
            String sql = "UPDATE Department SET name = ?, code = ? WHERE id = ?";

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
                preparedStatement.setString(1, obj.getName());
                preparedStatement.setString(2, obj.getCode());
                preparedStatement.setLong(3, obj.getId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
