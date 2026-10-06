import { useEffect, useMemo, useRef, useState } from "react";
import DetalhesInternacao from "../componentes/quartos/DetalhesInternacao";
import DetalhesQuarto from "../componentes/quartos/DetalhesQuarto";
import FormularioInternacao from "../componentes/quartos/FormularioInternacao";
import FormularioQuarto from "../componentes/quartos/FormularioQuarto";
import { nomePorId, rotuloSituacao, sincronizarSituacao } from "../componentes/quartos/formatoQuarto";
import ListaInternacoes from "../componentes/quartos/ListaInternacoes";
import ListaQuartos from "../componentes/quartos/ListaQuartos";
import "./PaginaQuartos.css";

export default function PaginaQuartos({
  quartos = [],
  aoDefinirQuartos,
  internacoes = [],
  aoDefinirInternacoes,
  pacientes = [],
  profissionais = [],
}) {
  const [carregando, setCarregando] = useState(true);
  const [buscaQuarto, setBuscaQuarto] = useState("");
  const [filtroSituacao, setFiltroSituacao] = useState("");
  const [buscaInternacao, setBuscaInternacao] = useState("");
  const [somenteAtivas, setSomenteAtivas] = useState(false);
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
      setCarregando(false);
    }, 280);
    return () => clearTimeout(temporizador);
  }, []);

  const quartosFiltrados = useMemo(() => {
    const termo = buscaQuarto.trim().toLowerCase();
    return quartos.filter((quarto) => {
      if (filtroSituacao && quarto.situacao !== filtroSituacao) return false;
      if (!termo) return true;
      const texto = [quarto.numeroIdentificacao, quarto.andar, rotuloSituacao(quarto.situacao), quarto.situacao]
        .join(" ")
        .toLowerCase();
      return texto.includes(termo);
    });
  }, [buscaQuarto, filtroSituacao, quartos]);

  const internacoesFiltradas = useMemo(() => {
    const termo = buscaInternacao.trim().toLowerCase();
    return internacoes.filter((internacao) => {
      if (somenteAtivas && internacao.dataEfetivaAlta) return false;
      if (!termo) return true;
      const quarto = quartos.find((item) => String(item.id) === String(internacao.quartoId));
      const texto = [
        nomePorId(pacientes, internacao.pacienteId),
        nomePorId(profissionais, internacao.profissionalId),
        quarto?.numeroIdentificacao,
        internacao.dataEntrada,
        internacao.dataPrevistaAlta,
        internacao.dataEfetivaAlta,
        internacao.observacoes,
      ]
        .join(" ")
        .toLowerCase();
      return texto.includes(termo);
    });
  }, [buscaInternacao, internacoes, pacientes, profissionais, quartos, somenteAtivas]);

  function fecharPainel() {
    setModo(null);
    setSelecionado(null);
  }

  function abrir(proximoModo, registro = null) {
    setSelecionado(registro);
    setModo(proximoModo);
    setPendenteExclusao(null);
  }

  function salvarQuarto(dados) {
    if (modo === "editar-quarto" && selecionado) {
      aoDefinirQuartos((lista) =>
        sincronizarSituacao(
          lista.map((quarto) => (quarto.id === selecionado.id ? { ...quarto, ...dados } : quarto)),
          internacoes,
        ),
      );
      mostrarAviso("Quarto atualizado.");
    } else {
      aoDefinirQuartos((lista) => {
        const proximoId = lista.reduce((maior, quarto) => Math.max(maior, Number(quarto.id) || 0), 0) + 1;
        return sincronizarSituacao([...lista, { id: proximoId, ...dados }], internacoes);
      });
      mostrarAviso("Quarto cadastrado.");
    }
    fecharPainel();
  }

  function salvarInternacao(dados) {
    const aplicar = (lista) => {
      aoDefinirInternacoes(lista);
      aoDefinirQuartos((quartosAtuais) => sincronizarSituacao(quartosAtuais, lista));
    };
    if (modo === "editar-internacao" && selecionado) {
      aplicar(internacoes.map((internacao) => (internacao.id === selecionado.id ? { ...internacao, ...dados } : internacao)));
      mostrarAviso("Internação atualizada.");
    } else {
      const proximoId = internacoes.reduce((maior, internacao) => Math.max(maior, Number(internacao.id) || 0), 0) + 1;
      aplicar([...internacoes, { id: proximoId, ...dados }]);
      mostrarAviso("Internação registrada.");
    }
    fecharPainel();
  }

  function pedirExclusaoQuarto(quarto) {
    const vinculadas = internacoes.some((internacao) => String(internacao.quartoId) === String(quarto.id));
    if (vinculadas) {
      mostrarAviso("Este quarto possui internações e não pode ser excluído.", "exclusao");
      return;
    }
    setPendenteExclusao({ entidade: "quarto", registro: quarto });
  }

  function confirmarExclusao() {
    if (!pendenteExclusao) return;
    if (pendenteExclusao.entidade === "quarto") {
      const quarto = pendenteExclusao.registro;
      aoDefinirQuartos((lista) => lista.filter((item) => item.id !== quarto.id));
      mostrarAviso(`Quarto ${quarto.numeroIdentificacao} saiu da lista.`, "exclusao");
    } else {
      const internacao = pendenteExclusao.registro;
      const restantes = internacoes.filter((item) => item.id !== internacao.id);
      aoDefinirInternacoes(restantes);
      aoDefinirQuartos((lista) => sincronizarSituacao(lista, restantes));
      const paciente = nomePorId(pacientes, internacao.pacienteId) || "A internação";
      mostrarAviso(`${paciente} saiu das internações.`, "exclusao");
    }
    if (selecionado?.id === pendenteExclusao.registro.id) fecharPainel();
    setPendenteExclusao(null);
  }

  const filtrosQuarto = Boolean(buscaQuarto.trim() || filtroSituacao);
  const filtrosInternacao = Boolean(buscaInternacao.trim() || somenteAtivas);
  const quantidadeQuartos = filtrosQuarto ? quartosFiltrados.length : quartos.length;
  const quantidadeInternacoes = filtrosInternacao ? internacoesFiltradas.length : internacoes.length;

  return (
    <section className="pagina-quartos">
      <div className="pagina-quartos__barra">
        <label className="pagina-quartos__busca" htmlFor="busca-quarto">
          <span>Buscar quarto</span>
          <input
            id="busca-quarto"
            type="search"
            value={buscaQuarto}
            placeholder="Número, andar ou status"
            onChange={(evento) => setBuscaQuarto(evento.target.value)}
          />
        </label>
        <label className="pagina-quartos__filtro" htmlFor="filtro-quarto">
          <span>Status</span>
          <select id="filtro-quarto" value={filtroSituacao} onChange={(evento) => setFiltroSituacao(evento.target.value)}>
            <option value="">Todos</option>
            <option value="DISPONIVEL">Disponível</option>
            <option value="OCUPADO">Ocupado</option>
          </select>
        </label>
        <p className="pagina-quartos__contagem">
          {carregando ? "Carregando..." : quantidadeQuartos === 1 ? "1 quarto" : `${quantidadeQuartos} quartos`}
        </p>
        <button type="button" className="pagina-quartos__novo" onClick={() => abrir("criar-quarto")}>
          Novo quarto
        </button>
      </div>

      {aviso ? (
        <p
          className={
            aviso.tipo === "exclusao" ? "pagina-quartos__aviso pagina-quartos__aviso--exclusao" : "pagina-quartos__aviso"
          }
          role="status"
        >
          {aviso.mensagem}
        </p>
      ) : null}

      <ListaQuartos
        quartos={quartosFiltrados}
        internacoes={internacoes}
        carregando={carregando}
        buscaAtiva={filtrosQuarto}
        aoCadastrar={() => abrir("criar-quarto")}
        aoEditar={(quarto) => abrir("editar-quarto", quarto)}
        aoDetalhar={(quarto) => abrir("detalhar-quarto", quarto)}
        aoExcluir={pedirExclusaoQuarto}
      />

      <h2 className="pagina-quartos__secao">Internações</h2>
      <div className="pagina-quartos__barra">
        <label className="pagina-quartos__busca" htmlFor="busca-internacao">
          <span>Buscar internação</span>
          <input
            id="busca-internacao"
            type="search"
            value={buscaInternacao}
            placeholder="Paciente, profissional ou quarto"
            onChange={(evento) => setBuscaInternacao(evento.target.value)}
          />
        </label>
        <label className="pagina-quartos__filtro pagina-quartos__marca" htmlFor="filtro-ativas">
          <input
            id="filtro-ativas"
            type="checkbox"
            checked={somenteAtivas}
            onChange={(evento) => setSomenteAtivas(evento.target.checked)}
          />
          <span>Somente em aberto</span>
        </label>
        <p className="pagina-quartos__contagem">
          {carregando
            ? "Carregando..."
            : quantidadeInternacoes === 1
              ? "1 internação"
              : `${quantidadeInternacoes} internações`}
        </p>
        <button type="button" className="pagina-quartos__novo" onClick={() => abrir("criar-internacao")}>
          Registrar internação
        </button>
      </div>

      <ListaInternacoes
        internacoes={internacoesFiltradas}
        pacientes={pacientes}
        profissionais={profissionais}
        quartos={quartos}
        carregando={carregando}
        buscaAtiva={filtrosInternacao}
        aoRegistrar={() => abrir("criar-internacao")}
        aoEditar={(internacao) => abrir("editar-internacao", internacao)}
        aoDetalhar={(internacao) => abrir("detalhar-internacao", internacao)}
        aoExcluir={(internacao) => setPendenteExclusao({ entidade: "internacao", registro: internacao })}
      />

      {modo === "criar-quarto" || modo === "editar-quarto" ? (
        <FormularioQuarto
          key={selecionado?.id ?? "novo-quarto"}
          quarto={modo === "editar-quarto" ? selecionado : null}
          quartos={quartos}
          internacoes={internacoes}
          aoSalvar={salvarQuarto}
          aoCancelar={fecharPainel}
        />
      ) : null}

      {modo === "detalhar-quarto" && selecionado ? (
        <DetalhesQuarto quarto={selecionado} internacoes={internacoes} aoFechar={fecharPainel} aoEditar={(quarto) => abrir("editar-quarto", quarto)} />
      ) : null}

      {modo === "criar-internacao" || modo === "editar-internacao" ? (
        <FormularioInternacao
          key={selecionado?.id ?? "nova-internacao"}
          internacao={modo === "editar-internacao" ? selecionado : null}
          internacoes={internacoes}
          pacientes={pacientes}
          profissionais={profissionais}
          quartos={quartos}
          aoSalvar={salvarInternacao}
          aoCancelar={fecharPainel}
        />
      ) : null}

      {modo === "detalhar-internacao" && selecionado ? (
        <DetalhesInternacao
          internacao={selecionado}
          pacientes={pacientes}
          profissionais={profissionais}
          quartos={quartos}
          aoFechar={fecharPainel}
          aoEditar={(internacao) => abrir("editar-internacao", internacao)}
        />
      ) : null}

      {pendenteExclusao ? (
        <div className="pagina-quartos__confirma" role="alertdialog" aria-labelledby="confirma-exclusao-leito">
          <div className="pagina-quartos__confirma-caixa">
            <h2 id="confirma-exclusao-leito">
              {pendenteExclusao.entidade === "quarto" ? "Excluir quarto" : "Excluir internação"}
            </h2>
            <p>
              {pendenteExclusao.entidade === "quarto"
                ? `Confirma a exclusão do quarto ${pendenteExclusao.registro.numeroIdentificacao}?`
                : `Confirma a exclusão da internação de ${nomePorId(pacientes, pendenteExclusao.registro.pacienteId) || "paciente"}?`}
            </p>
            <div className="pagina-quartos__confirma-acoes">
              <button type="button" onClick={() => setPendenteExclusao(null)}>
                Cancelar
              </button>
              <button type="button" className="pagina-quartos__excluir" onClick={confirmarExclusao}>
                Excluir
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  );
}
