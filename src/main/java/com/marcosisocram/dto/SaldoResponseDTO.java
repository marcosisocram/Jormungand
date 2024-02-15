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
