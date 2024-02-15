package com.marcosisocram.handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosisocram.dto.TransacaoRequestDTO;
import com.marcosisocram.dto.TransacaoResponseDTO;
import com.marcosisocram.exception.NaoSeiException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransacaoHandle extends CustonHttpHandler {

    private final HikariDataSource hikariDataSource;

    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(TransacaoHandle.class);

    public TransacaoHandle(HikariDataSource hikariDataSource, ObjectMapper objectMapper) {
        this.hikariDataSource = hikariDataSource;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange httpExchange, String requestId) throws IOException {

        final Headers requestHeaders = httpExchange.getRequestHeaders();

        final int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

        final InputStream is = httpExchange.getRequestBody();

        final byte[] data = new byte[contentLength];

        final int readIntoBuffer = is.read(data);

        if (readIntoBuffer < 0) {
            handleResponse(httpExchange, 422);
            return;
        }

        final String jsonRequestBody = new String(data);

        final TransacaoRequestDTO transacaoRequestDTO = objectMapper.readValue(jsonRequestBody, TransacaoRequestDTO.class);

        if ((transacaoRequestDTO.getDescricao() == null
                || transacaoRequestDTO.getDescricao().isEmpty()
                || transacaoRequestDTO.getDescricao().length() > 10)
                || transacaoRequestDTO.getValor() == null
                || transacaoRequestDTO.getTipo() == null ) {

            handleResponse(httpExchange, 422);
            return;
        }

        final var transacao = new TransacaoResponseDTO();

        if ("c".equalsIgnoreCase(transacaoRequestDTO.getTipo())) {

            try (Connection connection = hikariDataSource.getConnection()) {

                try (PreparedStatement st = connection.prepareCall("{call creditar(?, ?, ?)}")) {

                    st.setInt(1, Integer.parseInt(requestId));
                    st.setInt(2, transacaoRequestDTO.getValor().intValue());
                    st.setString(3, transacaoRequestDTO.getDescricao());

                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            transacao.setSaldo(rs.getLong("saldo_r"));
                            transacao.setLimite(rs.getLong("limite_r"));
                        }
                    }
                }
            } catch (SQLException e) {
                logger.atError().setMessage("Erro ao creditar o cliente: \n{}").addArgument(e.getMessage()).log();

                throw new NaoSeiException(e.getMessage());
            }

        } else if ("d".equalsIgnoreCase(transacaoRequestDTO.getTipo())) {

            try (Connection connection = hikariDataSource.getConnection()) {

                try (PreparedStatement st = connection.prepareCall("{call debitar(?, ?, ?)}")) {

                    st.setInt(1, Integer.parseInt(requestId));
                    st.setInt(2, transacaoRequestDTO.getValor().intValue());
                    st.setString(3, transacaoRequestDTO.getDescricao());

                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            transacao.setSaldo(rs.getLong("saldo_r"));
                            transacao.setLimite(rs.getLong("limite_r"));
                        }
                    }
                }
            } catch (PSQLException e) {

                if (e.getServerErrorMessage() != null && "Valor ultrapassa o limite+saldo".equalsIgnoreCase(e.getServerErrorMessage().getMessage())) {
                    logger.atWarn().setMessage("Cliente não tem limite").log();

                    handleResponse(httpExchange, 422);
                    return;
                }

                logger.atError().setMessage("Erro ao debitar o cliente: \n{}").addArgument(e.getMessage()).log();
                throw new NaoSeiException(e.getMessage());

            } catch (SQLException e) {

                logger.atError().setMessage("Erro ao debitar o cliente: \n{}").addArgument(e.getMessage()).log();
                throw new NaoSeiException(e.getMessage());

            }
        }

        final String transacaoResponse = objectMapper.writeValueAsString(transacao);

        handleResponse(transacaoResponse, httpExchange, 200);

    }
}
