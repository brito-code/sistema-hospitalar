import { useEffect, useState } from "react";
import {
  digitos,
  emailValido,
  formatarRegistro,
  formatarTelefone,
  registroValido,
} from "./formatoProfissional";
import "./FormularioProfissional.css";

const FORMULARIO_VAZIO = {
  nome: "",
  registroProfissional: "",
  especialidade: "",
  telefone: "",
  email: "",
};

export default function FormularioProfissional({ profissional, profissionais, aoSalvar, aoCancelar }) {
  const [dados, setDados] = useState(() =>
    profissional
      ? {
          nome: profissional.nome ?? "",
          registroProfissional: profissional.registroProfissional ?? "",
          especialidade: profissional.especialidade ?? "",
          telefone: profissional.telefone ?? "",
          email: profissional.email ?? "",
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
    const especialidade = dados.especialidade.trim();
    const email = dados.email.trim().toLowerCase();
    const registro = formatarRegistro(dados.registroProfissional);
    const telefone = digitos(dados.telefone);
    const outros = profissionais.filter((item) => item.id !== profissional?.id);

    if (nome.length < 3) encontrados.nome = "Informe o nome completo do profissional.";
    if (!registroValido(dados.registroProfissional)) {
      encontrados.registroProfissional = "Informe o registro no formato CRM-UF 00000 ou COREN-UF 000000.";
    } else if (
      outros.some((item) => formatarRegistro(item.registroProfissional).toUpperCase() === registro.toUpperCase())
    ) {
      encontrados.registroProfissional = "Já existe um profissional com este registro.";
    }
    if (especialidade.length < 3) encontrados.especialidade = "Informe a especialidade.";
    if (telefone.length < 10) encontrados.telefone = "Informe um telefone com DDD.";
    if (!emailValido(email)) encontrados.email = "Informe um e-mail válido.";
    else if (outros.some((item) => String(item.email).trim().toLowerCase() === email)) {
      encontrados.email = "Já existe um profissional com este e-mail.";
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
      registroProfissional: formatarRegistro(dados.registroProfissional),
      especialidade: dados.especialidade.trim(),
      telefone: formatarTelefone(dados.telefone),
      email: dados.email.trim().toLowerCase(),
    });
  }

  const editando = Boolean(profissional);

  function fecharSeFundo(evento) {
    if (evento.target === evento.currentTarget) aoCancelar();
  }

  return (
    <div
      className="formulario-profissional-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="formulario-profissional-titulo"
      onClick={fecharSeFundo}
    >
      <form className="formulario-profissional" onSubmit={enviar} noValidate>
        <div className="formulario-profissional__cabecalho">
          <h2 id="formulario-profissional-titulo">
            {editando ? "Editar profissional" : "Cadastrar profissional"}
          </h2>
          <p>Preencha os dados de identificação e contato da equipe de saúde.</p>
        </div>
        <div className="formulario-profissional__grade">
          <Campo
            id="profissional-nome"
            rotulo="Nome"
            valor={dados.nome}
            erro={erros.nome}
            aoAlterar={(valor) => atualizar("nome", valor)}
          />
          <Campo
            id="profissional-registro"
            rotulo="CRM/COREN"
            valor={dados.registroProfissional}
            erro={erros.registroProfissional}
            aoAlterar={(valor) => atualizar("registroProfissional", valor)}
          />
          <Campo
            id="profissional-especialidade"
            rotulo="Especialidade"
            valor={dados.especialidade}
            erro={erros.especialidade}
            aoAlterar={(valor) => atualizar("especialidade", valor)}
          />
          <Campo
            id="profissional-telefone"
            rotulo="Telefone"
            valor={dados.telefone}
            erro={erros.telefone}
            modoEntrada="tel"
            aoAlterar={(valor) => atualizar("telefone", formatarTelefone(valor))}
          />
          <Campo
            id="profissional-email"
            rotulo="E-mail"
            tipo="email"
            valor={dados.email}
            erro={erros.email}
            largo
            aoAlterar={(valor) => atualizar("email", valor)}
          />
        </div>
        <div className="formulario-profissional__acoes">
          <button type="button" className="formulario-profissional__cancelar" onClick={aoCancelar}>
            Cancelar
          </button>
          <button type="submit" className="formulario-profissional__salvar">
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
    <label
      className={
        largo
          ? "formulario-profissional__campo formulario-profissional__campo--largo"
          : "formulario-profissional__campo"
      }
      htmlFor={id}
    >
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
        <small id={idErro} className="formulario-profissional__erro" role="alert">
          {erro}
        </small>
      ) : null}
    </label>
  );
}
