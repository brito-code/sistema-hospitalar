package br.pucminas.hospital.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospital.dto.ConsultaRequest;
import br.pucminas.hospital.dto.ConsultaResponse;
import br.pucminas.hospital.dto.ReagendarRequest;
import br.pucminas.hospital.model.enums.StatusConsulta;
import br.pucminas.hospital.service.ConsultaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConsultaResponse> agendar(@Valid @RequestBody ConsultaRequest req) {
        ConsultaResponse criada = service.agendar(req);
        return ResponseEntity.created(URI.create("/consultas/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<ConsultaResponse> listar(@RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long pacienteId, @RequestParam(required = false) StatusConsulta status,
            @RequestParam(required = false) LocalDate data) {
        return service.listar(profissionalId, pacienteId, status, data);
    }

    @GetMapping("/{id}")
    public ConsultaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/confirmar")
    public ConsultaResponse confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @PatchMapping("/{id}/cancelar")
    public ConsultaResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PostMapping("/{id}/reagendar")
    public ConsultaResponse reagendar(@PathVariable Long id, @Valid @RequestBody ReagendarRequest req) {
        return service.reagendar(id, req);
    }
}
