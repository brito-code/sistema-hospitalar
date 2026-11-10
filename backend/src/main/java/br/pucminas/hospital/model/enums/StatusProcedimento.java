package br.pucminas.hospital.model.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida do procedimento cirurgico. Nao existe AGENDADO -> CONCLUIDO:
 * o procedimento so e concluido depois de iniciado.
 */
public enum StatusProcedimento {
    AGENDADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO;

    private Set<StatusProcedimento> destinos() {
        return switch (this) {
            case AGENDADO -> EnumSet.of(EM_ANDAMENTO, CANCELADO);
            case EM_ANDAMENTO -> EnumSet.of(CONCLUIDO);
            case CONCLUIDO, CANCELADO -> EnumSet.noneOf(StatusProcedimento.class);
        };
    }

    public boolean podeIrPara(StatusProcedimento destino) {
        return destino != null && destinos().contains(destino);
    }

    public boolean isFinal() {
        return destinos().isEmpty();
    }
}
