package br.pucminas.hospital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import br.pucminas.hospital.model.Quarto;
import jakarta.persistence.LockModeType;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    /** Trava o quarto para evitar que duas internacoes simultaneas ultrapassem a capacidade (RN5/RN7). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from Quarto q where q.id = :id")
    Optional<Quarto> buscarComTrava(Long id);
}
