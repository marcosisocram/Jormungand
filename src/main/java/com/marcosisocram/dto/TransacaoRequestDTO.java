package com.marcosisocram.dto;

public class TransacaoRequestDTO {
    private Long valor;
    private String tipo;
    private String descricao;

    public TransacaoRequestDTO() {
        this(null, null, null);
    }

    public TransacaoRequestDTO(Long valor, String tipo, String descricao) {
        this.valor = valor;
        this.tipo = tipo;
        this.descricao = descricao;
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

    @Override
    public String toString() {
        return "TransacaoRequestDTO{" +
                "valor=" + valor +
                ", tipo='" + tipo + '\'' +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}
