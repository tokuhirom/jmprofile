package me.geso.jmprofile;

import lombok.Data;

@Data
public class QueryInfo {
    private final String query;
    private final String user;

    public QueryInfo(String query, String user) {
        this.query = query;
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public String getQuery() {
        return query;
    }
}
