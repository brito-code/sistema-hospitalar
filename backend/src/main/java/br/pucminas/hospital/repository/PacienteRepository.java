package br.pucminas.hospital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospital.model.Paciente;

// TODO(dono: Paciente): criado so para a internacao localizar o paciente; unificar no merge.
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
