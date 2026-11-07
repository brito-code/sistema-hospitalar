package br.pucminas.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospital.model.Internacao;

public interface InternacaoRepository extends JpaRepository<Internacao, Long> {
}
