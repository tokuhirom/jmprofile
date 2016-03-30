package sample;

public class SampleData {
    private final String value;
    private final String query;

    public SampleData(Double value, String query) {
        this.value = String.format("%.3f", value);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public String getValue() {
        return value;
    }
}
