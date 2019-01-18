package spark;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.Callable;

import static spark.route.HttpMethod.delete;
import static spark.route.HttpMethod.get;
import static spark.route.HttpMethod.post;
import static spark.route.HttpMethod.put;

public class SparkMetered {

    static final String METRIC_NAME = "http.server.requests";
    static final String URL_TAG = "url";
    static final String METHOD_TAG = "method";

    private static MeterRegistry registry;

    public static void registry(MeterRegistry registry) {
        SparkMetered.registry = registry;
    }

    private SparkMetered() {}

    private static SparkMetered INSTANCE;

    private static synchronized SparkMetered getInstance() {
        if (INSTANCE != null)
            return INSTANCE;
        else if (registry != null) {
            INSTANCE = new SparkMetered();
            return INSTANCE;
        }

        throw new IllegalStateException(
                "Provide io.micrometer.core.instrument.MeterRegistry using spark.SparkMetered#registry first!");
    }

    public static void get(String path, Route route) {
        final SparkMetered i = getInstance();
        Spark.get(path, (rq, rs) -> i.metered(path, get.name(), () -> route.handle(rq, rs)));
    }

    public static void post(String path, Route route) {
        final SparkMetered i = getInstance();
        Spark.post(path, (rq, rs) -> i.metered(path, post.name(), () -> route.handle(rq, rs)));
    }

    public static void put(String path, Route route) {
        final SparkMetered i = getInstance();
        Spark.put(path, (rq, rs) -> i.metered(path, put.name(), () -> route.handle(rq, rs)));
    }

    public static void delete(String path, Route route) {
        final SparkMetered i = getInstance();
        Spark.delete(path, (rq, rs) -> i.metered(path, delete.name(), () -> route.handle(rq, rs)));
    }

    private Object metered(String path, String method, Callable<Object> s) throws Exception {
        final Timer timer = registry.timer(METRIC_NAME, URL_TAG, path, METHOD_TAG, method);
        return timer.recordCallable(s);
    }
}
