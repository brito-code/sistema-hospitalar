package br.pucminas.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospital.model.ProfissionalSaude;

// TODO(dono: Profissional): criado so para a internacao localizar o profissional; unificar no merge.
public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, Long> {
}
