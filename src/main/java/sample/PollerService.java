package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PollerService extends ScheduledService<String> {
    private final Connection connection;
    private final ObservableList<SampleData> sampleDataObservableList;
    private Label stateLabel;
    private int samples;
    private final Map<String, Integer> data;

    public PollerService(Connection connection, ObservableList<SampleData> sampleDataObservableList, Label stateLabel) {
        this.connection = connection;
        this.sampleDataObservableList = sampleDataObservableList;
        this.stateLabel = stateLabel;
        this.samples = 0;
        this.data = new HashMap<>();
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            private QueryNormalizer queryNormalizer = new QueryNormalizer();

            protected String call() throws IOException {
                // get samples
                try {
                    samples++;
                    try (PreparedStatement preparedStatement = connection.prepareStatement("SHOW FULL PROCESSLIST")) {
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                String info = resultSet.getString("Info");
                                if ("SHOW FULL PROCESSLIST".equals(info)) {
                                    continue;
                                }
                                if (info != null) {
                                    String query = queryNormalizer.normalize(info);
                                    if (data.containsKey(query)) {
                                        data.put(query, data.get(query) + 1);
                                    } else {
                                        data.put(query, 1);
                                    }
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
                Platform.runLater(() -> {
                    sampleDataObservableList.clear();
                    sampleDataObservableList.addAll(
                            data.entrySet()
                                    .stream()
                                    .sorted((a, b) -> a.getValue() - b.getValue())
                                    .map(it -> new SampleData(it.getValue() / (samples * 100.0), it.getKey()))
                                    .toArray(SampleData[]::new)
                    );

                    stateLabel.setText("Samples: " + samples + ", Data size: " + data.size());
                });

                return "";
            }
        };
    }
}
