import { useEffect, useMemo, useRef, useState } from "react";
import DetalhesConsulta from "../componentes/consultas/DetalhesConsulta";
import FormularioConsulta from "../componentes/consultas/FormularioConsulta";
import { nomePorId, SITUACOES_CONSULTA, TIPOS_ATENDIMENTO } from "../componentes/consultas/formatoConsulta";
import ListaConsultas from "../componentes/consultas/ListaConsultas";
import { pacientes as pacientesIniciais, profissionais as profissionaisIniciais } from "../servicos/dadosSimulados";
import "./PaginaConsultas.css";

export default function PaginaConsultas({ consultas = [], aoDefinirConsultas }) {
  const [pacientes, setPacientes] = useState([]);
  const [profissionais, setProfissionais] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [busca, setBusca] = useState("");
  const [filtroTipo, setFiltroTipo] = useState("");
  const [filtroSituacao, setFiltroSituacao] = useState("");
  const [filtroProfissional, setFiltroProfissional] = useState("");
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
      setPacientes(Array.isArray(pacientesIniciais) ? pacientesIniciais.map((paciente) => ({ ...paciente })) : []);
      setProfissionais(
        Array.isArray(profissionaisIniciais) ? profissionaisIniciais.map((profissional) => ({ ...profissional })) : [],
      );
      setCarregando(false);
    }, 280);
    return () => clearTimeout(temporizador);
  }, []);

  const filtradas = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    return consultas.filter((consulta) => {
      if (filtroTipo && consulta.tipo !== filtroTipo) return false;
      if (filtroSituacao && consulta.situacao !== filtroSituacao) return false;
      if (filtroProfissional && String(consulta.profissionalId) !== filtroProfissional) return false;
      if (!termo) return true;
      const texto = [
        nomePorId(pacientes, consulta.pacienteId),
        nomePorId(profissionais, consulta.profissionalId),
        consulta.tipo,
        consulta.especialidade,
        consulta.situacao,
        consulta.motivo,
        consulta.observacoesMedicas,
        consulta.data,
        consulta.horario,
      ]
        .join(" ")
        .toLowerCase();
      return texto.includes(termo);
    });
  }, [busca, consultas, filtroProfissional, filtroSituacao, filtroTipo, pacientes, profissionais]);

  function abrirAgendamento() {
    setSelecionado(null);
    setModo("criar");
    setPendenteExclusao(null);
  }

  function abrirEdicao(consulta) {
    setSelecionado(consulta);
    setModo("editar");
    setPendenteExclusao(null);
  }

  function abrirDetalhes(consulta) {
    setSelecionado(consulta);
    setModo("detalhar");
  }

  function fecharPainel() {
    setModo(null);
    setSelecionado(null);
  }

  function salvar(dados) {
    if (modo === "editar" && selecionado) {
      aoDefinirConsultas((lista) =>
        lista.map((consulta) => (consulta.id === selecionado.id ? { ...consulta, ...dados } : consulta)),
      );
      mostrarAviso(dados.tipo === "Internação" ? "Internação atualizada." : "Consulta atualizada.");
    } else {
      aoDefinirConsultas((lista) => {
        const proximoId = lista.reduce((maior, consulta) => Math.max(maior, Number(consulta.id) || 0), 0) + 1;
        return [...lista, { id: proximoId, ...dados }];
      });
      mostrarAviso(dados.tipo === "Internação" ? "Internação registrada." : "Consulta agendada.");
    }
    fecharPainel();
  }

  function confirmarExclusao() {
    if (!pendenteExclusao) return;
    const paciente = nomePorId(pacientes, pendenteExclusao.pacienteId) || "A consulta";
    aoDefinirConsultas((lista) => lista.filter((consulta) => consulta.id !== pendenteExclusao.id));
    mostrarAviso(`${paciente} saiu da agenda.`, "exclusao");
    if (selecionado?.id === pendenteExclusao.id) fecharPainel();
    setPendenteExclusao(null);
  }

  const filtrosAtivos = Boolean(busca.trim() || filtroTipo || filtroSituacao || filtroProfissional);
  const quantidade = filtrosAtivos ? filtradas.length : consultas.length;
  const rotuloQuantidade = quantidade === 1 ? "1 consulta" : `${quantidade} consultas`;

  return (
    <section className="pagina-consultas">
      <div className="pagina-consultas__barra">
        <label className="pagina-consultas__busca" htmlFor="busca-consulta">
          <span>Buscar consulta</span>
          <input
            id="busca-consulta"
            type="search"
            value={busca}
            placeholder="Paciente, profissional, especialidade ou motivo"
            onChange={(evento) => setBusca(evento.target.value)}
          />
        </label>
        <label className="pagina-consultas__filtro" htmlFor="filtro-tipo">
          <span>Tipo</span>
          <select id="filtro-tipo" value={filtroTipo} onChange={(evento) => setFiltroTipo(evento.target.value)}>
            <option value="">Todos</option>
            {TIPOS_ATENDIMENTO.map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo}
              </option>
            ))}
          </select>
        </label>
        <label className="pagina-consultas__filtro" htmlFor="filtro-situacao">
          <span>Status</span>
          <select id="filtro-situacao" value={filtroSituacao} onChange={(evento) => setFiltroSituacao(evento.target.value)}>
            <option value="">Todos</option>
            {SITUACOES_CONSULTA.map((situacao) => (
              <option key={situacao} value={situacao}>
                {situacao}
              </option>
            ))}
          </select>
        </label>
        <label className="pagina-consultas__filtro" htmlFor="filtro-profissional">
          <span>Profissional</span>
          <select
            id="filtro-profissional"
            value={filtroProfissional}
            onChange={(evento) => setFiltroProfissional(evento.target.value)}
          >
            <option value="">Todos</option>
            {profissionais.map((profissional) => (
              <option key={profissional.id} value={profissional.id}>
                {profissional.nome}
              </option>
            ))}
          </select>
        </label>
        <p className="pagina-consultas__contagem">{carregando ? "Carregando..." : rotuloQuantidade}</p>
        <button type="button" className="pagina-consultas__novo" onClick={abrirAgendamento}>
          Agendar atendimento
        </button>
      </div>

      {aviso ? (
        <p
          className={
            aviso.tipo === "exclusao"
              ? "pagina-consultas__aviso pagina-consultas__aviso--exclusao"
              : "pagina-consultas__aviso"
          }
          role="status"
        >
          {aviso.mensagem}
        </p>
      ) : null}

      {modo === "criar" || modo === "editar" ? (
        <FormularioConsulta
          key={selecionado?.id ?? "nova"}
          consulta={modo === "editar" ? selecionado : null}
          consultas={consultas}
          pacientes={pacientes}
          profissionais={profissionais}
          aoSalvar={salvar}
          aoCancelar={fecharPainel}
        />
      ) : null}

      <ListaConsultas
        consultas={filtradas}
        pacientes={pacientes}
        profissionais={profissionais}
        carregando={carregando}
        buscaAtiva={filtrosAtivos}
        aoAgendar={abrirAgendamento}
        aoEditar={abrirEdicao}
        aoDetalhar={abrirDetalhes}
        aoExcluir={setPendenteExclusao}
      />

      {modo === "detalhar" && selecionado ? (
        <DetalhesConsulta
          consulta={selecionado}
          pacientes={pacientes}
          profissionais={profissionais}
          aoFechar={fecharPainel}
          aoEditar={abrirEdicao}
        />
      ) : null}

      {pendenteExclusao ? (
        <div className="pagina-consultas__confirma" role="alertdialog" aria-labelledby="confirma-exclusao-consulta">
          <div className="pagina-consultas__confirma-caixa">
            <h2 id="confirma-exclusao-consulta">Excluir consulta</h2>
            <p>
              Confirma a exclusão da consulta de{" "}
              <strong>{nomePorId(pacientes, pendenteExclusao.pacienteId) || "paciente"}</strong>? O horário sai da
              agenda desta sessão.
            </p>
            <div className="pagina-consultas__confirma-acoes">
              <button type="button" onClick={() => setPendenteExclusao(null)}>
                Cancelar
              </button>
              <button type="button" className="pagina-consultas__excluir" onClick={confirmarExclusao}>
                Excluir
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  );
}
