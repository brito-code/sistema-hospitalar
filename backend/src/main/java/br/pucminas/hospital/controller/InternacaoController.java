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
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospital.dto.AltaRequest;
import br.pucminas.hospital.dto.InternacaoRequest;
import br.pucminas.hospital.dto.InternacaoResponse;
import br.pucminas.hospital.service.InternacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService service;

    public InternacaoController(InternacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<InternacaoResponse> internar(@Valid @RequestBody InternacaoRequest req) {
        InternacaoResponse criada = service.internar(req);
        return ResponseEntity.created(URI.create("/internacoes/" + criada.id())).body(criada);
    }

    @GetMapping
    public List<InternacaoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public InternacaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PatchMapping("/{id}/alta")
    public InternacaoResponse darAlta(@PathVariable Long id, @RequestBody(required = false) AltaRequest req) {
        return service.darAlta(id, req);
    }
}
