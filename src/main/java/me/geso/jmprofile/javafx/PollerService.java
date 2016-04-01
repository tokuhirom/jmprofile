package me.geso.jmprofile.javafx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import lombok.Data;
import me.geso.jmprofile.Aggregator;
import me.geso.jmprofile.Stats;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PollerService extends ScheduledService<String> {
    private final Aggregator aggregator;
    private Consumer<SQLException> errorCallback;

    public PollerService(Connection connection, Stats stats, Consumer<SQLException> errorCallback) {
        this.errorCallback = errorCallback;
        this.aggregator = new Aggregator(connection, stats);
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            protected String call() throws IOException {
                // get samples
                try {
                    aggregator.aggregate();
                } catch (SQLException e) {
                    errorCallback.accept(e);
                }

                return "";
            }
        };
    }

}
