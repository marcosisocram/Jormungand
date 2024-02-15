package com.marcosisocram.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.marcosisocram.dto.TransacaoResponseDTO;

import java.io.IOException;

public class TransacaoResponseSerializer extends StdSerializer<TransacaoResponseDTO> {

    public TransacaoResponseSerializer() {
        this(null);
    }

    public TransacaoResponseSerializer(Class<TransacaoResponseDTO> clazz) {
        super(clazz);
    }

    @Override
    public void serialize(TransacaoResponseDTO transacaoResponseDTO, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("limite", transacaoResponseDTO.getLimite());
        jsonGenerator.writeNumberField("saldo", transacaoResponseDTO.getSaldo());
        jsonGenerator.writeEndObject();
    }
}
