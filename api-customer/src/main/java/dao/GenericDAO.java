package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericDAO<T> {
    private Connection connection;

    public GenericDAO() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/customer", "postgres", "postgres");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    protected abstract String getInsertQuery();

    protected abstract String getUpdateQuery();

    protected abstract String getDeleteQuery();

    protected abstract String getSelectAllQuery();

    protected abstract String getSelectByIdQuery();

    protected abstract void setStatementParams(PreparedStatement statement, T entity) throws SQLException;

    protected abstract T createEntity(ResultSet resultSet) throws SQLException;

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert(T entity) {
        try (PreparedStatement statement = connection.prepareStatement(getInsertQuery())) {
            setStatementParams(statement, entity);
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                System.out.println("A chave primária gerada foi: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(T entity) {
        try (PreparedStatement statement = connection.prepareStatement(getUpdateQuery())) {
            setStatementParams(statement, entity);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(T entity) {
        try (PreparedStatement statement = connection.prepareStatement(getDeleteQuery())) {
            setStatementParams(statement, entity);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<T> selectAll() {
        List<T> entities = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(getSelectAllQuery())) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                T entity = createEntity(resultSet);
                entities.add(entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entities;
    }

    public T selectById(int id) {
        T entity = null;
        try (PreparedStatement statement = connection.prepareStatement(getSelectByIdQuery())) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                entity = createEntity(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }
}
