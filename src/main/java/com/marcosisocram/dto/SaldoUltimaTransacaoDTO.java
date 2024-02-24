package com.marcosisocram.dto;

import java.time.LocalDateTime;

public class SaldoUltimaTransacaoDTO {
    private Long valor;
    private String tipo;
    private String descricao;
//    @JsonProperty("realizada_em")
    private LocalDateTime realizadaEm;

    public SaldoUltimaTransacaoDTO() {
        this(0L, "c", "", LocalDateTime.now());
    }

    public SaldoUltimaTransacaoDTO(Long valor, String tipo, String descricao, LocalDateTime realizadaEm) {
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
        this.realizadaEm = realizadaEm;
    }

    @Override
    public String toString() {
        return "SaldoUltimaTransacaoDTO{" +
                "valor=" + valor +
                ", tipo='" + tipo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", realizadaEm=" + realizadaEm +
                '}';
    }

    public Long getValor() {
        return valor;
    }

    public void setValor(Long valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getRealizadaEm() {
        return realizadaEm;
    }

    public void setRealizadaEm(LocalDateTime realizadaEm) {
        this.realizadaEm = realizadaEm;
    }
}
