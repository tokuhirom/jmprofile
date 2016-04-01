package me.geso.jmprofile.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import me.geso.jmprofile.SampleData;
import me.geso.jmprofile.Stats;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextField hostField;

    @FXML
    TextField passwordField;

    @FXML
    TextField usernameField;

    @FXML
    TextField interval;

    @FXML
    Label stateLabel;

    @FXML
    TextField uriField;

    @FXML
    Button startButton;

    @FXML
    TableView<SampleData> tableView;

    @FXML
    TableColumn<SampleData, String> meanRateColumn;

    @FXML
    TableColumn<SampleData, String> oneMinuteRateColumn;

    @FXML
    TableColumn<SampleData, String> fiveMinuteRateColumn;

    @FXML
    TableColumn<SampleData, String> fifteenMinuteRateColumn;

    @FXML
    TableColumn<SampleData, String> queryColumn;

    @FXML
    TableColumn<SampleData, String> userColumn;

    private PollerService pollerService;

    private final ObservableList<SampleData> sampleDataObservableList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private Stats stats = new Stats();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        uriElementUpdated();
        tableView.setItems(sampleDataObservableList);
        meanRateColumn.setCellValueFactory(new PropertyValueFactory<>("meanRate"));
        oneMinuteRateColumn.setCellValueFactory(new PropertyValueFactory<>("oneMinuteRate"));
        fiveMinuteRateColumn.setCellValueFactory(new PropertyValueFactory<>("fiveMinuteRate"));
        fifteenMinuteRateColumn.setCellValueFactory(new PropertyValueFactory<>("fifteenMinuteRate"));
        queryColumn.setCellValueFactory(new PropertyValueFactory<>("query"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));

        // tableView updater
        ScheduledService<String> svc = new ScheduledService<String>() {
            protected Task<String> createTask() {
                return new Task<String>() {
                    protected String call() {
                        Platform.runLater(() -> {
                            sampleDataObservableList.setAll(stats.toStream()
                                    .toArray(SampleData[]::new));
                            stateLabel.setText("Data size: " + stats.size());
                        });
                        return null;
                    }
                };
            }
        };
        svc.setPeriod(Duration.seconds(1));
        svc.start();
    }

    public void doStart(ActionEvent actionEvent) {
        if (pollerService != null) {
            // stop previous job
            pollerService.cancel();
            pollerService = null;

            startButton.setText("Start");
        } else {
            sampleDataObservableList.clear();

            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();

                Connection connection = DriverManager.getConnection(buildUri());

                pollerService = new PollerService(
                        connection,
                        stats,
                        e -> Platform.runLater(() -> showExceptionAlertDialog(e)));
                pollerService.setPeriod(new Duration(Double.valueOf(interval.getText())));
                pollerService.start();

                startButton.setText("Stop");
            } catch (InstantiationException | IllegalAccessException
                    | ClassNotFoundException | SQLException | NumberFormatException e) {
                showExceptionAlertDialog(e);
            }
        }
    }

    private void showExceptionAlertDialog(Exception e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exception");
        alert.setHeaderText("Gah!!!! Exception was occurred.");
        alert.setContentText(e.getMessage());

        alert.showAndWait();
    }

    private String buildUri() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("jdbc:mysql://");
        stringBuilder.append(hostField.getText());
        stringBuilder.append("/?user=");
        stringBuilder.append(usernameField.getText());
        if (StringUtils.isNotBlank(passwordField.getText())) {
            stringBuilder.append("&password=").append(passwordField.getText());
        }
        return stringBuilder.toString();
    }

    public void uriElementUpdated() {
        String uri = buildUri();
        uriField.setText(uri);
    }
}
