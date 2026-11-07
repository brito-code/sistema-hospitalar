package br.pucminas.hospital.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospital.dto.QuartoRequest;
import br.pucminas.hospital.dto.QuartoResponse;
import br.pucminas.hospital.model.enums.SituacaoQuarto;
import br.pucminas.hospital.service.QuartoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService service;

    public QuartoController(QuartoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<QuartoResponse> criar(@Valid @RequestBody QuartoRequest req) {
        QuartoResponse criado = service.criar(req);
        return ResponseEntity.created(URI.create("/quartos/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<QuartoResponse> listar(@RequestParam(required = false) SituacaoQuarto situacao) {
        return service.listar(situacao);
    }

    @GetMapping("/{id}")
    public QuartoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public QuartoResponse atualizar(@PathVariable Long id, @Valid @RequestBody QuartoRequest req) {
        return service.atualizar(id, req);
    }
}
