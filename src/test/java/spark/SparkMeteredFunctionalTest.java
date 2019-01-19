package spark;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static spark.MeteredSpark.METHOD_TAG;
import static spark.MeteredSpark.METRIC_NAME;
import static spark.MeteredSpark.URL_TAG;
import static spark.MeteredSpark.meteredService;

class SparkMeteredFunctionalTest {

    private static final MeterRegistry registry = new SimpleMeterRegistry();
    private static final MeteredSpark spark;
    private static final RequestSpecification client = given().port(4567);
    private static final int SLEEP = 10;

    static {
        spark = meteredService(registry);
        spark.get("/foo_get", (rq, rs) -> longMethod());
        spark.post("/foo_post", (rq, rs) -> longMethod());
        spark.put("/foo_put", (rq, rs) -> longMethod());
        spark.delete("/foo_delete", (rq, rs) -> longMethod());

        spark.path("/path", () -> {
            spark.get("/foo_get", (rq, rs) -> longMethod());
            spark.post("/foo_post", (rq, rs) -> longMethod());
            spark.put("/foo_put", (rq, rs) -> longMethod());
            spark.delete("/foo_delete", (rq, rs) -> longMethod());
        });

        getRuntime().addShutdownHook(new Thread(spark::stop));
        spark.awaitInitialization();
    }

    @Test
    void should_register_get_method_call() {
        // when
        client.get("/foo_get").body();
        client.get("/path/foo_get").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_get")
                .tag(METHOD_TAG, "get")
                .timer();

        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    @Test
    void should_register_post_method_call() {
        // when
        client.post("/foo_post").body();
        client.post("/path/foo_post").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_post")
                .tag(METHOD_TAG, "post")
                .timer();

        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    @Test
    void should_register_put_method_call() {
        // when
        client.put("/foo_put").body();
        client.put("/path/foo_put").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_put")
                .tag(METHOD_TAG, "put")
                .timer();

        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    @Test
    void should_register_delete_method_call() {
        // when
        client.delete("/foo_delete").body();
        client.delete("/path/foo_delete").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_delete")
                .tag(METHOD_TAG, "delete")
                .timer();

        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    private static Object longMethod() throws Exception {
        Thread.sleep(SLEEP);
        return null;
    }

    @AfterAll
    static void shutDown() {
        spark.stop();
    }
}
