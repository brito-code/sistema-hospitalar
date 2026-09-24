import { useEffect } from "react";
import "./DetalhesProfissional.css";

export default function DetalhesProfissional({ profissional, aoFechar, aoEditar }) {
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
      className="detalhes-profissional"
      role="dialog"
      aria-modal="true"
      aria-labelledby="detalhes-profissional-titulo"
      onClick={fecharSeFundo}
    >
      <div className="detalhes-profissional__corpo">
        <header className="detalhes-profissional__topo">
          <div>
            <p className="detalhes-profissional__etiqueta">Equipe de saúde</p>
            <h2 id="detalhes-profissional-titulo">{profissional.nome || "Profissional"}</h2>
          </div>
          <button type="button" className="detalhes-profissional__fechar" onClick={aoFechar}>
            Fechar
          </button>
        </header>

        <dl className="detalhes-profissional__dados">
          <div>
            <dt>CRM/COREN</dt>
            <dd>{profissional.registroProfissional || "—"}</dd>
          </div>
          <div>
            <dt>Especialidade</dt>
            <dd>{profissional.especialidade || "—"}</dd>
          </div>
          <div>
            <dt>Telefone</dt>
            <dd>{profissional.telefone || "—"}</dd>
          </div>
          <div>
            <dt>E-mail</dt>
            <dd>{profissional.email || "—"}</dd>
          </div>
        </dl>

        <div className="detalhes-profissional__acoes">
          <button type="button" onClick={() => aoEditar(profissional)}>
            Editar profissional
          </button>
        </div>
      </div>
    </div>
  );
}
