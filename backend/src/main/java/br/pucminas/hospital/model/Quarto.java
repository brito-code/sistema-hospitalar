package br.pucminas.hospital.model;

import java.util.ArrayList;
import java.util.List;

import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.model.enums.TipoQuarto;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private int andar;
    private int capacidadeMaxima;

    @Enumerated(EnumType.STRING)
    private TipoQuarto tipo;

    @OneToMany(mappedBy = "quarto")
    private List<Internacao> internacoes = new ArrayList<>();

    protected Quarto() {
    }

    public Quarto(String numero, int andar, int capacidadeMaxima, TipoQuarto tipo) {
        this.numero = numero;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        this.tipo = tipo;
    }

    /** Quantidade de internacoes ativas (sem alta efetiva). */
    public int getOcupacaoAtual() {
        return (int) internacoes.stream().filter(Internacao::isAtiva).count();
    }

    public boolean temVaga() {
        return getOcupacaoAtual() < capacidadeMaxima;
    }

    /** Derivada da ocupacao; nao e armazenada. */
    public SituacaoQuarto getSituacao() {
        return temVaga() ? SituacaoQuarto.DISPONIVEL : SituacaoQuarto.OCUPADO;
    }

    public void atualizar(String numero, int andar, int capacidadeMaxima, TipoQuarto tipo) {
        this.numero = numero;
        this.andar = andar;
        this.capacidadeMaxima = capacidadeMaxima;
        this.tipo = tipo;
    }

    void adicionarInternacao(Internacao internacao) {
        internacoes.add(internacao);
    }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public int getAndar() { return andar; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public TipoQuarto getTipo() { return tipo; }
    public List<Internacao> getInternacoes() { return List.copyOf(internacoes); }
}
