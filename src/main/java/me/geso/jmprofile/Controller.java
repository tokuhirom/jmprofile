package me.geso.jmprofile;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
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

                pollerService = new PollerService(connection, sampleDataObservableList, stateLabel);
                pollerService.setPeriod(new Duration(Double.valueOf(interval.getText())));
                pollerService.start();

                startButton.setText("Stop");
            } catch (InstantiationException | IllegalAccessException
                    | ClassNotFoundException | SQLException | NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exception");
                alert.setHeaderText("Gah!!!! Exception was occurred.");
                alert.setContentText(e.getMessage());

                alert.showAndWait();
            }
        }
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
