export const TIPOS_ATENDIMENTO = ["Consulta", "Internação"];
export const SITUACOES_CONSULTA = ["Agendada", "Realizada", "Cancelada"];

export function formatarData(iso) {
  if (typeof iso !== "string" || !iso) return "—";
  const [ano, mes, dia] = iso.split("-");
  if (!ano || !mes || !dia) return iso;
  return `${dia}/${mes}/${ano}`;
}

export function nomePorId(lista, id) {
  if (!Array.isArray(lista)) return "";
  return lista.find((item) => String(item.id) === String(id))?.nome ?? "";
}

export function horarioConflitante(consultas, { profissionalId, data, horario, situacao, idIgnorado }) {
  if (!profissionalId || !data || !horario || situacao === "Cancelada") return false;
  return consultas.some(
    (consulta) =>
      String(consulta.id) !== String(idIgnorado ?? "") &&
      consulta.situacao !== "Cancelada" &&
      String(consulta.profissionalId) === String(profissionalId) &&
      consulta.data === data &&
      consulta.horario === horario,
  );
}
