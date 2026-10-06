import { useEffect, useState } from "react";
import BarraLateral from "./componentes/comum/BarraLateral";
import Cabecalho from "./componentes/comum/Cabecalho";
import { ITENS_NAVEGACAO } from "./componentes/comum/navegacao";
import PaginaConsultas from "./paginas/PaginaConsultas";
import PaginaPacientes from "./paginas/PaginaPacientes";
import PaginaPainel from "./paginas/PaginaPainel";
import PaginaProfissionais from "./paginas/PaginaProfissionais";
import PaginaQuartos from "./paginas/PaginaQuartos";
import { carregarRegistros, salvarRegistros } from "./servicos/api";

const PAGINAS = {
  painel: PaginaPainel,
  pacientes: PaginaPacientes,
  profissionais: PaginaProfissionais,
  consultas: PaginaConsultas,
  quartos: PaginaQuartos,
};

export default function App() {
  const [paginaAtiva, setPaginaAtiva] = useState("pacientes");
  const [consultas, setConsultas] = useState([]);
  const [quartos, setQuartos] = useState([]);
  const [internacoes, setInternacoes] = useState([]);
  const [pacientes, setPacientes] = useState([]);
  const [profissionais, setProfissionais] = useState([]);
  const [dadosProntos, setDadosProntos] = useState(false);

  useEffect(() => {
    const temporizador = setTimeout(() => {
      const registros = carregarRegistros();
      setConsultas(registros.consultas);
      setQuartos(registros.quartos);
      setInternacoes(registros.internacoes);
      setPacientes(registros.pacientes);
      setProfissionais(registros.profissionais);
      setDadosProntos(true);
    }, 280);
    return () => clearTimeout(temporizador);
  }, []);

  useEffect(() => {
    if (!dadosProntos) return;
    salvarRegistros({ pacientes, profissionais, consultas, quartos, internacoes });
  }, [consultas, dadosProntos, internacoes, pacientes, profissionais, quartos]);
  const itemAtual =
    ITENS_NAVEGACAO.find((item) => item.id === paginaAtiva) ?? ITENS_NAVEGACAO[0];

  return (
    <div className="aplicacao">
      <BarraLateral itens={ITENS_NAVEGACAO} paginaAtiva={itemAtual.id} aoNavegar={setPaginaAtiva} />
      <div className="aplicacao__principal">
        <Cabecalho titulo={itemAtual.titulo} resumo={itemAtual.resumo} />
        <main className="aplicacao__conteudo">
          {ITENS_NAVEGACAO.map((item) => {
            const Pagina = PAGINAS[item.id];
            return (
              <div key={item.id} hidden={item.id !== itemAtual.id}>
                <Pagina
                  consultas={consultas}
                  aoDefinirConsultas={setConsultas}
                  quartos={quartos}
                  aoDefinirQuartos={setQuartos}
                  internacoes={internacoes}
                  aoDefinirInternacoes={setInternacoes}
                  pacientes={pacientes}
                  aoDefinirPacientes={setPacientes}
                  profissionais={profissionais}
                  aoDefinirProfissionais={setProfissionais}
                  dadosProntos={dadosProntos}
                />
              </div>
            );
          })}
        </main>
      </div>
    </div>
  );
}
