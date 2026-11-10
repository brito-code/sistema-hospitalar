package br.pucminas.hospital.controller;

import java.net.URI;
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

import br.pucminas.hospital.dto.ClassificacaoRequest;
import br.pucminas.hospital.dto.ConclusaoRequest;
import br.pucminas.hospital.dto.EmergenciaRequest;
import br.pucminas.hospital.dto.EmergenciaResponse;
import br.pucminas.hospital.model.enums.StatusEmergencia;
import br.pucminas.hospital.service.EmergenciaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/emergencias")
public class EmergenciaController {

    private final EmergenciaService service;

    public EmergenciaController(EmergenciaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EmergenciaResponse> registrar(@Valid @RequestBody EmergenciaRequest req) {
        EmergenciaResponse criada = service.registrar(req);
        return ResponseEntity.created(URI.create("/emergencias/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<EmergenciaResponse> listar(@RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) StatusEmergencia status) {
        return service.listar(pacienteId, status);
    }

    /** Fila de espera ordenada por prioridade (CRITICA primeiro) e, no empate, por ordem de chegada. */
    @GetMapping("/aguardando")
    public List<EmergenciaResponse> listarAguardando() {
        return service.listarAguardando();
    }

    @GetMapping("/{id}")
    public EmergenciaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/classificar")
    public EmergenciaResponse classificar(@PathVariable Long id, @Valid @RequestBody ClassificacaoRequest req) {
        return service.classificar(id, req);
    }

    @PatchMapping("/{id}/iniciar")
    public EmergenciaResponse iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PatchMapping("/{id}/concluir")
    public EmergenciaResponse concluir(@PathVariable Long id, @RequestBody(required = false) ConclusaoRequest req) {
        return service.concluir(id, req);
    }
}
