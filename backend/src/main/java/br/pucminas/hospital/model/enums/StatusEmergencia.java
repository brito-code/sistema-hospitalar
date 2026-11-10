package br.pucminas.hospital.model.enums;

import java.util.EnumSet;
import java.util.Set;

/** Ciclo de vida do atendimento de emergencia. As transicoes ficam no enum, como em StatusConsulta. */
public enum StatusEmergencia {
    AGUARDANDO_AVALIACAO, EM_ATENDIMENTO, CONCLUIDO;

    private Set<StatusEmergencia> destinos() {
        return switch (this) {
            case AGUARDANDO_AVALIACAO -> EnumSet.of(EM_ATENDIMENTO);
            case EM_ATENDIMENTO -> EnumSet.of(CONCLUIDO);
            case CONCLUIDO -> EnumSet.noneOf(StatusEmergencia.class);
        };
    }

    public boolean podeIrPara(StatusEmergencia destino) {
        return destino != null && destinos().contains(destino);
    }

    public boolean isFinal() {
        return destinos().isEmpty();
    }
}
