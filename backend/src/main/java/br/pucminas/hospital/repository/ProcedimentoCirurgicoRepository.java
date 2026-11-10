package br.pucminas.hospital.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.pucminas.hospital.model.ProcedimentoCirurgico;
import br.pucminas.hospital.model.enums.StatusProcedimento;

public interface ProcedimentoCirurgicoRepository extends JpaRepository<ProcedimentoCirurgico, Long> {

    /** Horario ocupado por procedimento que nao foi cancelado. */
    @Query("""
            select case when count(p) > 0 then true else false end
            from ProcedimentoCirurgico p
            where p.profissionalResponsavel.id = :profissionalId
              and p.dataHoraPrevista = :dataHora
              and p.status <> br.pucminas.hospital.model.enums.StatusProcedimento.CANCELADO
            """)
    boolean existeConflito(@Param("profissionalId") Long profissionalId, @Param("dataHora") LocalDateTime dataHora);

    @Query("""
            select p from ProcedimentoCirurgico p
            where (:profissionalId is null or p.profissionalResponsavel.id = :profissionalId)
              and (:pacienteId is null or p.paciente.id = :pacienteId)
              and (:status is null or p.status = :status)
            order by p.dataHoraPrevista
            """)
    List<ProcedimentoCirurgico> filtrar(@Param("profissionalId") Long profissionalId,
            @Param("pacienteId") Long pacienteId, @Param("status") StatusProcedimento status);
}
