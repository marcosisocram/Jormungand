package com.marcosisocram.handle;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.marcosisocram.dto.SaldoResponseDTO;
import com.marcosisocram.dto.TransacaoRequestDTO;
import com.marcosisocram.dto.TransacaoResponseDTO;
import com.marcosisocram.jackson.SaldoResponseSerializer;
import com.marcosisocram.jackson.TransacaoRequestDeserializer;
import com.marcosisocram.jackson.TransacaoResponseSerializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RequestHandler extends BaseHandle implements HttpHandler {

    private final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final ExtratoHandle extratoHandle;
    private final TransacaoHandle transacaoHandle;

    public RequestHandler(HikariDataSource hikariDataSource) {

        final ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module =
                new SimpleModule("CustomSimpleModule", new Version(1, 0, 0, null, null, null));

        module.addDeserializer(TransacaoRequestDTO.class, new TransacaoRequestDeserializer());
        module.addSerializer(TransacaoResponseDTO.class, new TransacaoResponseSerializer());
        module.addSerializer(SaldoResponseDTO.class, new SaldoResponseSerializer());

        objectMapper.registerModule(module);

        extratoHandle = new ExtratoHandle(hikariDataSource, objectMapper);
        transacaoHandle = new TransacaoHandle(hikariDataSource, objectMapper);

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        try {

            final String requestURI = httpExchange.getRequestURI().toString();

            final String replaceRequestURI = requestURI.replace("/clientes/", "");

            final String[] splitRequestURI = replaceRequestURI.split("/", 2);

            final String id = splitRequestURI[0];
            final String path = splitRequestURI[1];

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
