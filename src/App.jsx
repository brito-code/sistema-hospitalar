import { useState } from "react";
import BarraLateral from "./componentes/comum/BarraLateral";
import Cabecalho from "./componentes/comum/Cabecalho";
import { ITENS_NAVEGACAO } from "./componentes/comum/navegacao";
import PaginaConsultas from "./paginas/PaginaConsultas";
import PaginaPacientes from "./paginas/PaginaPacientes";
import PaginaPainel from "./paginas/PaginaPainel";
import PaginaProfissionais from "./paginas/PaginaProfissionais";
import PaginaQuartos from "./paginas/PaginaQuartos";

const PAGINAS = {
  painel: PaginaPainel,
  pacientes: PaginaPacientes,
  profissionais: PaginaProfissionais,
  consultas: PaginaConsultas,
  quartos: PaginaQuartos,
};

export default function App() {
  const [paginaAtiva, setPaginaAtiva] = useState("pacientes");
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
                <Pagina />
              </div>
            );
          })}
        </main>
      </div>
    </div>
  );
}
