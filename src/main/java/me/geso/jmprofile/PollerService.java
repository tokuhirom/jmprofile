package me.geso.jmprofile;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import lombok.Data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class PollerService extends ScheduledService<String> {
    private final Connection connection;
    private final ObservableList<SampleData> sampleDataObservableList;
    private Label stateLabel;
    private int samples;

    public PollerService(Connection connection, ObservableList<SampleData> sampleDataObservableList, Label stateLabel) {
        this.connection = connection;
        this.sampleDataObservableList = sampleDataObservableList;
        this.stateLabel = stateLabel;
        this.samples = 0;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            private QueryNormalizer queryNormalizer = new QueryNormalizer();
            private Stats stats = new Stats();

            protected String call() throws IOException {
                // get samples
                try {
                    samples++;
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SHOW FULL PROCESSLIST")) {
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
                } catch (SQLException e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Exception");
                        alert.setHeaderText("Gah!!!! Exception was occurred.");
                        alert.setContentText(e.getMessage());

                        alert.showAndWait();
                    });
                }

                // update table view
                // TODO: should we update tableView in another thread?
                Platform.runLater(() -> {
                    sampleDataObservableList.clear();
                    sampleDataObservableList.addAll(
                            stats.toStream()
                                    .toArray(SampleData[]::new)
                    );

                    stateLabel.setText("Samples: " + samples + ", Data size: " + stats.size());
                });

                return "";
            }
        };
    }

    @Data
    public static class QueryInfo {
        private final String query;
        private final String user;

        public QueryInfo(String query, String user) {
            this.query = query;
            this.user = user;
        }

        public String getUser() {
            return user;
        }

        public String getQuery() {
            return query;
        }
    }
}
