import { formatarData, nomePorId, quartoPorId } from "./formatoQuarto";
import "./ListaInternacoes.css";

export default function ListaInternacoes({
  internacoes,
  pacientes,
  profissionais,
  quartos,
  carregando,
  buscaAtiva,
  aoRegistrar,
  aoEditar,
  aoDetalhar,
  aoExcluir,
}) {
  if (carregando) {
    return (
      <section className="lista-internacoes" aria-busy="true" aria-live="polite">
        <p className="lista-internacoes__estado">Carregando internações...</p>
      </section>
    );
  }

  if (!Array.isArray(internacoes) || internacoes.length === 0) {
    return (
      <section className="lista-internacoes">
        <div className="lista-internacoes__estado">
          <h2>{buscaAtiva ? "Nenhuma internação encontrada" : "Nenhuma internação registrada"}</h2>
          <p>
            {buscaAtiva
              ? "Ajuste a busca ou o filtro para localizar outro registro."
              : "Registre a primeira internação em um quarto com leito livre."}
          </p>
          {buscaAtiva ? null : (
            <button type="button" className="lista-internacoes__novo" onClick={aoRegistrar}>
              Registrar internação
            </button>
          )}
        </div>
      </section>
    );
  }

  return (
    <section className="lista-internacoes">
      <div className="lista-internacoes__rolagem">
        <table className="lista-internacoes__tabela">
          <caption>Internações registradas</caption>
          <thead>
            <tr>
              <th scope="col" className="lista-internacoes__pessoa">Paciente</th>
              <th scope="col" className="lista-internacoes__pessoa">Profissional</th>
              <th scope="col" className="lista-internacoes__quarto">Quarto</th>
              <th scope="col" className="lista-internacoes__data">Entrada</th>
              <th scope="col" className="lista-internacoes__data">Previsão de alta</th>
              <th scope="col" className="lista-internacoes__data">Alta efetiva</th>
              <th scope="col" className="lista-internacoes__operacoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            {internacoes.map((internacao) => {
              const quarto = quartoPorId(quartos, internacao.quartoId);
              return (
                <tr key={internacao.id}>
                  <td className="lista-internacoes__pessoa">{nomePorId(pacientes, internacao.pacienteId) || "—"}</td>
                  <td className="lista-internacoes__pessoa">{nomePorId(profissionais, internacao.profissionalId) || "—"}</td>
                  <td className="lista-internacoes__quarto">{quarto?.numeroIdentificacao || "—"}</td>
                  <td className="lista-internacoes__data">{formatarData(internacao.dataEntrada)}</td>
                  <td className="lista-internacoes__data">{formatarData(internacao.dataPrevistaAlta)}</td>
                  <td className="lista-internacoes__data">
                    {internacao.dataEfetivaAlta ? formatarData(internacao.dataEfetivaAlta) : "Em aberto"}
                  </td>
                  <td className="lista-internacoes__operacoes">
                    <div className="lista-internacoes__acoes">
                      <button type="button" onClick={() => aoDetalhar(internacao)}>
                        Detalhar
                      </button>
                      <button type="button" onClick={() => aoEditar(internacao)}>
                        Editar
                      </button>
                      <button type="button" className="lista-internacoes__excluir" onClick={() => aoExcluir(internacao)}>
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
