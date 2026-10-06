import { useEffect, useState } from "react";
import { ocupacaoAtiva, SITUACOES_QUARTO, situacaoPorOcupacao } from "./formatoQuarto";
import "./painelLeitos.css";

const FORMULARIO_VAZIO = {
  numeroIdentificacao: "",
  andar: "",
  capacidadeMaxima: "",
  situacao: "DISPONIVEL",
};

export default function FormularioQuarto({ quarto, quartos, internacoes, aoSalvar, aoCancelar }) {
  const ocupacao = quarto ? ocupacaoAtiva(internacoes, quarto.id) : 0;
  const [dados, setDados] = useState(() =>
    quarto
      ? {
          numeroIdentificacao: quarto.numeroIdentificacao ?? "",
          andar: String(quarto.andar ?? ""),
          capacidadeMaxima: String(quarto.capacidadeMaxima ?? ""),
          situacao: quarto.situacao ?? "DISPONIVEL",
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
      if (campo === "capacidadeMaxima") {
        const capacidade = Number(valor);
        if (Number.isInteger(capacidade) && capacidade >= 1) {
          proximo.situacao = situacaoPorOcupacao(capacidade, ocupacao);
        }
      }
      return proximo;
    });
    setErros((atual) => ({ ...atual, [campo]: undefined }));
  }

  function validar() {
    const encontrados = {};
    const numero = dados.numeroIdentificacao.trim();
    const andar = Number(dados.andar);
    const capacidade = Number(dados.capacidadeMaxima);
    const outros = quartos.filter((item) => item.id !== quarto?.id);

    if (!numero) encontrados.numeroIdentificacao = "Informe o número do quarto.";
    else if (outros.some((item) => String(item.numeroIdentificacao).trim() === numero)) {
      encontrados.numeroIdentificacao = "Já existe um quarto com este número.";
    }
    if (dados.andar === "" || !Number.isInteger(andar) || andar < 0) encontrados.andar = "Informe o andar.";
    if (!Number.isInteger(capacidade) || capacidade < 1) {
      encontrados.capacidadeMaxima = "Informe a capacidade com pelo menos 1 leito.";
    } else if (capacidade < ocupacao) {
      encontrados.capacidadeMaxima = `A capacidade não pode ser menor que a ocupação atual (${ocupacao}).`;
    }
    if (!SITUACOES_QUARTO.includes(dados.situacao)) {
      encontrados.situacao = "Selecione o status do quarto.";
    } else if (Number.isInteger(capacidade) && capacidade >= 1 && dados.situacao !== situacaoPorOcupacao(capacidade, ocupacao)) {
      encontrados.situacao =
        ocupacao >= capacidade
          ? "O quarto fica ocupado quando a ocupação atinge a capacidade."
          : "O quarto fica disponível enquanto houver leito livre.";
    }
    return encontrados;
  }

  function enviar(evento) {
    evento.preventDefault();
    const encontrados = validar();
    setErros(encontrados);
    if (Object.keys(encontrados).length > 0) return;
    aoSalvar({
      numeroIdentificacao: dados.numeroIdentificacao.trim(),
      andar: Number(dados.andar),
      capacidadeMaxima: Number(dados.capacidadeMaxima),
      situacao: dados.situacao,
    });
  }

  const editando = Boolean(quarto);

  function fecharSeFundo(evento) {
    if (evento.target === evento.currentTarget) aoCancelar();
  }

  return (
    <div
      className="formulario-quarto-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="formulario-quarto-titulo"
      onClick={fecharSeFundo}
    >
      <form className="formulario-quarto" onSubmit={enviar} noValidate>
        <div className="formulario-quarto__cabecalho">
          <h2 id="formulario-quarto-titulo">{editando ? "Editar quarto" : "Novo quarto"}</h2>
          <p>O status acompanha a ocupação: o quarto fica ocupado quando os leitos ativos atingem a capacidade.</p>
        </div>
        <div className="formulario-quarto__grade">
          <Campo
            id="quarto-numero"
            rotulo="Número"
            valor={dados.numeroIdentificacao}
            erro={erros.numeroIdentificacao}
            aoAlterar={(valor) => atualizar("numeroIdentificacao", valor)}
          />
          <Campo
            id="quarto-andar"
            rotulo="Andar"
            tipo="number"
            valor={dados.andar}
            erro={erros.andar}
            aoAlterar={(valor) => atualizar("andar", valor)}
          />
          <Campo
            id="quarto-capacidade"
            rotulo="Capacidade"
            tipo="number"
            valor={dados.capacidadeMaxima}
            erro={erros.capacidadeMaxima}
            aoAlterar={(valor) => atualizar("capacidadeMaxima", valor)}
          />
          <label className="formulario-quarto__campo" htmlFor="quarto-situacao">
            <span>Status</span>
            <select
              id="quarto-situacao"
              value={dados.situacao}
              aria-invalid={erros.situacao ? "true" : "false"}
              onChange={(evento) => atualizar("situacao", evento.target.value)}
            >
              {SITUACOES_QUARTO.map((situacao) => (
                <option key={situacao} value={situacao}>
                  {situacao === "OCUPADO" ? "Ocupado" : "Disponível"}
                </option>
              ))}
            </select>
            {erros.situacao ? <small className="formulario-quarto__erro">{erros.situacao}</small> : null}
          </label>
        </div>
        <div className="formulario-quarto__acoes">
          <button type="button" className="formulario-quarto__cancelar" onClick={aoCancelar}>
            Cancelar
          </button>
          <button type="submit" className="formulario-quarto__salvar">
            {editando ? "Salvar alterações" : "Cadastrar"}
          </button>
        </div>
      </form>
    </div>
  );
}

function Campo({ id, rotulo, tipo = "text", valor, erro, aoAlterar }) {
  const idErro = `${id}-erro`;
  return (
    <label className="formulario-quarto__campo" htmlFor={id}>
      <span>{rotulo}</span>
      <input
        id={id}
        type={tipo}
        min={tipo === "number" ? "0" : undefined}
        value={valor}
        aria-invalid={erro ? "true" : "false"}
        aria-describedby={erro ? idErro : undefined}
        onChange={(evento) => aoAlterar(evento.target.value)}
      />
      {erro ? (
        <small id={idErro} className="formulario-quarto__erro" role="alert">
          {erro}
        </small>
      ) : null}
    </label>
  );
}
