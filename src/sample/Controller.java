package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    TextField hostField;

    @FXML
    TextField passwordField;

    @FXML
    TextField usernameField;

    @FXML
    TextField interval;

    @FXML
    Button startButton;

    @FXML
    Button resetButton;

    @FXML
    TableView<String> tableView;

    public void doStart(ActionEvent actionEvent) {

    }

    public void doReset(ActionEvent actionEvent) {

    }
}
