import { useEffect } from "react";
import { ocupacaoAtiva, rotuloSituacao } from "./formatoQuarto";
import "./painelLeitos.css";

export default function DetalhesQuarto({ quarto, internacoes, aoFechar, aoEditar }) {
  const ocupacao = ocupacaoAtiva(internacoes, quarto.id);

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
    <div className="detalhes-quarto" role="dialog" aria-modal="true" aria-labelledby="detalhes-quarto-titulo" onClick={fecharSeFundo}>
      <div className="detalhes-quarto__corpo">
        <header className="detalhes-quarto__topo">
          <div>
            <p className="detalhes-quarto__etiqueta">{rotuloSituacao(quarto.situacao)}</p>
            <h2 id="detalhes-quarto-titulo">Quarto {quarto.numeroIdentificacao || "—"}</h2>
          </div>
          <button type="button" className="detalhes-quarto__fechar" onClick={aoFechar}>
            Fechar
          </button>
        </header>
        <dl className="detalhes-quarto__dados">
          <div>
            <dt>Número</dt>
            <dd>{quarto.numeroIdentificacao || "—"}</dd>
          </div>
          <div>
            <dt>Andar</dt>
            <dd>{quarto.andar ?? "—"}</dd>
          </div>
          <div>
            <dt>Capacidade</dt>
            <dd>{quarto.capacidadeMaxima ?? "—"}</dd>
          </div>
          <div>
            <dt>Ocupação</dt>
            <dd>
              {ocupacao}/{quarto.capacidadeMaxima ?? "—"}
            </dd>
          </div>
          <div className="detalhes-quarto__largo">
            <dt>Status</dt>
            <dd>{rotuloSituacao(quarto.situacao)}</dd>
          </div>
        </dl>
        <div className="detalhes-quarto__acoes">
          <button type="button" onClick={() => aoEditar(quarto)}>
            Editar quarto
          </button>
        </div>
      </div>
    </div>
  );
}
