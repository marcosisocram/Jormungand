package com.marcosisocram.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.NullNode;
import com.marcosisocram.dto.TransacaoRequestDTO;

import java.io.IOException;

public class TransacaoRequestDeserializer extends StdDeserializer<TransacaoRequestDTO> {

    public TransacaoRequestDeserializer(Class<?> vc) {
        super(vc);
    }

    public TransacaoRequestDeserializer() {
        this(null);
    }

    @Override
    public TransacaoRequestDTO deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {


        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode jsonNode = objectCodec.readTree(jsonParser);

        JsonNode valor = jsonNode.get("valor");
        JsonNode tipo = jsonNode.get("tipo");
        JsonNode descricao = jsonNode.get("descricao");

        final TransacaoRequestDTO transacaoRequestDTO = new TransacaoRequestDTO();

        if (valor.asText().matches("^\\d+$")) {
            transacaoRequestDTO.setValor(valor.asLong());
        }

        if ("c".equalsIgnoreCase(tipo.asText()) || ("d".equalsIgnoreCase(tipo.asText()))) {
            transacaoRequestDTO.setTipo(tipo.asText());
        }

        if (!(descricao instanceof NullNode) && !descricao.asText().isEmpty()) {
            transacaoRequestDTO.setDescricao(descricao.asText());
        }

        return transacaoRequestDTO;

    }
}
