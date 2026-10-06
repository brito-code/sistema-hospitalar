import { rotuloSituacao } from "../quartos/formatoQuarto";
import "./ConsultasDoDia.css";
import "./OcupacaoQuartos.css";

export default function OcupacaoQuartos({ ocupacao, leitosOcupados, leitos, taxaOcupacao }) {
  return (
    <section className="ocupacao-quartos" aria-label="Ocupação dos quartos">
      <header>
        <h2>Ocupação dos quartos</h2>
        <p>
          {leitosOcupados}/{leitos} leitos · {taxaOcupacao}%
        </p>
      </header>
      {ocupacao.length === 0 ? (
        <p className="ocupacao-quartos__vazio">Nenhum quarto cadastrado.</p>
      ) : (
        <ul>
          {ocupacao.map((quarto) => {
            const percentual = quarto.capacidade > 0 ? Math.round((quarto.ocupacao / quarto.capacidade) * 100) : 0;
            return (
              <li key={quarto.id}>
                <div className="ocupacao-quartos__topo">
                  <strong>Quarto {quarto.numero}</strong>
                  <span>
                    {quarto.ocupacao}/{quarto.capacidade} · {rotuloSituacao(quarto.situacao)}
                  </span>
                </div>
                <div className="ocupacao-quartos__trilha" aria-hidden="true">
                  <span style={{ width: `${percentual}%` }} />
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
