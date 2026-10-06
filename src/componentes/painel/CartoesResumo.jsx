import "./CartoesResumo.css";

export default function CartoesResumo({ resumo }) {
  const cartoes = [
    { id: "pacientes", rotulo: "Total de pacientes", valor: String(resumo.totalPacientes), nota: "Cadastrados no hospital" },
    { id: "consultas", rotulo: "Consultas agendadas para hoje", valor: String(resumo.consultasHoje), nota: "Status agendada na data de hoje" },
    {
      id: "leitos",
      rotulo: "Leitos ocupados",
      valor: `${resumo.leitosOcupados}/${resumo.leitos}`,
      nota: `Taxa de ocupação ${resumo.taxaOcupacao}%`,
    },
    { id: "profissionais", rotulo: "Profissionais ativos", valor: String(resumo.profissionaisAtivos), nota: "Equipe assistencial cadastrada" },
  ];

  return (
    <section className="cartoes-resumo" aria-label="Resumo do hospital">
      {cartoes.map((cartao) => (
        <article key={cartao.id} className={`cartoes-resumo__cartao cartoes-resumo__cartao--${cartao.id}`}>
          <p>{cartao.rotulo}</p>
          <strong>{cartao.valor}</strong>
          <span>{cartao.nota}</span>
        </article>
      ))}
    </section>
  );
}
