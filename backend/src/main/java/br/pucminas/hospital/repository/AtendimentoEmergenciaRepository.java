package br.pucminas.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.pucminas.hospital.model.AtendimentoEmergencia;
import br.pucminas.hospital.model.enums.StatusEmergencia;

public interface AtendimentoEmergenciaRepository extends JpaRepository<AtendimentoEmergencia, Long> {

    @Query("""
            select e from AtendimentoEmergencia e
            where (:pacienteId is null or e.paciente.id = :pacienteId)
              and (:status is null or e.status = :status)
            order by e.dataHoraChegada
            """)
    List<AtendimentoEmergencia> filtrar(@Param("pacienteId") Long pacienteId,
            @Param("status") StatusEmergencia status);

    List<AtendimentoEmergencia> findByStatus(StatusEmergencia status);
}
