package com.tdt.springbatch.config;

import com.tdt.springbatch.entity.Order;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderRowMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Order(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("stock_code"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                rs.getDouble("total"),
                rs.getDate("order_date")
        );
    }
}
