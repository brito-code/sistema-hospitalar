export const SITUACOES_QUARTO = ["DISPONIVEL", "OCUPADO"];

export function formatarData(iso) {
  if (typeof iso !== "string" || !iso) return "—";
  const [ano, mes, dia] = iso.split("-");
  if (!ano || !mes || !dia) return iso;
  return `${dia}/${mes}/${ano}`;
}

export function rotuloSituacao(situacao) {
  if (situacao === "OCUPADO") return "Ocupado";
  if (situacao === "DISPONIVEL") return "Disponível";
  return situacao || "—";
}

export function nomePorId(lista, id) {
  if (!Array.isArray(lista)) return "";
  return lista.find((item) => String(item.id) === String(id))?.nome ?? "";
}

export function quartoPorId(quartos, id) {
  if (!Array.isArray(quartos)) return null;
  return quartos.find((quarto) => String(quarto.id) === String(id)) ?? null;
}

export function ocupacaoAtiva(internacoes, quartoId, idIgnorado) {
  if (!Array.isArray(internacoes)) return 0;
  return internacoes.filter(
    (internacao) =>
      String(internacao.id) !== String(idIgnorado ?? "") &&
      String(internacao.quartoId) === String(quartoId) &&
      !internacao.dataEfetivaAlta,
  ).length;
}

export function situacaoPorOcupacao(capacidade, ocupacao) {
  return Number(ocupacao) >= Number(capacidade) ? "OCUPADO" : "DISPONIVEL";
}

export function sincronizarSituacao(quartos, internacoes) {
  if (!Array.isArray(quartos)) return [];
  return quartos.map((quarto) => ({
    ...quarto,
    situacao: situacaoPorOcupacao(quarto.capacidadeMaxima, ocupacaoAtiva(internacoes, quarto.id)),
  }));
}

export function quartoSemVaga(quartos, internacoes, { quartoId, idIgnorado, dataEfetivaAlta }) {
  if (dataEfetivaAlta) return false;
  const quarto = quartoPorId(quartos, quartoId);
  if (!quarto) return false;
  return ocupacaoAtiva(internacoes, quartoId, idIgnorado) >= Number(quarto.capacidadeMaxima);
}
