package me.geso.jmprofile;

import com.codahale.metrics.Meter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Stats {
    private QueryNormalizer queryNormalizer = new QueryNormalizer();
    private final Map<PollerService.QueryInfo, Meter> data;

    public Stats() {
        this.data = new ConcurrentHashMap<>();
    }

    public void post(String sql, String user) {
        String query = queryNormalizer.normalize(sql);
        PollerService.QueryInfo queryInfo = new PollerService.QueryInfo(query, user);
        if (data.containsKey(queryInfo)) {
            data.get(queryInfo).mark();
        } else {
            Meter meter = new Meter();
            meter.mark();
            data.put(queryInfo, meter);
        }
    }

    public Stream<SampleData> toStream() {
        return data.entrySet()
                .stream()
                .sorted((a, b) -> (int)(b.getValue().getCount() - a.getValue().getCount()))
                .map(it -> new SampleData(it.getValue(), it.getKey()));
    }

    public int size() {
        return data.size();
    }
}
