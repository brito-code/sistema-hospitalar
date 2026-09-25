import { useEffect, useMemo } from "react";
import { formatarData } from "./formatoPaciente";
import "./DetalhesPaciente.css";

export default function DetalhesPaciente({
  paciente,
  consultas,
  internacoes,
  profissionais,
  quartos,
  aoFechar,
  aoEditar,
}) {
  useEffect(() => {
    function fecharComEsc(evento) {
      if (evento.key === "Escape") aoFechar();
    }
    document.addEventListener("keydown", fecharComEsc);
    return () => document.removeEventListener("keydown", fecharComEsc);
  }, [aoFechar]);

  const eventos = useMemo(() => {
    const listaConsultas = Array.isArray(consultas) ? consultas : [];
    const listaInternacoes = Array.isArray(internacoes) ? internacoes : [];

    const linhaConsultas = listaConsultas
      .filter((consulta) => consulta.pacienteId === paciente.id)
      .map((consulta) => ({
        id: `consulta-${consulta.id}`,
        tipo: consulta.tipo === "Internação" ? "Internação" : "Consulta",
        data: consulta.data,
        horario: consulta.horario,
        titulo: consulta.motivo || "Consulta sem motivo informado",
        detalhe: consulta.observacoesMedicas || "Sem observações médicas.",
        complemento: nomeDe(profissionais, consulta.profissionalId),
      }));

    const linhaInternacoes = listaInternacoes
      .filter((internacao) => internacao.pacienteId === paciente.id)
      .map((internacao) => ({
        id: `internacao-${internacao.id}`,
        tipo: "Internação",
        data: internacao.dataEntrada,
        horario: "",
        titulo: `Quarto ${numeroDe(quartos, internacao.quartoId)}`,
        detalhe: internacao.observacoes || "Sem observações.",
        complemento: `${nomeDe(profissionais, internacao.profissionalId)} · alta prevista ${formatarData(internacao.dataPrevistaAlta)} · alta efetiva ${internacao.dataEfetivaAlta ? formatarData(internacao.dataEfetivaAlta) : "em aberto"}`,
      }));

    return [...linhaConsultas, ...linhaInternacoes].sort((a, b) => {
      const porData = String(b.data).localeCompare(String(a.data));
      if (porData !== 0) return porData;
      return String(b.horario).localeCompare(String(a.horario));
    });
  }, [consultas, internacoes, paciente.id, profissionais, quartos]);

  function fecharSeFundo(evento) {
    if (evento.target === evento.currentTarget) aoFechar();
  }

  return (
    <div
      className="detalhes-paciente"
      role="dialog"
      aria-modal="true"
      aria-labelledby="detalhes-paciente-titulo"
      onClick={fecharSeFundo}
    >
      <div className="detalhes-paciente__corpo">
        <header className="detalhes-paciente__topo">
          <div>
            <p className="detalhes-paciente__etiqueta">Prontuário</p>
            <h2 id="detalhes-paciente-titulo">{paciente.nome || "Paciente"}</h2>
          </div>
          <button type="button" className="detalhes-paciente__fechar" onClick={aoFechar}>
            Fechar
          </button>
        </header>

        <dl className="detalhes-paciente__dados">
          <div>
            <dt>CPF</dt>
            <dd>{paciente.cpf || "—"}</dd>
          </div>
          <div>
            <dt>Nascimento</dt>
            <dd>{formatarData(paciente.dataNascimento)}</dd>
          </div>
          <div>
            <dt>Telefone</dt>
            <dd>{paciente.telefone || "—"}</dd>
          </div>
          <div>
            <dt>E-mail</dt>
            <dd>{paciente.email || "—"}</dd>
          </div>
          <div className="detalhes-paciente__largo">
            <dt>Endereço</dt>
            <dd>{paciente.endereco || "—"}</dd>
          </div>
        </dl>

        <section className="detalhes-paciente__historico" aria-label="Histórico médico">
          <h3>Histórico de consultas e internações</h3>
          {eventos.length === 0 ? (
            <p className="detalhes-paciente__vazio">
              Este paciente ainda não possui consultas ou internações registradas.
            </p>
          ) : (
            <ol className="detalhes-paciente__linha">
              {eventos.map((evento) => (
                <li key={evento.id}>
                  <span className="detalhes-paciente__tipo">{evento.tipo}</span>
                  <strong>
                    {formatarData(evento.data)}
                    {evento.horario ? ` · ${evento.horario}` : ""}
                  </strong>
                  <p>{evento.titulo}</p>
                  <p>{evento.complemento}</p>
                  <p>{evento.detalhe}</p>
                </li>
              ))}
            </ol>
          )}
        </section>

        <div className="detalhes-paciente__acoes">
          <button type="button" onClick={() => aoEditar(paciente)}>
            Editar paciente
          </button>
        </div>
      </div>
    </div>
  );
}

function nomeDe(profissionais, id) {
  if (!Array.isArray(profissionais)) return "Profissional não identificado";
  return profissionais.find((item) => item.id === id)?.nome ?? "Profissional não identificado";
}

function numeroDe(quartos, id) {
  if (!Array.isArray(quartos)) return "não identificado";
  return quartos.find((item) => item.id === id)?.numeroIdentificacao ?? "não identificado";
}
