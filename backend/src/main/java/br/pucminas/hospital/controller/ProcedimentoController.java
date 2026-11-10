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

import br.pucminas.hospital.dto.ConclusaoProcedimentoRequest;
import br.pucminas.hospital.dto.ProcedimentoRequest;
import br.pucminas.hospital.dto.ProcedimentoResponse;
import br.pucminas.hospital.model.enums.StatusProcedimento;
import br.pucminas.hospital.service.ProcedimentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/procedimentos")
public class ProcedimentoController {

    private final ProcedimentoService service;

    public ProcedimentoController(ProcedimentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProcedimentoResponse> agendar(@Valid @RequestBody ProcedimentoRequest req) {
        ProcedimentoResponse criado = service.agendar(req);
        return ResponseEntity.created(URI.create("/procedimentos/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<ProcedimentoResponse> listar(@RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) StatusProcedimento status) {
        return service.listar(profissionalId, pacienteId, status);
    }

    @GetMapping("/{id}")
    public ProcedimentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/iniciar")
    public ProcedimentoResponse iniciar(@PathVariable Long id) {
        return service.iniciar(id);
    }

    @PatchMapping("/{id}/concluir")
    public ProcedimentoResponse concluir(@PathVariable Long id, @RequestBody(required = false) ConclusaoProcedimentoRequest req) {
        return service.concluir(id, req);
    }

    @PatchMapping("/{id}/cancelar")
    public ProcedimentoResponse cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
