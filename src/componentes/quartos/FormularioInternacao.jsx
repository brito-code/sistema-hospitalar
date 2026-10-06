import { useEffect, useState } from "react";
import { ocupacaoAtiva, quartoPorId, quartoSemVaga } from "./formatoQuarto";
import "./painelLeitos.css";

const FORMULARIO_VAZIO = {
  pacienteId: "",
  profissionalId: "",
  quartoId: "",
  dataEntrada: "",
  dataPrevistaAlta: "",
  dataEfetivaAlta: "",
  observacoes: "",
};

export default function FormularioInternacao({
  internacao,
  internacoes,
  pacientes,
  profissionais,
  quartos,
  aoSalvar,
  aoCancelar,
}) {
  const [dados, setDados] = useState(() =>
    internacao
      ? {
          pacienteId: String(internacao.pacienteId ?? ""),
          profissionalId: String(internacao.profissionalId ?? ""),
          quartoId: String(internacao.quartoId ?? ""),
          dataEntrada: internacao.dataEntrada ?? "",
          dataPrevistaAlta: internacao.dataPrevistaAlta ?? "",
          dataEfetivaAlta: internacao.dataEfetivaAlta ?? "",
          observacoes: internacao.observacoes ?? "",
        }
      : FORMULARIO_VAZIO,
  );
  const [erros, setErros] = useState({});

  useEffect(() => {
    function fecharComEsc(evento) {
      if (evento.key === "Escape") aoCancelar();
    }
    document.addEventListener("keydown", fecharComEsc);
    return () => document.removeEventListener("keydown", fecharComEsc);
  }, [aoCancelar]);

  function atualizar(campo, valor) {
    setDados((atual) => ({ ...atual, [campo]: valor }));
    setErros((atual) => ({ ...atual, [campo]: undefined, quartoId: campo === "quartoId" ? undefined : atual.quartoId }));
  }

  function validar() {
    const encontrados = {};
    if (!dados.pacienteId) encontrados.pacienteId = "Selecione o paciente.";
    if (!dados.profissionalId) encontrados.profissionalId = "Selecione o profissional.";
    if (!dados.quartoId) encontrados.quartoId = "Selecione o quarto.";
    if (!dados.dataEntrada) encontrados.dataEntrada = "Informe a data de entrada.";
    if (!dados.dataPrevistaAlta) encontrados.dataPrevistaAlta = "Informe a previsão de alta.";
    else if (dados.dataEntrada && dados.dataPrevistaAlta < dados.dataEntrada) {
      encontrados.dataPrevistaAlta = "A previsão de alta não pode ser anterior à entrada.";
    }
    if (dados.dataEfetivaAlta && dados.dataEntrada && dados.dataEfetivaAlta < dados.dataEntrada) {
      encontrados.dataEfetivaAlta = "A alta efetiva não pode ser anterior à entrada.";
    }
    if (
      !encontrados.quartoId &&
      quartoSemVaga(quartos, internacoes, {
        quartoId: dados.quartoId,
        idIgnorado: internacao?.id,
        dataEfetivaAlta: dados.dataEfetivaAlta,
      })
    ) {
      const quarto = quartoPorId(quartos, dados.quartoId);
      encontrados.quartoId = `O quarto ${quarto?.numeroIdentificacao ?? ""} já atingiu a capacidade de ${quarto?.capacidadeMaxima ?? ""} leito(s).`;
    }
    return encontrados;
  }

  function enviar(evento) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (Object.keys(encontrados).length > 0) return;
    aoSalvar({
      pacienteId: Number(dados.pacienteId),
      profissionalId: Number(dados.profissionalId),
      quartoId: Number(dados.quartoId),
      dataEntrada: dados.dataEntrada,
      dataPrevistaAlta: dados.dataPrevistaAlta,
      dataEfetivaAlta: dados.dataEfetivaAlta || null,
      observacoes: dados.observacoes.trim(),
    });
  }

  const editando = Boolean(internacao);

  function fecharSeFundo(evento) {
    if (evento.target === evento.currentTarget) aoCancelar();
  }

  return (
    <div
      className="formulario-internacao-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="formulario-internacao-titulo"
      onClick={fecharSeFundo}
    >
      <form className="formulario-internacao" onSubmit={enviar} noValidate>
        <div className="formulario-internacao__cabecalho">
          <h2 id="formulario-internacao-titulo">{editando ? "Editar internação" : "Registrar internação"}</h2>
          <p>Só é possível internar em quarto com leito livre. A alta efetiva libera a vaga.</p>
        </div>
        <div className="formulario-internacao__grade">
          <CampoSelecao
            id="internacao-paciente"
            rotulo="Paciente"
            valor={dados.pacienteId}
            erro={erros.pacienteId}
            aoAlterar={(valor) => atualizar("pacienteId", valor)}
            opcoes={pacientes.map((paciente) => ({ id: paciente.id, rotulo: paciente.nome }))}
            textoVazio="Selecione o paciente"
          />
          <CampoSelecao
            id="internacao-profissional"
            rotulo="Profissional"
            valor={dados.profissionalId}
            erro={erros.profissionalId}
            aoAlterar={(valor) => atualizar("profissionalId", valor)}
            opcoes={profissionais.map((profissional) => ({ id: profissional.id, rotulo: profissional.nome }))}
            textoVazio="Selecione o profissional"
          />
          <CampoSelecao
            id="internacao-quarto"
            rotulo="Quarto"
            valor={dados.quartoId}
            erro={erros.quartoId}
            aoAlterar={(valor) => atualizar("quartoId", valor)}
            opcoes={quartos.map((quarto) => ({ id: quarto.id, rotulo: rotuloQuarto(quarto, internacoes, internacao?.id) }))}
            textoVazio="Selecione o quarto"
            largo
          />
          <Campo
            id="internacao-entrada"
            rotulo="Data de entrada"
            tipo="date"
            valor={dados.dataEntrada}
            erro={erros.dataEntrada}
            aoAlterar={(valor) => atualizar("dataEntrada", valor)}
          />
          <Campo
            id="internacao-prevista"
            rotulo="Previsão de alta"
            tipo="date"
            valor={dados.dataPrevistaAlta}
            erro={erros.dataPrevistaAlta}
            aoAlterar={(valor) => atualizar("dataPrevistaAlta", valor)}
          />
          <Campo
            id="internacao-efetiva"
            rotulo="Alta efetiva"
            tipo="date"
            valor={dados.dataEfetivaAlta}
            erro={erros.dataEfetivaAlta}
            aoAlterar={(valor) => atualizar("dataEfetivaAlta", valor)}
          />
          <label className="formulario-internacao__campo formulario-internacao__campo--largo" htmlFor="internacao-observacoes">
            <span>Observações</span>
            <textarea
              id="internacao-observacoes"
              value={dados.observacoes}
              rows={3}
              onChange={(evento) => atualizar("observacoes", evento.target.value)}
            />
          </label>
        </div>
        <div className="formulario-internacao__acoes">
          <button type="button" className="formulario-internacao__cancelar" onClick={aoCancelar}>
            Cancelar
          </button>
          <button type="submit" className="formulario-internacao__salvar">
            {editando ? "Salvar alterações" : "Registrar"}
          </button>
        </div>
      </form>
    </div>
  );
}

function rotuloQuarto(quarto, internacoes, idIgnorado) {
  const ocupacao = ocupacaoAtiva(internacoes, quarto.id, idIgnorado);
  const lotado = ocupacao >= Number(quarto.capacidadeMaxima);
  return `Quarto ${quarto.numeroIdentificacao} · ${ocupacao}/${quarto.capacidadeMaxima}${lotado ? " · lotado" : ""}`;
}

function Campo({ id, rotulo, tipo = "text", valor, erro, aoAlterar }) {
  const idErro = `${id}-erro`;
  return (
    <label className="formulario-internacao__campo" htmlFor={id}>
      <span>{rotulo}</span>
      <input
        id={id}
        type={tipo}
        value={valor}
        aria-invalid={erro ? "true" : "false"}
        aria-describedby={erro ? idErro : undefined}
        onChange={(evento) => aoAlterar(evento.target.value)}
      />
      {erro ? (
        <small id={idErro} className="formulario-internacao__erro" role="alert">
          {erro}
        </small>
      ) : null}
    </label>
  );
}

function CampoSelecao({ id, rotulo, valor, erro, aoAlterar, opcoes, textoVazio, largo = false }) {
  const idErro = `${id}-erro`;
  return (
    <label className={largo ? "formulario-internacao__campo formulario-internacao__campo--largo" : "formulario-internacao__campo"} htmlFor={id}>
      <span>{rotulo}</span>
      <select
        id={id}
        value={valor}
        aria-invalid={erro ? "true" : "false"}
        aria-describedby={erro ? idErro : undefined}
        onChange={(evento) => aoAlterar(evento.target.value)}
      >
        <option value="">{textoVazio}</option>
        {opcoes.map((opcao) => (
          <option key={opcao.id} value={opcao.id}>
            {opcao.rotulo}
          </option>
        ))}
      </select>
      {erro ? (
        <small id={idErro} className="formulario-internacao__erro" role="alert">
          {erro}
        </small>
      ) : null}
    </label>
  );
}
