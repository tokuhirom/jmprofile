package me.geso.jmprofile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Aggregator {
    private final Connection connection;
    private final Stats stats;

    public Aggregator(Connection connection, Stats stats) {
        this.connection = connection;
        this.stats = stats;
    }

    public void aggregate() throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SHOW FULL PROCESSLIST"
        )) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String info = resultSet.getString("Info");
                    String user = resultSet.getString("User");
                    if ("SHOW FULL PROCESSLIST".equals(info)) {
                        continue;
                    }
                    if (info != null) {
                        stats.post(info, user);
                    }
                }
            }
        }
    }
}
