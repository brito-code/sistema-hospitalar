import { formatarData, nomePorId } from "../consultas/formatoConsulta";
import "./ConsultasDoDia.css";

export default function ConsultasDoDia({ consultas, pacientes, profissionais, hoje }) {
  return (
    <section className="consultas-do-dia" aria-label="Próximas consultas do dia">
      <header>
        <h2>Próximas consultas do dia</h2>
        <p>{formatarData(hoje)}</p>
      </header>
      {consultas.length === 0 ? (
        <p className="consultas-do-dia__vazio">Nenhuma consulta agendada para hoje.</p>
      ) : (
        <ol>
          {consultas.map((consulta) => (
            <li key={consulta.id}>
              <strong>{consulta.horario || "—"}</strong>
              <span>{nomePorId(pacientes, consulta.pacienteId) || "Paciente"}</span>
              <span>{nomePorId(profissionais, consulta.profissionalId) || "Profissional"}</span>
              <span>{consulta.especialidade || "—"}</span>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}
