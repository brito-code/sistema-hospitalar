package br.pucminas.hospital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.pucminas.hospital.model.MedicoEspecialista;
import br.pucminas.hospital.model.ProfissionalSaude;
import br.pucminas.hospital.model.enums.Especialidade;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, Long> {

    @Query("select m from MedicoEspecialista m where m.especialidade = :especialidade order by m.nome")
    List<MedicoEspecialista> buscarEspecialistas(@Param("especialidade") Especialidade especialidade);

    boolean existsByRegistroProfissionalIgnoreCase(String registroProfissional);

    boolean existsByRegistroProfissionalIgnoreCaseAndIdNot(String registroProfissional, Long id);

    /** Verdadeiro se o profissional e responsavel por algum atendimento (consulta, internacao...). */
    @Query("""
            select case when count(a) > 0 then true else false end
            from Atendimento a
            where a.profissionalResponsavel.id = :id
            """)
    boolean possuiAtendimentos(@Param("id") Long id);
}
