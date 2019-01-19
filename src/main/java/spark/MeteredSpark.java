package spark;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import spark.route.HttpMethod;

import java.util.concurrent.Callable;

public class MeteredSpark extends Routable {

    static final String METRIC_NAME = "http.server.requests";
    static final String URL_TAG = "url";
    static final String METHOD_TAG = "method";

    private final Service service;
    private final MeterRegistry registry;

    private MeteredSpark(MeterRegistry registry) {
        this.registry = registry;
        this.service = Service.ignite();
    }

    public static MeteredSpark meteredService(MeterRegistry registry) {
        return new MeteredSpark(registry);
    }

    @Override
    protected void addRoute(HttpMethod httpMethod, RouteImpl route) {
        addRoute(httpMethod.name(), route);
    }

    @Override
    protected void addRoute(String httpMethod, RouteImpl route) {
        final String fullPath = service.getPaths() + route.getPath();

        service.addRoute(httpMethod, new RouteImpl(route.getPath()) {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                return metered(fullPath, httpMethod, () -> route.handle(request, response));
            }
        });
    }

    @Override
    protected void addFilter(HttpMethod httpMethod, FilterImpl filter) {
        service.addFilter(httpMethod, filter);
    }

    @Override
    protected void addFilter(String httpMethod, FilterImpl filter) {
        service.addFilter(httpMethod, filter);
    }

    public void path(String route, RouteGroup routeGroup) {
        service.path(route, routeGroup);
    }

    public void awaitInitialization() {
        service.awaitInitialization();
    }

    public void stop() {
        service.stop();
        service.awaitStop();
    }

    private Object metered(String path, String method, Callable<Object> s) throws Exception {
        final Timer timer = registry.timer(METRIC_NAME, URL_TAG, path, METHOD_TAG, method);
        return timer.recordCallable(s);
    }
}
