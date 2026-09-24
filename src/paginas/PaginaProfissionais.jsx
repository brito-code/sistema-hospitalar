import { useEffect, useMemo, useRef, useState } from "react";
import DetalhesProfissional from "../componentes/profissionais/DetalhesProfissional";
import FormularioProfissional from "../componentes/profissionais/FormularioProfissional";
import { digitos } from "../componentes/profissionais/formatoProfissional";
import ListaProfissionais from "../componentes/profissionais/ListaProfissionais";
import { profissionais as profissionaisIniciais } from "../servicos/dadosSimulados";
import "./PaginaProfissionais.css";

export default function PaginaProfissionais() {
  const [profissionais, setProfissionais] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [busca, setBusca] = useState("");
  const [modo, setModo] = useState(null);
  const [selecionado, setSelecionado] = useState(null);
  const [pendenteExclusao, setPendenteExclusao] = useState(null);
  const [aviso, setAviso] = useState(null);
  const temporizadorAviso = useRef(null);

  useEffect(() => () => clearTimeout(temporizadorAviso.current), []);

  function mostrarAviso(mensagem, tipo = "sucesso") {
    setAviso({ mensagem, tipo });
    clearTimeout(temporizadorAviso.current);
    temporizadorAviso.current = setTimeout(() => setAviso(null), 3500);
  }

  useEffect(() => {
    const temporizador = setTimeout(() => {
      const semente = Array.isArray(profissionaisIniciais) ? profissionaisIniciais : [];
      setProfissionais(semente.map((profissional) => ({ ...profissional })));
      setCarregando(false);
    }, 280);
    return () => clearTimeout(temporizador);
  }, []);

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    if (!termo) return profissionais;
    const termoDigitos = digitos(termo);
    const termoTelefone = termo.replace(/[()\s-]/g, "");
    return profissionais.filter((profissional) => {
      const texto = [profissional.nome, profissional.registroProfissional, profissional.especialidade, profissional.email]
        .join(" ")
        .toLowerCase();
      if (texto.includes(termo)) return true;
      const telefone = String(profissional.telefone ?? "").toLowerCase().replace(/[()\s-]/g, "");
      if (termoTelefone && telefone.includes(termoTelefone)) return true;
      if (!termoDigitos) return false;
      return digitos(profissional.registroProfissional).includes(termoDigitos);
    });
  }, [busca, profissionais]);

  function abrirCadastro() {
    setSelecionado(null);
    setModo("criar");
    setPendenteExclusao(null);
  }

  function abrirEdicao(profissional) {
    setSelecionado(profissional);
    setModo("editar");
    setPendenteExclusao(null);
  }

  function abrirDetalhes(profissional) {
    setSelecionado(profissional);
    setModo("detalhar");
  }

  function fecharPainel() {
    setModo(null);
    setSelecionado(null);
  }

  function salvar(dados) {
    if (modo === "editar" && selecionado) {
      setProfissionais((lista) =>
        lista.map((profissional) =>
          profissional.id === selecionado.id ? { ...profissional, ...dados } : profissional,
        ),
      );
      mostrarAviso("Profissional atualizado.");
    } else {
      setProfissionais((lista) => {
        const proximoId =
          lista.reduce((maior, profissional) => Math.max(maior, Number(profissional.id) || 0), 0) + 1;
        return [...lista, { id: proximoId, ...dados }];
      });
      mostrarAviso("Profissional cadastrado.");
    }
    fecharPainel();
  }

  function confirmarExclusao() {
    if (!pendenteExclusao) return;
    setProfissionais((lista) => lista.filter((profissional) => profissional.id !== pendenteExclusao.id));
    mostrarAviso(`${pendenteExclusao.nome} saiu da lista.`, "exclusao");
    if (selecionado?.id === pendenteExclusao.id) fecharPainel();
    setPendenteExclusao(null);
  }

  const quantidade = busca.trim() ? filtrados.length : profissionais.length;
  const rotuloQuantidade =
    quantidade === 1 ? "1 profissional" : `${quantidade} profissionais`;

  return (
    <section className="pagina-profissionais">
      <div className="pagina-profissionais__barra">
        <label className="pagina-profissionais__busca" htmlFor="busca-profissional">
          <span>Buscar profissional</span>
          <input
            id="busca-profissional"
            type="search"
            value={busca}
            placeholder="Nome, CRM/COREN, especialidade, telefone ou e-mail"
            onChange={(evento) => setBusca(evento.target.value)}
          />
        </label>
        <p className="pagina-profissionais__contagem">
          {carregando ? "Carregando..." : rotuloQuantidade}
        </p>
        <button type="button" className="pagina-profissionais__novo" onClick={abrirCadastro}>
          Novo profissional
        </button>
      </div>

      {aviso ? (
        <p
          className={
            aviso.tipo === "exclusao"
              ? "pagina-profissionais__aviso pagina-profissionais__aviso--exclusao"
              : "pagina-profissionais__aviso"
          }
          role="status"
        >
          {aviso.mensagem}
        </p>
      ) : null}

      {modo === "criar" || modo === "editar" ? (
        <FormularioProfissional
          key={selecionado?.id ?? "novo"}
          profissional={modo === "editar" ? selecionado : null}
          profissionais={profissionais}
          aoSalvar={salvar}
          aoCancelar={fecharPainel}
        />
      ) : null}

      <ListaProfissionais
        profissionais={filtrados}
        carregando={carregando}
        buscaAtiva={busca.trim().length > 0}
        aoCadastrar={abrirCadastro}
        aoEditar={abrirEdicao}
        aoDetalhar={abrirDetalhes}
        aoExcluir={setPendenteExclusao}
      />

      {modo === "detalhar" && selecionado ? (
        <DetalhesProfissional
          profissional={selecionado}
          aoFechar={fecharPainel}
          aoEditar={abrirEdicao}
        />
      ) : null}

      {pendenteExclusao ? (
        <div
          className="pagina-profissionais__confirma"
          role="alertdialog"
          aria-labelledby="confirma-exclusao-profissional"
        >
          <div className="pagina-profissionais__confirma-caixa">
            <h2 id="confirma-exclusao-profissional">Excluir profissional</h2>
            <p>
              Confirma a exclusão de <strong>{pendenteExclusao.nome}</strong>? O registro sai da
              lista desta sessão.
            </p>
            <div className="pagina-profissionais__confirma-acoes">
              <button type="button" onClick={() => setPendenteExclusao(null)}>
                Cancelar
              </button>
              <button type="button" className="pagina-profissionais__excluir" onClick={confirmarExclusao}>
                Excluir
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  );
}
