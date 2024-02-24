package com.marcosisocram.handle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler extends BaseHandle implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final ExtratoHandle extratoHandle;
    private final TransacaoHandle transacaoHandle;

    public RequestHandler(/*HikariDataSource hikariDataSource*/) throws SQLException {

        extratoHandle = new ExtratoHandle();
        transacaoHandle = new TransacaoHandle();

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        try {

            final String requestURI = httpExchange.getRequestURI().toString();

            final Pattern ptt = Pattern.compile("^/clientes/(\\d+)/(extrato|transacoes)$");

            Matcher matcher = ptt.matcher(requestURI);

            String id = "0";
            String path = "outro";

            if(matcher.find()) {
                id = matcher.group(1);
                path = matcher.group(2);
            }

            if (httpExchange.getRequestMethod().equals("GET") && "extrato".equals(path)) {

                extratoHandle.handle(httpExchange, id);

            } else if (httpExchange.getRequestMethod().equals("POST") && "transacoes".equals(path)) {

                transacaoHandle.handle(httpExchange, id);

            } else {
                //chegou aqui 404
                handleResponse(httpExchange, 404);
            }
        } catch (Exception exception) {
            logger.atError().setMessage("Erro geral: \n{}").addArgument(exception.getMessage()).log();
            handleResponse(httpExchange, 500);
        }
    }
}
