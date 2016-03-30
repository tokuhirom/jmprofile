package me.geso.jmprofile;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class QueryNormalizerTest {

    @Test
    public void normalize() throws Exception {
        QueryNormalizer queryNormalizer = new QueryNormalizer();
        assertThat(queryNormalizer.normalize("hoge"))
                .isEqualTo("hoge");
        assertThat(queryNormalizer.normalize("SELECT a.b FROM c"))
                .isEqualTo("SELECT a.b FROM c");
        assertThat(queryNormalizer.normalize("WHERE 5"))
                .isEqualTo("WHERE ?");
        assertThat(queryNormalizer.normalize("WHERE 'foo'"))
                .isEqualTo("WHERE ?");
    }
}