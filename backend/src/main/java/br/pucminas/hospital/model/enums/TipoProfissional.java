package br.pucminas.hospital.model.enums;

/** Subtipos concretos de profissional. O nome e o discriminador no banco e no JSON. */
public enum TipoProfissional {
    CLINICO_GERAL, ESPECIALISTA, CIRURGIAO;

    /** Constantes para uso em anotacoes, que nao aceitam {@code name()}. */
    public static final class Valor {
        public static final String CLINICO_GERAL = "CLINICO_GERAL";
        public static final String ESPECIALISTA = "ESPECIALISTA";
        public static final String CIRURGIAO = "CIRURGIAO";

        private Valor() {
        }
    }
}
