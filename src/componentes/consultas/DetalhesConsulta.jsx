import { useEffect } from "react";
import { formatarData, nomePorId } from "./formatoConsulta";
import "./DetalhesConsulta.css";

export default function DetalhesConsulta({ consulta, pacientes, profissionais, aoFechar, aoEditar }) {
  useEffect(() => {
    function fecharComEsc(evento) {
      if (evento.key === "Escape") aoFechar();
    }
    document.addEventListener("keydown", fecharComEsc);
    return () => document.removeEventListener("keydown", fecharComEsc);
  }, [aoFechar]);

  function fecharSeFundo(evento) {
    if (evento.target === evento.currentTarget) aoFechar();
  }

  return (
    <div
      className="detalhes-consulta"
      role="dialog"
      aria-modal="true"
      aria-labelledby="detalhes-consulta-titulo"
      onClick={fecharSeFundo}
    >
      <div className="detalhes-consulta__corpo">
        <header className="detalhes-consulta__topo">
          <div>
            <p className="detalhes-consulta__etiqueta">{consulta.tipo || "Consulta"}</p>
            <h2 id="detalhes-consulta-titulo">
              {formatarData(consulta.data)} · {consulta.horario || "—"}
            </h2>
          </div>
          <button type="button" className="detalhes-consulta__fechar" onClick={aoFechar}>
            Fechar
          </button>
        </header>
        <dl className="detalhes-consulta__dados">
          <div>
            <dt>Tipo</dt>
            <dd>{consulta.tipo || "Consulta"}</dd>
          </div>
          <div>
            <dt>Paciente</dt>
            <dd>{nomePorId(pacientes, consulta.pacienteId) || "—"}</dd>
          </div>
          <div>
            <dt>Profissional</dt>
            <dd>{nomePorId(profissionais, consulta.profissionalId) || "—"}</dd>
          </div>
          <div>
            <dt>Especialidade</dt>
            <dd>{consulta.especialidade || "—"}</dd>
          </div>
          <div>
            <dt>Status</dt>
            <dd>{consulta.situacao || "—"}</dd>
          </div>
          <div className="detalhes-consulta__largo">
            <dt>Motivo</dt>
            <dd>{consulta.motivo || "—"}</dd>
          </div>
          <div className="detalhes-consulta__largo">
            <dt>Observações</dt>
            <dd>{consulta.observacoesMedicas || "Sem observações."}</dd>
          </div>
        </dl>
        <div className="detalhes-consulta__acoes">
          <button type="button" onClick={() => aoEditar(consulta)}>
            Editar consulta
          </button>
        </div>
      </div>
    </div>
  );
}
