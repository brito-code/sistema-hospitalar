package br.pucminas.hospital.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospital.dto.QuartoRequest;
import br.pucminas.hospital.dto.QuartoResponse;
import br.pucminas.hospital.exception.RecursoNaoEncontradoException;
import br.pucminas.hospital.exception.RegraNegocioException;
import br.pucminas.hospital.model.Quarto;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.repository.QuartoRepository;

@Service
@Transactional
public class QuartoService {

    private final QuartoRepository quartos;

    public QuartoService(QuartoRepository quartos) {
        this.quartos = quartos;
    }

    public QuartoResponse criar(QuartoRequest req) {
        Quarto q = new Quarto(req.numero(), req.andar(), req.capacidadeMaxima(), req.tipo());
        return QuartoResponse.de(quartos.save(q));
    }

    @Transactional(readOnly = true)
    public List<QuartoResponse> listar(SituacaoQuarto situacao) {
        return quartos.findAll().stream()
                .filter(q -> situacao == null || q.getSituacao() == situacao)
                .map(QuartoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public QuartoResponse buscar(Long id) {
        return QuartoResponse.de(obter(id));
    }

    public QuartoResponse atualizar(Long id, QuartoRequest req) {
        Quarto q = quartos.buscarComTrava(id).orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", id));
        // RN5/RN7: nao reduzir a capacidade abaixo da ocupacao atual.
        if (req.capacidadeMaxima() < q.getOcupacaoAtual()) {
            throw new RegraNegocioException("Capacidade maxima menor que a ocupacao atual (" + q.getOcupacaoAtual() + ")");
        }
        q.atualizar(req.numero(), req.andar(), req.capacidadeMaxima(), req.tipo());
        return QuartoResponse.de(q);
    }

    private Quarto obter(Long id) {
        return quartos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", id));
    }
}
