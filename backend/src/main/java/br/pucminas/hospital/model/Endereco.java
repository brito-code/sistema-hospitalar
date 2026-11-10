package br.pucminas.hospital.model;

import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereco do paciente. Nao tem identidade propria: as colunas ficam na tabela do paciente (@Embeddable).
 * UF e CEP sao guardados ja normalizados (UF em maiusculas, CEP so com os 8 digitos).
 */
@Embeddable
public class Endereco {

    public static final String PADRAO_UF =
            "^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$";

    public static final String PADRAO_CEP = "^\\d{5}-?\\d{3}$";

    @Column(length = 120)
    private String logradouro;

    @Column(length = 10)
    private String numero;

    @Column(length = 60)
    private String complemento;

    @Column(length = 80)
    private String bairro;

    @Column(length = 80)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 8)
    private String cep;

    protected Endereco() {
    }

    public Endereco(String logradouro, String numero, String complemento, String bairro, String cidade, String uf,
            String cep) {
        this.logradouro = limpar(logradouro);
        this.numero = limpar(numero);
        this.complemento = limpar(complemento);
        this.bairro = limpar(bairro);
        this.cidade = limpar(cidade);
        this.uf = uf == null ? null : uf.trim().toUpperCase(Locale.ROOT);
        this.cep = cep == null ? null : cep.replaceAll("\\D", "");
    }

    /** 30160011 vira 30160-011. */
    public String getCepFormatado() {
        if (cep == null || cep.length() != 8) {
            return cep;
        }
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    private static String limpar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.isEmpty() ? null : limpo;
    }

    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public String getCep() { return cep; }
}
