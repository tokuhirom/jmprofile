package me.geso.jmprofile;

import java.util.regex.Pattern;

public class QueryNormalizer {
    private static final Pattern INFO1 = Pattern.compile("(-?(?:0x[0-9a-f]+|[0-9][0-9.]*)|'.*?[^'\\\\]'|\".*?[^\\\\]\")");
    private static final Pattern INFO2 = Pattern.compile("(\\s+IN\\s+)\\([\\?,\\s]+\\)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern INFO3 = Pattern.compile("(\\s+VALUES\\s+)[\\(\\)\\?\\,\\s]+",
            Pattern.CASE_INSENSITIVE);

    public String normalize(String query) {
        query = INFO1.matcher(query).replaceAll("?");
        query = INFO2.matcher(query).replaceAll("\\1(...)");
        query = INFO3.matcher(query).replaceAll("\\1...");
        return query;
    }
}
