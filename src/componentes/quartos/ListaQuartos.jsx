import { ocupacaoAtiva, rotuloSituacao } from "./formatoQuarto";
import "./ListaQuartos.css";

export default function ListaQuartos({
  quartos,
  internacoes,
  carregando,
  buscaAtiva,
  aoCadastrar,
  aoEditar,
  aoDetalhar,
  aoExcluir,
}) {
  if (carregando) {
    return (
      <section className="lista-quartos" aria-busy="true" aria-live="polite">
        <p className="lista-quartos__estado">Carregando quartos...</p>
      </section>
    );
  }

  if (!Array.isArray(quartos) || quartos.length === 0) {
    return (
      <section className="lista-quartos">
        <div className="lista-quartos__estado">
          <h2>{buscaAtiva ? "Nenhum quarto encontrado" : "Nenhum quarto cadastrado"}</h2>
          <p>
            {buscaAtiva
              ? "Ajuste a busca ou o filtro para localizar outro leito."
              : "Cadastre o primeiro quarto para controlar a ocupação."}
          </p>
          {buscaAtiva ? null : (
            <button type="button" className="lista-quartos__novo" onClick={aoCadastrar}>
              Novo quarto
            </button>
          )}
        </div>
      </section>
    );
  }

  return (
    <section className="lista-quartos">
      <div className="lista-quartos__rolagem">
        <table className="lista-quartos__tabela">
          <caption>Quartos do hospital</caption>
          <thead>
            <tr>
              <th scope="col" className="lista-quartos__numero">Número</th>
              <th scope="col" className="lista-quartos__andar">Andar</th>
              <th scope="col" className="lista-quartos__capacidade">Capacidade</th>
              <th scope="col" className="lista-quartos__ocupacao">Ocupação</th>
              <th scope="col" className="lista-quartos__situacao">Status</th>
              <th scope="col" className="lista-quartos__operacoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            {quartos.map((quarto) => {
              const ocupacao = ocupacaoAtiva(internacoes, quarto.id);
              return (
                <tr key={quarto.id}>
                  <td className="lista-quartos__numero">{quarto.numeroIdentificacao || "—"}</td>
                  <td className="lista-quartos__andar">{quarto.andar ?? "—"}</td>
                  <td className="lista-quartos__capacidade">{quarto.capacidadeMaxima ?? "—"}</td>
                  <td className="lista-quartos__ocupacao">
                    {ocupacao}/{quarto.capacidadeMaxima ?? "—"}
                  </td>
                  <td className="lista-quartos__situacao">
                    <span className={`lista-quartos__selo lista-quartos__selo--${classeSituacao(quarto.situacao)}`}>
                      {rotuloSituacao(quarto.situacao)}
                    </span>
                  </td>
                  <td className="lista-quartos__operacoes">
                    <div className="lista-quartos__acoes">
                      <button type="button" onClick={() => aoDetalhar(quarto)}>
                        Detalhar
                      </button>
                      <button type="button" onClick={() => aoEditar(quarto)}>
                        Editar
                      </button>
                      <button type="button" className="lista-quartos__excluir" onClick={() => aoExcluir(quarto)}>
                        Excluir
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function classeSituacao(situacao) {
  return situacao === "OCUPADO" ? "ocupado" : "disponivel";
}
