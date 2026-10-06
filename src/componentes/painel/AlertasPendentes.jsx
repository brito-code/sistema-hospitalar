import "./ConsultasDoDia.css";
import "./OcupacaoQuartos.css";

export default function AlertasPendentes({ alertas }) {
  const atrasos = alertas.filter((alerta) => alerta.gravidade === "atraso").length;

  return (
    <section className="alertas-pendentes" aria-label="Atendimentos e internações pendentes">
      <header>
        <h2>Atendimentos e internações pendentes</h2>
        <p>{atrasos === 1 ? "1 em atraso" : `${atrasos} em atraso`}</p>
      </header>
      {alertas.length === 0 ? (
        <p className="alertas-pendentes__vazio">Nenhum atendimento ou internação pendente.</p>
      ) : (
        <ul>
          {alertas.map((alerta) => (
            <li key={alerta.id} className={`alertas-pendentes__item alertas-pendentes__item--${alerta.gravidade}`}>
              <strong>{alerta.titulo}</strong>
              <p>{alerta.detalhe}</p>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
