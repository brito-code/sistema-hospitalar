package br.pucminas.hospital.dto;

import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.model.enums.TipoQuarto;

public record QuartoResponse(Long id, String numero, int andar, int capacidadeMaxima, TipoQuarto tipo,
        int ocupacaoAtual, SituacaoQuarto situacao) {

    public static QuartoResponse de(Quarto q) {
        return new QuartoResponse(q.getId(), q.getNumero(), q.getAndar(), q.getCapacidadeMaxima(), q.getTipo(),
                q.getOcupacaoAtual(), q.getSituacao());
    }
}
