import { useEffect } from "react";
import { formatarData, nomePorId, quartoPorId } from "./formatoQuarto";
import "./painelLeitos.css";

export default function DetalhesInternacao({ internacao, pacientes, profissionais, quartos, aoFechar, aoEditar }) {
  const quarto = quartoPorId(quartos, internacao.quartoId);

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
      className="detalhes-internacao"
      role="dialog"
      aria-modal="true"
      aria-labelledby="detalhes-internacao-titulo"
      onClick={fecharSeFundo}
    >
      <div className="detalhes-internacao__corpo">
        <header className="detalhes-internacao__topo">
          <div>
            <p className="detalhes-internacao__etiqueta">{internacao.dataEfetivaAlta ? "Alta registrada" : "Em aberto"}</p>
            <h2 id="detalhes-internacao-titulo">{nomePorId(pacientes, internacao.pacienteId) || "Internação"}</h2>
          </div>
          <button type="button" className="detalhes-internacao__fechar" onClick={aoFechar}>
            Fechar
          </button>
        </header>
        <dl className="detalhes-internacao__dados">
          <div>
            <dt>Paciente</dt>
            <dd>{nomePorId(pacientes, internacao.pacienteId) || "—"}</dd>
          </div>
          <div>
            <dt>Profissional</dt>
            <dd>{nomePorId(profissionais, internacao.profissionalId) || "—"}</dd>
          </div>
          <div>
            <dt>Quarto</dt>
            <dd>{quarto ? `${quarto.numeroIdentificacao} · ${quarto.andar}º andar` : "—"}</dd>
          </div>
          <div>
            <dt>Data de entrada</dt>
            <dd>{formatarData(internacao.dataEntrada)}</dd>
          </div>
          <div>
            <dt>Previsão de alta</dt>
            <dd>{formatarData(internacao.dataPrevistaAlta)}</dd>
          </div>
          <div>
            <dt>Alta efetiva</dt>
            <dd>{internacao.dataEfetivaAlta ? formatarData(internacao.dataEfetivaAlta) : "Em aberto"}</dd>
          </div>
          <div className="detalhes-internacao__largo">
            <dt>Observações</dt>
            <dd>{internacao.observacoes || "Sem observações."}</dd>
          </div>
        </dl>
        <div className="detalhes-internacao__acoes">
          <button type="button" onClick={() => aoEditar(internacao)}>
            Editar internação
          </button>
        </div>
      </div>
    </div>
  );
}
