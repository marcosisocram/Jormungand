package com.marcosisocram.handle;

import com.marcosisocram.db.DbConnection;
import com.marcosisocram.dto.TransacaoRequestDTO;
import com.marcosisocram.dto.TransacaoResponseDTO;
import com.marcosisocram.exception.NaoSeiException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class TransacaoHandle extends CustonHttpHandler {

//    private final HikariDataSource hikariDataSource;

    private final Logger logger = LoggerFactory.getLogger(TransacaoHandle.class);
//    private final Connection connection;

    public TransacaoHandle() throws SQLException {
//        this.hikariDataSource = hikariDataSource;
//        this.connection = DbConnection.getInstance();
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

        final var tt = Pattern.compile("\"valor\": ?\"?\\W?([0-9.]+)\"?|\"tipo\": ?\"([cd])\"|\"descricao\": ?\"([\\w\\s]{1,10})\"");

        final var tt2 = tt.matcher(jsonRequestBody);

        long valor = 0L;
        String tipo = "";
        String descricao = "";
        try {
            if (tt2.find()) {
                String valorStr = tt2.group(1);
                if (valorStr.indexOf(".") > 0) {
                    handleResponse(httpExchange, 422);
                    return;
                }
                valor = Long.parseLong(valorStr);
            }
        } catch (Throwable throwable) {
            handleResponse(httpExchange, 422);
            return;
        }

        try {
            if (!tt2.find()) {
                handleResponse(httpExchange, 422);
                return;
            }

            tipo = tt2.group(2);
        } catch (Throwable throwable) {
            handleResponse(httpExchange, 422);
            return;
        }

        try {
            if (!tt2.find()) {
                handleResponse(httpExchange, 422);
                return;
            }

            descricao = tt2.group(3);
        } catch (Throwable throwable) {
            handleResponse(httpExchange, 422);
            return;
        }

        final TransacaoRequestDTO transacaoRequestDTO = new TransacaoRequestDTO(valor, tipo, descricao);

        final var transacao = new TransacaoResponseDTO();

        if ("c".equalsIgnoreCase(transacaoRequestDTO.getTipo())) {

            try /*(Connection connection = DbConnection.getInstance()) */{

                try (PreparedStatement st = DbConnection.getInstance().prepareCall("{call creditar(?, ?, ?)}")) {

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

//            try /*(Connection connection = hikariDataSource.getConnection())*/ {
            try /*(Connection connection = DbConnection.getInstance()) */{

                try (PreparedStatement st = DbConnection.getInstance().prepareCall("{call debitar(?, ?, ?)}")) {

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

        handleResponse(transacao.toJson(), httpExchange, 200);

    }
}
