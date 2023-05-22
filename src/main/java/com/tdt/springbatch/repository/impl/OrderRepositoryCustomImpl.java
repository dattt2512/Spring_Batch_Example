package com.tdt.springbatch.repository.impl;

import com.tdt.springbatch.repository.OrderRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    @Autowired
    private DataSource dataSource;

    @Override
    @Async
    public int[] batchInsert(int recordsStart, int recordsStop) {

        PreparedStatement preparedStatement;

        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(true);

            String compiledQuery = "INSERT INTO orders(id, code, stock_code, price, quantity, total, order_date)" +
                    " VALUES" + "(?, ?, ?, ?, ?, ?, ?)";

            preparedStatement = conn.prepareStatement(compiledQuery);
            preparedStatement.setFetchSize(1000);
            preparedStatement.setFetchDirection(ResultSet.FETCH_UNKNOWN);

            int batchTotal=0;
            for (int i = recordsStart; i < recordsStop ; i++) {
                preparedStatement.setLong(1, i+1);
                preparedStatement.setString(2, "A" + i);
                preparedStatement.setString(3, "HPG");
                preparedStatement.setDouble(4, 10500);
                preparedStatement.setInt(5, 100);
                preparedStatement.setDouble(6, 100 * 10500);
                preparedStatement.setDate(7, new Date(System.currentTimeMillis()));
                preparedStatement.addBatch();
                batchTotal++;
                if (batchTotal == preparedStatement.getFetchSize()) {
                    System.out.println("check " + i);
                    int[] result = preparedStatement.executeBatch();
                    preparedStatement.clearBatch();
                    batchTotal=0;
                }
            }

            long start = System.currentTimeMillis();

            int[] inserted = preparedStatement.executeBatch();
            long end = System.currentTimeMillis();

            System.out.println("total time taken to insert the batch = " + (end - start) + " ms");
            System.out.println("total time taken = " + (end - start) / (recordsStop - recordsStart) + " s");

            preparedStatement.close();
            conn.close();

            return inserted;
        } catch (SQLException ex) {
            System.err.println("SQLException information");
            while (ex != null) {
                System.err.println("Error msg: " + ex.getMessage());
                ex = ex.getNextException();
            }
            throw new RuntimeException("Error");
        }
    }
}
