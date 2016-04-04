package me.geso.jmprofile.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import me.geso.jmprofile.QueryInfo;
import me.geso.jmprofile.SampleData;
import me.geso.jmprofile.Stats;
import org.apache.commons.lang3.StringUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

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

    private final ObservableList<SampleData> sampleDataObservableList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private Stats stats = new Stats();
    private Subscription subscription;

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
        if (subscription != null) {
            // stop previous job
            subscription.unsubscribe();
            subscription = null;

            stats.clear();

            startButton.setText("Start");
        } else {
            sampleDataObservableList.clear();

            try {
                this.subscription =
                        Observable.<Connection>create(observer -> {
                            try {
                                observer.onNext(DriverManager.getConnection(buildUri()));
                            } catch (SQLException e) {
                                observer.onError(e);
                            }
                        }).flatMap(
                                connection -> Observable.<QueryInfo>create(observer ->
                                        Schedulers.newThread().createWorker().schedulePeriodically(
                                                () -> showFullProcesslist(observer, connection),
                                                0,
                                                (long) (Double.valueOf(interval.getText()) * 1000),
                                                TimeUnit.MILLISECONDS))
                                        .doOnNext(info -> stats.post(info.getQuery(), info.getUser()))
                        ).debounce(500, TimeUnit.MILLISECONDS)
                        .subscribe(it -> refreshTable(),
                                e -> {
                                    Platform.runLater(() -> showExceptionAlertDialog(e));
                                });


                startButton.setText("Stop");
            } catch (NumberFormatException e) {
                showExceptionAlertDialog(e);
            }
        }
    }

    private void refreshTable() {
        Platform.runLater(() -> {
            sampleDataObservableList.setAll(stats.toStream()
                    .toArray(SampleData[]::new));
            stateLabel.setText("Data size: " + stats.size());
        });
    }

    private void showFullProcesslist(Subscriber<? super QueryInfo> subscriber,
                                     Connection connection) {
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
                        subscriber.onNext(new QueryInfo(info, user));
                    }
                }
            }
        } catch (SQLException e) {
            subscriber.onError(e);
        }
    }

    private void showExceptionAlertDialog(Throwable e) {
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
