package com.marcosisocram.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.marcosisocram.dto.SaldoResponseDTO;
import com.marcosisocram.dto.SaldoUltimaTransacaoDTO;

import java.io.IOException;

public class SaldoResponseSerializer extends StdSerializer<SaldoResponseDTO> {

    public SaldoResponseSerializer() {
        this(null);
    }

    public SaldoResponseSerializer(Class<SaldoResponseDTO> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(SaldoResponseDTO saldoResponseDTO, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeFieldName("saldo");

        jsonGenerator.writeStartObject();


        jsonGenerator.writeNumberField("limite", saldoResponseDTO.getLimite());
        jsonGenerator.writeNumberField("total", saldoResponseDTO.getTotal());
        jsonGenerator.writeStringField("data_extrato", saldoResponseDTO.getDataExtrato().toString());

        jsonGenerator.writeEndObject();

        jsonGenerator.writeArrayFieldStart("ultimas_transacoes");

        for (SaldoUltimaTransacaoDTO saldoUltimaTransacaoDTO : saldoResponseDTO.getSaldoUltimasTransacoesDTO()) {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField("valor", saldoUltimaTransacaoDTO.getValor());
                jsonGenerator.writeStringField("tipo", saldoUltimaTransacaoDTO.getTipo());
                jsonGenerator.writeStringField("descricao", saldoUltimaTransacaoDTO.getDescricao());
                jsonGenerator.writeStringField("realizada_em", saldoUltimaTransacaoDTO.getRealizadaEm().toString());
                jsonGenerator.writeEndObject();
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
            }
        }

        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
