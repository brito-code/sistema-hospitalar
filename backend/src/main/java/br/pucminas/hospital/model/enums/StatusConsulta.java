package br.pucminas.hospital.model.enums;

import java.util.EnumSet;
import java.util.Set;

public enum StatusConsulta {
    AGENDADA, CONFIRMADA, EM_ATENDIMENTO, CONCLUIDA, CANCELADA, NAO_COMPARECEU;

    /** Destinos permitidos a partir de cada estado. Estados finais nao transitam. */
    private Set<StatusConsulta> destinos() {
        return switch (this) {
            case AGENDADA -> EnumSet.of(CONFIRMADA, CANCELADA, NAO_COMPARECEU);
            case CONFIRMADA -> EnumSet.of(EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU);
            case EM_ATENDIMENTO -> EnumSet.of(CONCLUIDA);
            case CONCLUIDA, CANCELADA, NAO_COMPARECEU -> EnumSet.noneOf(StatusConsulta.class);
        };
    }

    public boolean podeIrPara(StatusConsulta destino) {
        return destino != null && destinos().contains(destino);
    }

    public boolean isFinal() {
        return destinos().isEmpty();
    }
}
