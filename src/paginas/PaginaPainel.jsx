import { useMemo } from "react";
import AlertasPendentes from "../componentes/painel/AlertasPendentes";
import CartoesResumo from "../componentes/painel/CartoesResumo";
import ConsultasDoDia from "../componentes/painel/ConsultasDoDia";
import OcupacaoQuartos from "../componentes/painel/OcupacaoQuartos";
import { dataDeHoje, montarPainel } from "../componentes/painel/resumoPainel";
import "./PaginaPainel.css";

export default function PaginaPainel({
  pacientes = [],
  profissionais = [],
  consultas = [],
  quartos = [],
  internacoes = [],
  dadosProntos = false,
}) {
  const hoje = dataDeHoje();
  const resumo = useMemo(
    () => montarPainel({ pacientes, profissionais, consultas, quartos, internacoes, hoje }),
    [consultas, hoje, internacoes, pacientes, profissionais, quartos],
  );

  if (!dadosProntos) {
    return (
      <section className="pagina-painel" aria-busy="true">
        <p className="pagina-painel__carregando">Carregando o painel...</p>
      </section>
    );
  }

  return (
    <section className="pagina-painel">
      <CartoesResumo resumo={resumo} />
      <div className="pagina-painel__grade">
        <ConsultasDoDia consultas={resumo.consultasDeHoje} pacientes={pacientes} profissionais={profissionais} hoje={hoje} />
        <OcupacaoQuartos
          ocupacao={resumo.ocupacao}
          leitosOcupados={resumo.leitosOcupados}
          leitos={resumo.leitos}
          taxaOcupacao={resumo.taxaOcupacao}
        />
      </div>
      <AlertasPendentes alertas={resumo.alertas} />
    </section>
  );
}
