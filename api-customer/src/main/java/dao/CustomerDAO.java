package dao;

import model.Customer;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDAO extends GenericDAO<Customer> {


    @Override
    protected String getInsertQuery() {
        return "INSERT INTO customer (name, cpf) VALUES (?, ?)";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE customer SET name = ?, cpf = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM customer WHERE id = ?";
    }

    @Override
    protected String getSelectAllQuery() {
        return "SELECT * FROM customer";
    }

    @Override
    protected String getSelectByIdQuery() {
        return "SELECT id, name, cpf FROM users WHERE id = ?";
    }

    @Override
    protected void setStatementParams(PreparedStatement statement, Customer entity) throws SQLException {
         statement.setString(1,entity.getName());
         statement.setString(2,entity.getCpf());

    }

    @Override
    protected Customer createEntity(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setName(resultSet.getString("name"));
        customer.setCpf(resultSet.getString("cpf"));
        return customer;
    }
}
