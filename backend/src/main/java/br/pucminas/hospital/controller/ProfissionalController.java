package br.pucminas.hospital.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospital.dto.EncaminhamentoRequest;
import br.pucminas.hospital.dto.EncaminhamentoResponse;
import br.pucminas.hospital.dto.ProfissionalRequest;
import br.pucminas.hospital.dto.ProfissionalResponse;
import br.pucminas.hospital.model.enums.Especialidade;
import br.pucminas.hospital.service.ProfissionalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProfissionalResponse> cadastrar(@Valid @RequestBody ProfissionalRequest req) {
        ProfissionalResponse criado = service.cadastrar(req);
        return ResponseEntity.created(URI.create("/profissionais/" + criado.id())).body(criado);
    }

    @GetMapping
    public List<ProfissionalResponse> listar(@RequestParam(required = false) Especialidade especialidade) {
        return service.listar(especialidade);
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/encaminhamentos")
    public EncaminhamentoResponse encaminhar(@PathVariable Long id, @Valid @RequestBody EncaminhamentoRequest req) {
        return service.encaminhar(id, req);
    }
}
