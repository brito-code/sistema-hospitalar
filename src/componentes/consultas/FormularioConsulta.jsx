import { useEffect, useState } from "react";
import { horarioConflitante, SITUACOES_CONSULTA, TIPOS_ATENDIMENTO } from "./formatoConsulta";
import "./FormularioConsulta.css";

const FORMULARIO_VAZIO = {
  tipo: "Consulta",
  pacienteId: "",
  profissionalId: "",
  data: "",
  horario: "",
  especialidade: "",
  situacao: "Agendada",
  motivo: "",
  observacoesMedicas: "",
};

export default function FormularioConsulta({
  consulta,
  consultas,
  pacientes,
  profissionais,
  aoSalvar,
  aoCancelar,
}) {
  const [dados, setDados] = useState(() =>
    consulta
      ? {
          tipo: consulta.tipo === "Internação" ? "Internação" : "Consulta",
          pacienteId: String(consulta.pacienteId ?? ""),
          profissionalId: String(consulta.profissionalId ?? ""),
          data: consulta.data ?? "",
          horario: consulta.horario ?? "",
          especialidade: consulta.especialidade ?? "",
          situacao: consulta.situacao ?? "Agendada",
          motivo: consulta.motivo ?? "",
          observacoesMedicas: consulta.observacoesMedicas ?? "",
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
    setDados((atual) => {
      const proximo = { ...atual, [campo]: valor };
      if (campo === "profissionalId") {
        const profissional = profissionais.find((item) => String(item.id) === valor);
        if (profissional?.especialidade) proximo.especialidade = profissional.especialidade;
      }
      return proximo;
    });
    setErros((atual) => ({ ...atual, [campo]: undefined, horario: campo === "horario" ? undefined : atual.horario }));
  }

  function validar() {
    const encontrados = {};
    if (!TIPOS_ATENDIMENTO.includes(dados.tipo)) encontrados.tipo = "Selecione se é consulta ou internação.";
    if (!dados.pacienteId) encontrados.pacienteId = "Selecione o paciente.";
    if (!dados.profissionalId) encontrados.profissionalId = "Selecione o profissional.";
    if (!dados.data) encontrados.data = "Informe a data da consulta.";
    if (!dados.horario) encontrados.horario = "Informe o horário da consulta.";
    if (dados.especialidade.trim().length < 3) encontrados.especialidade = "Informe a especialidade.";
    if (!SITUACOES_CONSULTA.includes(dados.situacao)) encontrados.situacao = "Selecione o status da consulta.";
    if (dados.motivo.trim().length < 3) encontrados.motivo = "Informe o motivo da consulta.";
    if (
      !encontrados.profissionalId &&
      !encontrados.data &&
      !encontrados.horario &&
      horarioConflitante(consultas, {
        profissionalId: dados.profissionalId,
        data: dados.data,
        horario: dados.horario,
        situacao: dados.situacao,
        idIgnorado: consulta?.id,
      })
    ) {
      encontrados.horario = "Este profissional já possui uma consulta nesse dia e horário.";
    }
    return encontrados;
  }

  function enviar(evento) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (Object.keys(encontrados).length > 0) return;
    aoSalvar({
      tipo: dados.tipo,
      pacienteId: Number(dados.pacienteId),
      profissionalId: Number(dados.profissionalId),
      data: dados.data,
      horario: dados.horario,
      especialidade: dados.especialidade.trim(),
      situacao: dados.situacao,
      motivo: dados.motivo.trim(),
      observacoesMedicas: dados.observacoesMedicas.trim(),
    });
  }

  const editando = Boolean(consulta);

  function fecharSeFundo(evento) {
    if (evento.target === evento.currentTarget) aoCancelar();
  }

  return (
    <div
      className="formulario-consulta-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="formulario-consulta-titulo"
      onClick={fecharSeFundo}
    >
      <form className="formulario-consulta" onSubmit={enviar} noValidate>
        <div className="formulario-consulta__cabecalho">
          <h2 id="formulario-consulta-titulo">{editando ? "Editar atendimento" : "Agendar atendimento"}</h2>
          <p>O mesmo profissional não pode ter dois atendimentos no mesmo dia e horário.</p>
        </div>
        <div className="formulario-consulta__grade">
          <label className="formulario-consulta__campo" htmlFor="consulta-tipo">
            <span>Tipo</span>
            <select
              id="consulta-tipo"
              value={dados.tipo}
              aria-invalid={erros.tipo ? "true" : "false"}
              onChange={(evento) => atualizar("tipo", evento.target.value)}
            >
              {TIPOS_ATENDIMENTO.map((tipo) => (
                <option key={tipo} value={tipo}>
                  {tipo}
                </option>
              ))}
            </select>
            {erros.tipo ? <small className="formulario-consulta__erro">{erros.tipo}</small> : null}
          </label>
          <CampoSelecao
            id="consulta-paciente"
            rotulo="Paciente"
            valor={dados.pacienteId}
            erro={erros.pacienteId}
            aoAlterar={(valor) => atualizar("pacienteId", valor)}
            opcoes={pacientes}
            textoVazio="Selecione o paciente"
          />
          <CampoSelecao
            id="consulta-profissional"
            rotulo="Profissional"
            valor={dados.profissionalId}
            erro={erros.profissionalId}
            aoAlterar={(valor) => atualizar("profissionalId", valor)}
            opcoes={profissionais}
            textoVazio="Selecione o profissional"
          />
          <Campo
            id="consulta-data"
            rotulo="Data"
            tipo="date"
            valor={dados.data}
            erro={erros.data}
            aoAlterar={(valor) => atualizar("data", valor)}
          />
          <Campo
            id="consulta-hora"
            rotulo="Hora"
            tipo="time"
            valor={dados.horario}
            erro={erros.horario}
            aoAlterar={(valor) => atualizar("horario", valor)}
          />
          <Campo
            id="consulta-especialidade"
            rotulo="Especialidade"
            valor={dados.especialidade}
            erro={erros.especialidade}
            aoAlterar={(valor) => atualizar("especialidade", valor)}
          />
          <label className="formulario-consulta__campo" htmlFor="consulta-situacao">
            <span>Status</span>
            <select
              id="consulta-situacao"
              value={dados.situacao}
              aria-invalid={erros.situacao ? "true" : "false"}
              onChange={(evento) => atualizar("situacao", evento.target.value)}
            >
              {SITUACOES_CONSULTA.map((situacao) => (
                <option key={situacao} value={situacao}>
                  {situacao}
                </option>
              ))}
            </select>
            {erros.situacao ? <small className="formulario-consulta__erro">{erros.situacao}</small> : null}
          </label>
          <Campo
            id="consulta-motivo"
            rotulo="Motivo"
            valor={dados.motivo}
            erro={erros.motivo}
            largo
            aoAlterar={(valor) => atualizar("motivo", valor)}
          />
          <label className="formulario-consulta__campo formulario-consulta__campo--largo" htmlFor="consulta-observacoes">
            <span>Observações</span>
            <textarea
              id="consulta-observacoes"
              value={dados.observacoesMedicas}
              rows={3}
              onChange={(evento) => atualizar("observacoesMedicas", evento.target.value)}
            />
          </label>
        </div>
        <div className="formulario-consulta__acoes">
          <button type="button" className="formulario-consulta__cancelar" onClick={aoCancelar}>
            Cancelar
          </button>
          <button type="submit" className="formulario-consulta__salvar">
            {editando ? "Salvar alterações" : "Agendar"}
          </button>
        </div>
      </form>
    </div>
  );
}

function Campo({ id, rotulo, tipo = "text", valor, erro, aoAlterar, largo = false }) {
  const idErro = `${id}-erro`;
  return (
    <label
      className={largo ? "formulario-consulta__campo formulario-consulta__campo--largo" : "formulario-consulta__campo"}
      htmlFor={id}
    >
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
        <small id={idErro} className="formulario-consulta__erro" role="alert">
          {erro}
        </small>
      ) : null}
    </label>
  );
}

function CampoSelecao({ id, rotulo, valor, erro, aoAlterar, opcoes, textoVazio }) {
  const idErro = `${id}-erro`;
  return (
    <label className="formulario-consulta__campo" htmlFor={id}>
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
            {opcao.nome}
          </option>
        ))}
      </select>
      {erro ? (
        <small id={idErro} className="formulario-consulta__erro" role="alert">
          {erro}
        </small>
      ) : null}
    </label>
  );
}
