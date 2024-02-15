package com.marcosisocram;

import com.marcosisocram.handle.RequestHandler;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Main {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final long MILHAO = 1_000_000L;

    public static void main(String[] args) throws IOException {

        final long time = System.nanoTime();

        final String port = Optional.ofNullable(System.getenv("PORT")).orElse("8080");

        final HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);

        final ExecutorService threadPoolExecutor = Executors.newVirtualThreadPerTaskExecutor();

        final Supplier<String> nextRequestId = () -> Long.toString(System.nanoTime());

        final HikariDataSource connection = getConnection();

        final Logger logger = LoggerFactory.getLogger(Main.class);

        final List<Filter> filters = List.of(tracing(nextRequestId), logging(logger));

        server.createContext("/clientes", new RequestHandler(connection)).getFilters().addAll(filters);

        server.setExecutor(threadPoolExecutor);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            logger.info("Server is shutting down...");

            connection.close();

            server.stop(5);
            logger.info("Server stopped");

        }));

        logger.atInfo()
                .setMessage("Server stared on port :{} in {}ms")
                .addArgument(port)
                .addArgument((System.nanoTime() - time) / MILHAO)
                .log();
    }

    private static HikariDataSource getConnection() {

        final String jdbcUrl = Optional.ofNullable(System.getenv("DB_URL")).orElse("jdbc:postgresql://localhost:5432/postgres");
        final String username = Optional.ofNullable(System.getenv("DB_USER")).orElse("user");
        final String password = Optional.ofNullable(System.getenv("DB_PASSWORD")).orElse("rinha-de-bK");
        final String poolsize = Optional.ofNullable(System.getenv("DB_POOLSIZE")).orElse("10");

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        //TODO separar um dataSource de leitura e outro de escrita
        config.setMaximumPoolSize(Integer.parseInt(poolsize));

        //TODO testar se tirando essas properties muda alguma coisa
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    private static Filter logging(Logger logger) {
        return new Filter() {
            @Override
            public void doFilter(HttpExchange http, Chain chain) throws IOException {
                try {

                    if (logger.isDebugEnabled()) {

                        String requestId = getRequestId(http);

                        logger.atDebug()
                                .setMessage("--> {} {} {} {}")
                                .addArgument(requestId)
                                .addArgument(http.getRequestMethod())
                                .addArgument(http.getRequestURI().getPath())
                                .addArgument(http.getRequestHeaders().getFirst("User-Agent"))
                                .log();
                    }

                    chain.doFilter(http);
                } finally {
                    if (logger.isDebugEnabled()) {
                        int responseCode = http.getResponseCode();

                        String requestId = getRequestId(http);

                        logger.atDebug()
                                .setMessage("<-- {} {} {} {}")
                                .addArgument(requestId)
                                .addArgument(http.getRequestMethod())
                                .addArgument(http.getRequestURI().getPath())
                                .addArgument(responseCode)
                                .log();
                    }
                }
            }

            @Override
            public String description() {
                return "logging";
            }
        };
    }

    private static String getRequestId(HttpExchange http) {
        Object possibleRequestId = http.getAttribute(REQUEST_ID_KEY);

        String requestId = "unknown";
        if (possibleRequestId instanceof String) {
            requestId = (String) possibleRequestId;
        }
        return requestId;
    }

    private static Filter tracing(Supplier<String> nextRequestId) {
        return new Filter() {
            @Override
            public void doFilter(HttpExchange http, Chain chain) throws IOException {
                String requestId = http.getRequestHeaders().getFirst("X-Request-Id");

                if (requestId == null) {
                    requestId = nextRequestId.get();
                }

                http.setAttribute(REQUEST_ID_KEY, requestId);
                http.getResponseHeaders().add("X-Request-Id", requestId);
                chain.doFilter(http);
            }

            @Override
            public String description() {
                return "tracing";
            }
        };
    }
}