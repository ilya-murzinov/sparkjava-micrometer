package spark;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static spark.SparkMetered.get;

public class SparkMeteredTest {

    @Test
    void should_throw_exception__if_not_initialized() {
        assertThatThrownBy(() -> get("/", (rq, rs) -> null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(SparkMetered.class.getSimpleName());
    }
}
