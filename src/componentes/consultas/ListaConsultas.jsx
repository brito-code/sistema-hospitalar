import { formatarData, nomePorId } from "./formatoConsulta";
import "./ListaConsultas.css";

export default function ListaConsultas({
  consultas,
  pacientes,
  profissionais,
  carregando,
  buscaAtiva,
  aoAgendar,
  aoEditar,
  aoDetalhar,
  aoExcluir,
}) {
  if (carregando) {
    return (
      <section className="lista-consultas" aria-busy="true" aria-live="polite">
        <p className="lista-consultas__estado">Carregando consultas...</p>
      </section>
    );
  }

  if (!Array.isArray(consultas) || consultas.length === 0) {
    return (
      <section className="lista-consultas">
        <div className="lista-consultas__estado">
          <h2>{buscaAtiva ? "Nenhuma consulta encontrada" : "Nenhuma consulta agendada"}</h2>
          <p>
            {buscaAtiva
              ? "Ajuste a busca ou os filtros para localizar outro horário."
              : "Agende a primeira consulta para montar a agenda do hospital."}
          </p>
          {buscaAtiva ? null : (
            <button type="button" className="lista-consultas__novo" onClick={aoAgendar}>
              Agendar consulta
            </button>
          )}
        </div>
      </section>
    );
  }

  return (
    <section className="lista-consultas">
      <div className="lista-consultas__rolagem">
        <table className="lista-consultas__tabela">
          <caption>Consultas da agenda</caption>
          <thead>
            <tr>
              <th scope="col" className="lista-consultas__tipo">Tipo</th>
              <th scope="col" className="lista-consultas__quando">Data</th>
              <th scope="col" className="lista-consultas__quando">Hora</th>
              <th scope="col" className="lista-consultas__pessoa">Paciente</th>
              <th scope="col" className="lista-consultas__pessoa">Profissional</th>
              <th scope="col" className="lista-consultas__especialidade">Especialidade</th>
              <th scope="col" className="lista-consultas__situacao">Status</th>
              <th scope="col" className="lista-consultas__operacoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            {consultas.map((consulta) => (
              <tr key={consulta.id}>
                <td className="lista-consultas__tipo">{consulta.tipo || "Consulta"}</td>
                <td className="lista-consultas__quando">{formatarData(consulta.data)}</td>
                <td className="lista-consultas__quando">{consulta.horario || "—"}</td>
                <td className="lista-consultas__pessoa">{nomePorId(pacientes, consulta.pacienteId) || "—"}</td>
                <td className="lista-consultas__pessoa">{nomePorId(profissionais, consulta.profissionalId) || "—"}</td>
                <td className="lista-consultas__especialidade">{consulta.especialidade || "—"}</td>
                <td className="lista-consultas__situacao">
                  <span className={`lista-consultas__selo lista-consultas__selo--${classeSituacao(consulta.situacao)}`}>
                    {consulta.situacao || "—"}
                  </span>
                </td>
                <td className="lista-consultas__operacoes">
                  <div className="lista-consultas__acoes">
                    <button type="button" onClick={() => aoDetalhar(consulta)}>
                      Detalhar
                    </button>
                    <button type="button" onClick={() => aoEditar(consulta)}>
                      Editar
                    </button>
                    <button type="button" className="lista-consultas__excluir" onClick={() => aoExcluir(consulta)}>
                      Excluir
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function classeSituacao(situacao) {
  if (situacao === "Realizada") return "realizada";
  if (situacao === "Cancelada") return "cancelada";
  return "agendada";
}
