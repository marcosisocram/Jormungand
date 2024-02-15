package com.marcosisocram.handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosisocram.dto.SaldoResponseDTO;
import com.marcosisocram.dto.SaldoUltimaTransacaoDTO;
import com.sun.net.httpserver.HttpExchange;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExtratoHandle extends CustonHttpHandler {

    private final HikariDataSource hikariDataSource;

    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(ExtratoHandle.class);

    public ExtratoHandle(HikariDataSource hikariDataSource, ObjectMapper objectMapper) {
        this.hikariDataSource = hikariDataSource;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange httpExchange, String requestId) throws IOException {

        try (Connection connection = hikariDataSource.getConnection()) {

            SaldoResponseDTO saldoResponseDTO = null;

            try (PreparedStatement st = connection.prepareStatement("select saldo, limite from clientes where id = ?")) {

                st.setInt(1, Integer.parseInt(requestId));

                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        saldoResponseDTO = new SaldoResponseDTO(rs.getLong("saldo"), LocalDateTime.now(), rs.getLong("limite"), null);
                    }
                }
            }

            if (saldoResponseDTO == null) {
                try (OutputStream outputStream = httpExchange.getResponseBody()) {

                    httpExchange.sendResponseHeaders(404, 0);
                    outputStream.flush();

                } catch (Exception exception) {

                    logger.atError().setMessage(exception.getMessage()).log();

                    throw exception;
                }

                return;
            }

            final List<SaldoUltimaTransacaoDTO> saldoUltimaTransacaoDTOS = new ArrayList<>();

            try (PreparedStatement st = connection.prepareStatement("select valor, tipo, descricao, realizada_em from transacoes where cliente_id = ? order by realizada_em desc limit 10")) {

                st.setInt(1, Integer.parseInt(requestId));

                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        saldoUltimaTransacaoDTOS.add(new SaldoUltimaTransacaoDTO(rs.getLong("valor"), rs.getString("tipo"), rs.getString("descricao"), rs.getTimestamp("realizada_em").toLocalDateTime()));
                    }
                }
            }

            saldoResponseDTO.setSaldoUltimasTransacoesDTO(saldoUltimaTransacaoDTOS);

            String saldoResponse = objectMapper.writeValueAsString(saldoResponseDTO);

            handleResponse(saldoResponse, httpExchange, 200);

        } catch (SQLException e) {

            logger.atError().setMessage("Erro ao buscar o cliente: \n{}").addArgument(e.getMessage()).log();
            throw new RuntimeException(e);
        }

    }
}
