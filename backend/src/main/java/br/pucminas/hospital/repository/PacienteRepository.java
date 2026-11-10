package br.pucminas.hospital.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.pucminas.hospital.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    List<Paciente> findByNomeContainingIgnoreCase(String nome, Sort sort);

    /** Verdadeiro se o paciente tem algum atendimento (consulta, internacao...) no historico. */
    @Query("""
            select case when count(a) > 0 then true else false end
            from Atendimento a
            where a.paciente.id = :id
            """)
    boolean possuiAtendimentos(@Param("id") Long id);
}
