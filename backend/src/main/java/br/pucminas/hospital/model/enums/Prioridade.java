package br.pucminas.hospital.model.enums;

/**
 * Classificacao de risco da emergencia, da menos para a mais grave.
 * A ordem das constantes importa: {@link #getNivel()} usa o ordinal para ordenar a fila.
 */
public enum Prioridade {
    BAIXA, MODERADA, ALTA, CRITICA;

    /** Quanto maior, mais urgente. */
    public int getNivel() {
        return ordinal();
    }
}
