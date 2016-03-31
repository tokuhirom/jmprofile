package me.geso.jmprofile;

import com.codahale.metrics.Meter;
import lombok.Data;

@Data
public class SampleData {
    private final String meanRate;
    private final String oneMinuteRate;
    private final String fiveMinuteRate;
    private final String fifteenMinuteRate;
    private final String query;
    private final String user;

    public SampleData(Meter meter, PollerService.QueryInfo queryInfo) {
        meanRate = String.format("%.3f", meter.getMeanRate());
        oneMinuteRate = String.format("%.3f", meter.getOneMinuteRate());
        fiveMinuteRate = String.format("%.3f", meter.getFiveMinuteRate());
        fifteenMinuteRate = String.format("%.3f", meter.getFifteenMinuteRate());
        this.query = queryInfo.getQuery();
        this.user = queryInfo.getUser();
    }

    public String getQuery() {
        return query;
    }

    public String getMeanRate() {
        return meanRate;
    }

    public String getUser() {
        return user;
    }
}
