package com.marcosisocram.dto;

public class TransacaoResponseDTO {
    private Long limite;
    private Long saldo;

    public TransacaoResponseDTO() {
        this(0L, 0L);
    }
    public TransacaoResponseDTO(Long limite, Long saldo) {
        this.limite = limite;
        this.saldo = saldo;
    }

    public Long getLimite() {
        return limite;
    }

    public void setLimite(Long limite) {
        this.limite = limite;
    }

    public Long getSaldo() {
        return saldo;
    }

    public void setSaldo(Long saldo) {
        this.saldo = saldo;
    }


    public String toJson() {
        return "{\"saldo\": " + this.saldo + ", \"limite\": " + this.limite + "}";
    }

    @Override
    public String toString() {
        return "TransacaoResponseDTO{" +
                "limite=" + limite +
                ", saldo=" + saldo +
                '}';
    }
}
