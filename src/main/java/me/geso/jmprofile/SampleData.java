package me.geso.jmprofile;

public class SampleData {
    private final String value;
    private final String query;
    private final String user;

    public SampleData(double value, PollerService.QueryInfo queryInfo) {
        this.value = String.format("%.3f", value);
        this.query = queryInfo.getQuery();
        this.user = queryInfo.getUser();
    }

    public String getQuery() {
        return query;
    }

    public String getValue() {
        return value;
    }

    public String getUser() {
        return user;
    }
}
