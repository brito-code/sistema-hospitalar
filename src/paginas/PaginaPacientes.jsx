import { useEffect, useMemo, useRef, useState } from "react";
import DetalhesPaciente from "../componentes/pacientes/DetalhesPaciente";
import FormularioPaciente from "../componentes/pacientes/FormularioPaciente";
import { digitos } from "../componentes/pacientes/formatoPaciente";
import ListaPacientes from "../componentes/pacientes/ListaPacientes";
import { internacoes, pacientes as pacientesIniciais, profissionais, quartos } from "../servicos/dadosSimulados";
import "./PaginaPacientes.css";

export default function PaginaPacientes({ consultas = [] }) {
  const [pacientes, setPacientes] = useState([]);
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
      const semente = Array.isArray(pacientesIniciais) ? pacientesIniciais : [];
      setPacientes(semente.map((paciente) => ({ ...paciente })));
      setCarregando(false);
    }, 280);
    return () => clearTimeout(temporizador);
  }, []);

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    if (!termo) return pacientes;
    const termoDigitos = digitos(termo);
    return pacientes.filter((paciente) => {
      const texto = [paciente.nome, paciente.cpf, paciente.email, paciente.telefone, paciente.endereco]
        .join(" ")
        .toLowerCase();
      if (texto.includes(termo)) return true;
      if (!termoDigitos) return false;
      return (
        digitos(paciente.cpf).includes(termoDigitos) ||
        digitos(paciente.telefone).includes(termoDigitos)
      );
    });
  }, [busca, pacientes]);

  function abrirCadastro() {
    setSelecionado(null);
    setModo("criar");
    setPendenteExclusao(null);
  }

  function abrirEdicao(paciente) {
    setSelecionado(paciente);
    setModo("editar");
    setPendenteExclusao(null);
  }

  function abrirDetalhes(paciente) {
    setSelecionado(paciente);
    setModo("detalhar");
  }

  function fecharPainel() {
    setModo(null);
    setSelecionado(null);
  }

  function salvar(dados) {
    if (modo === "editar" && selecionado) {
      setPacientes((lista) =>
        lista.map((paciente) =>
          paciente.id === selecionado.id ? { ...paciente, ...dados } : paciente,
        ),
      );
      mostrarAviso("Paciente atualizado.");
    } else {
      setPacientes((lista) => {
        const proximoId = lista.reduce((maior, paciente) => Math.max(maior, Number(paciente.id) || 0), 0) + 1;
        return [...lista, { id: proximoId, ...dados }];
      });
      mostrarAviso("Paciente cadastrado.");
    }
    fecharPainel();
  }

  function confirmarExclusao() {
    if (!pendenteExclusao) return;
    setPacientes((lista) => lista.filter((paciente) => paciente.id !== pendenteExclusao.id));
    mostrarAviso(`${pendenteExclusao.nome} saiu da lista.`, "exclusao");
    if (selecionado?.id === pendenteExclusao.id) fecharPainel();
    setPendenteExclusao(null);
  }

  return (
    <section className="pagina-pacientes">
      <div className="pagina-pacientes__barra">
        <label className="pagina-pacientes__busca" htmlFor="busca-paciente">
          <span>Buscar paciente</span>
          <input
            id="busca-paciente"
            type="search"
            value={busca}
            placeholder="Nome, CPF, telefone ou e-mail"
            onChange={(evento) => setBusca(evento.target.value)}
          />
        </label>
        <p className="pagina-pacientes__contagem">
          {carregando ? "Carregando..." : `${pacientes.length} paciente(s)`}
        </p>
        <button type="button" className="pagina-pacientes__novo" onClick={abrirCadastro}>
          Novo paciente
        </button>
      </div>

      {aviso ? (
        <p
          className={
            aviso.tipo === "exclusao"
              ? "pagina-pacientes__aviso pagina-pacientes__aviso--exclusao"
              : "pagina-pacientes__aviso"
          }
          role="status"
        >
          {aviso.mensagem}
        </p>
      ) : null}

      {modo === "criar" || modo === "editar" ? (
        <FormularioPaciente
          key={selecionado?.id ?? "novo"}
          paciente={modo === "editar" ? selecionado : null}
          pacientes={pacientes}
          aoSalvar={salvar}
          aoCancelar={fecharPainel}
        />
      ) : null}

      <ListaPacientes
        pacientes={filtrados}
        carregando={carregando}
        buscaAtiva={busca.trim().length > 0}
        aoCadastrar={abrirCadastro}
        aoEditar={abrirEdicao}
        aoDetalhar={abrirDetalhes}
        aoExcluir={setPendenteExclusao}
      />

      {modo === "detalhar" && selecionado ? (
        <DetalhesPaciente
          paciente={selecionado}
          consultas={consultas}
          internacoes={internacoes}
          profissionais={profissionais}
          quartos={quartos}
          aoFechar={fecharPainel}
          aoEditar={abrirEdicao}
        />
      ) : null}

      {pendenteExclusao ? (
        <div className="pagina-pacientes__confirma" role="alertdialog" aria-labelledby="confirma-exclusao">
          <div className="pagina-pacientes__confirma-caixa">
            <h2 id="confirma-exclusao">Excluir paciente</h2>
            <p>
              Confirma a exclusão de <strong>{pendenteExclusao.nome}</strong>? O registro sai da
              lista desta sessão.
            </p>
            <div className="pagina-pacientes__confirma-acoes">
              <button type="button" onClick={() => setPendenteExclusao(null)}>
                Cancelar
              </button>
              <button type="button" className="pagina-pacientes__excluir" onClick={confirmarExclusao}>
                Excluir
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  );
}
