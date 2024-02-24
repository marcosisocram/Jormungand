package com.marcosisocram;

import com.marcosisocram.db.DbConnection;
import com.marcosisocram.handle.RequestHandler;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

public class Main {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final long MILHAO = 1_000_000L;

    public static void main(String[] args) throws IOException, SQLException {

        final long time = System.nanoTime();

        final String port = Optional.ofNullable(System.getenv("PORT")).orElse("8080");

        final HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);

        final ExecutorService threadPoolExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("vt-rinha").factory());

        final Supplier<String> nextRequestId = () -> Long.toString(System.nanoTime());

        final Logger logger = LoggerFactory.getLogger(Main.class);

        Runnable runnable = () -> {
            try {
                DbConnection.getInstance();
            } catch (Throwable throwable) {
                //ferrou
            }
        };

        ThreadFactory virtualThreadFactory = Thread.ofVirtual().name("vt-rinha-db").factory();
        Thread virtualThread = virtualThreadFactory.newThread(runnable);
        virtualThread.start();


        final List<Filter> filters = List.of(tracing(nextRequestId), logging(logger));

        server.createContext("/clientes", new RequestHandler()).getFilters().addAll(filters);

        server.setExecutor(threadPoolExecutor);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            logger.info("Server is shutting down...");

            try {
                DbConnection.getInstance().close();
                logger.info("Conexao fechada");
            } catch (SQLException e) {
                //ignore
            }

            server.stop(5);
            logger.info("Server stopped");

        }));

        logger.atInfo()
                .setMessage("Server stared on port :{} in {}ms")
                .addArgument(port)
                .addArgument((System.nanoTime() - time) / MILHAO)
                .log();
    }

//    private static HikariDataSource getConnection() {
//
//        final String jdbcUrl = Optional.ofNullable(System.getenv("DB_URL")).orElse("jdbc:postgresql://localhost:5432/postgres");
//        final String username = Optional.ofNullable(System.getenv("DB_USER")).orElse("user");
//        final String password = Optional.ofNullable(System.getenv("DB_PASSWORD")).orElse("rinha-de-bK");
//        final String poolsize = Optional.ofNullable(System.getenv("DB_POOLSIZE")).orElse("10");
//
//        final HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(jdbcUrl);
//        config.setUsername(username);
//        config.setPassword(password);
//
//        config.setMaximumPoolSize(Integer.parseInt(poolsize));
//
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//
//        return new HikariDataSource(config);
//    }

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