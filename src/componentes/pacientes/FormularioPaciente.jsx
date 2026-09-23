import { useEffect, useState } from "react";
import {
  cpfValido,
  digitos,
  emailValido,
  formatarCpf,
  formatarTelefone,
} from "./formatoPaciente";
import "./FormularioPaciente.css";

const FORMULARIO_VAZIO = {
  nome: "",
  cpf: "",
  dataNascimento: "",
  telefone: "",
  endereco: "",
  email: "",
};

function dataNascimentoValida(valor) {
  if (!valor) return false;
  const nascimento = new Date(`${valor}T00:00:00`);
  if (Number.isNaN(nascimento.getTime())) return false;
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  const limite = new Date("1900-01-01T00:00:00");
  return nascimento >= limite && nascimento <= hoje;
}

export default function FormularioPaciente({ paciente, pacientes, aoSalvar, aoCancelar }) {
  const [dados, setDados] = useState(() =>
    paciente
      ? {
          nome: paciente.nome ?? "",
          cpf: paciente.cpf ?? "",
          dataNascimento: paciente.dataNascimento ?? "",
          telefone: paciente.telefone ?? "",
          endereco: paciente.endereco ?? "",
          email: paciente.email ?? "",
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
    setErros((atual) => ({ ...atual, [campo]: undefined }));
  }

  function validar() {
    const encontrados = {};
    const nome = dados.nome.trim();
    const endereco = dados.endereco.trim();
    const email = dados.email.trim().toLowerCase();
    const cpf = digitos(dados.cpf);
    const telefone = digitos(dados.telefone);
    const outros = pacientes.filter((item) => item.id !== paciente?.id);

    if (nome.length < 3) encontrados.nome = "Informe o nome completo do paciente.";
    if (!cpfValido(dados.cpf)) encontrados.cpf = "Informe um CPF válido.";
    else if (outros.some((item) => digitos(item.cpf) === cpf)) {
      encontrados.cpf = "Já existe um paciente com este CPF.";
    }
    if (!dataNascimentoValida(dados.dataNascimento)) {
      encontrados.dataNascimento = "Informe uma data de nascimento válida.";
    }
    if (telefone.length < 10) encontrados.telefone = "Informe um telefone com DDD.";
    if (endereco.length < 5) encontrados.endereco = "Informe o endereço do paciente.";
    if (!emailValido(email)) encontrados.email = "Informe um e-mail válido.";
    else if (outros.some((item) => String(item.email).trim().toLowerCase() === email)) {
      encontrados.email = "Já existe um paciente com este e-mail.";
    }

    return encontrados;
  }

  function enviar(evento) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (Object.keys(encontrados).length > 0) return;

    aoSalvar({
      nome: dados.nome.trim(),
      cpf: formatarCpf(dados.cpf),
      dataNascimento: dados.dataNascimento,
      telefone: formatarTelefone(dados.telefone),
      endereco: dados.endereco.trim(),
      email: dados.email.trim().toLowerCase(),
    });
  }

  const editando = Boolean(paciente);

  function fecharSeFundo(evento) {
    if (evento.target === evento.currentTarget) aoCancelar();
  }

  return (
    <div
      className="formulario-paciente-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="formulario-paciente-titulo"
      onClick={fecharSeFundo}
    >
      <form className="formulario-paciente" onSubmit={enviar} noValidate>
      <div className="formulario-paciente__cabecalho">
        <h2 id="formulario-paciente-titulo">{editando ? "Editar paciente" : "Cadastrar paciente"}</h2>
        <p>Preencha os dados de identificação e contato.</p>
      </div>
      <div className="formulario-paciente__grade">
        <Campo
          id="paciente-nome"
          rotulo="Nome"
          valor={dados.nome}
          erro={erros.nome}
          aoAlterar={(valor) => atualizar("nome", valor)}
        />
        <Campo
          id="paciente-cpf"
          rotulo="CPF"
          valor={dados.cpf}
          erro={erros.cpf}
          modoEntrada="numeric"
          aoAlterar={(valor) => atualizar("cpf", formatarCpf(valor))}
        />
        <Campo
          id="paciente-nascimento"
          rotulo="Data de nascimento"
          tipo="date"
          valor={dados.dataNascimento}
          erro={erros.dataNascimento}
          aoAlterar={(valor) => atualizar("dataNascimento", valor)}
        />
        <Campo
          id="paciente-telefone"
          rotulo="Telefone"
          valor={dados.telefone}
          erro={erros.telefone}
          modoEntrada="tel"
          aoAlterar={(valor) => atualizar("telefone", formatarTelefone(valor))}
        />
        <Campo
          id="paciente-email"
          rotulo="E-mail"
          tipo="email"
          valor={dados.email}
          erro={erros.email}
          aoAlterar={(valor) => atualizar("email", valor)}
        />
        <Campo
          id="paciente-endereco"
          rotulo="Endereço"
          valor={dados.endereco}
          erro={erros.endereco}
          largo
          aoAlterar={(valor) => atualizar("endereco", valor)}
        />
      </div>
      <div className="formulario-paciente__acoes">
        <button type="button" className="formulario-paciente__cancelar" onClick={aoCancelar}>
          Cancelar
        </button>
        <button type="submit" className="formulario-paciente__salvar">
          {editando ? "Salvar alterações" : "Cadastrar"}
        </button>
      </div>
      </form>
    </div>
  );
}

function Campo({ id, rotulo, tipo = "text", valor, erro, aoAlterar, largo = false, modoEntrada }) {
  const idErro = `${id}-erro`;
  return (
    <label className={largo ? "formulario-paciente__campo formulario-paciente__campo--largo" : "formulario-paciente__campo"} htmlFor={id}>
      <span>{rotulo}</span>
      <input
        id={id}
        name={id}
        type={tipo}
        value={valor}
        inputMode={modoEntrada}
        aria-invalid={erro ? "true" : "false"}
        aria-describedby={erro ? idErro : undefined}
        onChange={(evento) => aoAlterar(evento.target.value)}
      />
      {erro ? (
        <small id={idErro} className="formulario-paciente__erro" role="alert">
          {erro}
        </small>
      ) : null}
    </label>
  );
}
