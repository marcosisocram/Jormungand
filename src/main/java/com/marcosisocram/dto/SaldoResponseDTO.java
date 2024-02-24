package com.marcosisocram.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SaldoResponseDTO {
    private Long total;
    private LocalDateTime dataExtrato;
    private Long limite;

    public SaldoResponseDTO() {
        this(null, null, null, null);
    }

    public SaldoResponseDTO(Long total, LocalDateTime dataExtrato, Long limite, List<SaldoUltimaTransacaoDTO> saldoUltimasTransacoesDTO) {
        this.total = total;
        this.dataExtrato = dataExtrato;
        this.limite = limite;
        this.saldoUltimasTransacoesDTO = saldoUltimasTransacoesDTO;
    }

    private List<SaldoUltimaTransacaoDTO> saldoUltimasTransacoesDTO;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public LocalDateTime getDataExtrato() {
        return dataExtrato;
    }

    public void setDataExtrato(LocalDateTime dataExtrato) {
        this.dataExtrato = dataExtrato;
    }

    public Long getLimite() {
        return limite;
    }

    public void setLimite(Long limite) {
        this.limite = limite;
    }

    public List<SaldoUltimaTransacaoDTO> getSaldoUltimasTransacoesDTO() {
        return saldoUltimasTransacoesDTO;
    }

    public void setSaldoUltimasTransacoesDTO(List<SaldoUltimaTransacaoDTO> saldoUltimasTransacoesDTO) {
        this.saldoUltimasTransacoesDTO = saldoUltimasTransacoesDTO;
    }

    public String toJson() {

        StringBuilder ultimasTransacoes = new StringBuilder();
        for (int i = 0; i < this.saldoUltimasTransacoesDTO.size(); i++) {


            final SaldoUltimaTransacaoDTO saldoUltimaTransacaoDTO = saldoUltimasTransacoesDTO.get(i);
            ultimasTransacoes.append("{\"valor\": ")
                    .append(saldoUltimaTransacaoDTO.getValor())
                .append(", \"tipo\": \"")
                    .append(saldoUltimaTransacaoDTO.getTipo())
                .append("\", \"descricao\": \"")
                    .append(saldoUltimaTransacaoDTO.getDescricao())
                .append("\", \"realizada_em\": \"")
                    .append(saldoUltimaTransacaoDTO.getRealizadaEm().toString())
                .append("\" }");
            if(i + 1 < this.saldoUltimasTransacoesDTO.size()) {
                ultimasTransacoes.append(",");
            }
        }

        return "{ \"saldo\": { \"limite\": " + this.limite+ ", \"total\": " + this.total + ", \"data_extrato\": \" " + this.dataExtrato.toString() + " \"}, \"ultimas_transacoes\": ["+ ultimasTransacoes +"]}";
    }

    @Override
    public String toString() {
        return "SaldoResponseDTO{" +
                "total=" + total +
                ", dataExtrato=" + dataExtrato +
                ", limite=" + limite +
                ", saldoUltimasTransacoesDTO=" + saldoUltimasTransacoesDTO +
                '}';
    }
}
