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
import static spark.SparkMetered.METHOD_TAG;
import static spark.SparkMetered.METRIC_NAME;
import static spark.SparkMetered.URL_TAG;

class SparkMeteredFunctionalTest {

    private static final MeterRegistry registry = new SimpleMeterRegistry();
    private static final RequestSpecification client = given().port(4567);
    private static final int SLEEP = 10;

    static {
        SparkMetered.registry(registry);
        SparkMetered.get("/foo_get", (rq, rs) -> longMethod());
        SparkMetered.post("/foo_post", (rq, rs) -> longMethod());
        SparkMetered.put("/foo_put", (rq, rs) -> longMethod());
        SparkMetered.delete("/foo_delete", (rq, rs) -> longMethod());

        getRuntime().addShutdownHook(new Thread(Spark::stop));
        Spark.awaitInitialization();
    }

    @Test
    void should_register_get_method_call() {
        // when
        client.get("/foo_get").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_get")
                .tag(METHOD_TAG, "get")
                .timer();

        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    @Test
    void should_register_post_method_call() {
        // when
        client.post("/foo_post").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_post")
                .tag(METHOD_TAG, "post")
                .timer();

        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    @Test
    void should_register_put_method_call() {
        // when
        client.put("/foo_put").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_put")
                .tag(METHOD_TAG, "put")
                .timer();

        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    @Test
    void should_register_delete_method_call() {
        // when
        client.delete("/foo_delete").body();

        // then
        final Timer timer = registry.get(METRIC_NAME)
                .tag(URL_TAG, "/foo_delete")
                .tag(METHOD_TAG, "delete")
                .timer();

        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.max(MILLISECONDS)).isGreaterThan(SLEEP);
    }

    private static Object longMethod() throws Exception {
        Thread.sleep(SLEEP);
        return null;
    }

    @AfterAll
    static void shutDown() {
        Spark.stop();
        Spark.awaitStop();
    }
}
