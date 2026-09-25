import { useEffect, useState } from "react";
import BarraLateral from "./componentes/comum/BarraLateral";
import Cabecalho from "./componentes/comum/Cabecalho";
import { ITENS_NAVEGACAO } from "./componentes/comum/navegacao";
import PaginaConsultas from "./paginas/PaginaConsultas";
import PaginaPacientes from "./paginas/PaginaPacientes";
import PaginaPainel from "./paginas/PaginaPainel";
import PaginaProfissionais from "./paginas/PaginaProfissionais";
import PaginaQuartos from "./paginas/PaginaQuartos";
import { consultas as consultasIniciais } from "./servicos/dadosSimulados";

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

  useEffect(() => {
    const temporizador = setTimeout(() => {
      const semente = Array.isArray(consultasIniciais) ? consultasIniciais : [];
      setConsultas(semente.map((consulta) => ({ ...consulta, tipo: consulta.tipo || "Consulta" })));
    }, 280);
    return () => clearTimeout(temporizador);
  }, []);
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
                <Pagina consultas={consultas} aoDefinirConsultas={setConsultas} />
              </div>
            );
          })}
        </main>
      </div>
    </div>
  );
}
