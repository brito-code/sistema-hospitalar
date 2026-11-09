package br.pucminas.hospital.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.pucminas.hospital.model.Consulta;
import br.pucminas.hospital.model.enums.StatusConsulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /** Horario ocupado por atendimento que nao foi cancelado. */
    @Query("""
            select case when count(c) > 0 then true else false end
            from Consulta c
            where c.profissionalResponsavel.id = :profissionalId
              and c.data = :data
              and c.horario = :horario
              and c.status <> br.pucminas.hospital.model.enums.StatusConsulta.CANCELADA
            """)
    boolean existeConflito(@Param("profissionalId") Long profissionalId, @Param("data") LocalDate data,
            @Param("horario") LocalTime horario);

    @Query("""
            select c from Consulta c
            where (:profissionalId is null or c.profissionalResponsavel.id = :profissionalId)
              and (:pacienteId is null or c.paciente.id = :pacienteId)
              and (:status is null or c.status = :status)
              and (:data is null or c.data = :data)
            """)
    List<Consulta> filtrar(@Param("profissionalId") Long profissionalId, @Param("pacienteId") Long pacienteId,
            @Param("status") StatusConsulta status, @Param("data") LocalDate data);
}
