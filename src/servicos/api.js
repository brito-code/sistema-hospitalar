import dadosSimulados from "./dadosSimulados";

const CHAVE_ARMAZENAMENTO = "sistema-hospitalar";

function armazenamento() {
  try {
    return typeof localStorage === "undefined" ? null : localStorage;
  } catch {
    return null;
  }
}

function copiarLista(lista) {
  return Array.isArray(lista) ? lista.map((item) => ({ ...item })) : [];
}

function registrosIniciais() {
  return {
    pacientes: copiarLista(dadosSimulados.pacientes),
    profissionais: copiarLista(dadosSimulados.profissionais),
    consultas: copiarLista(dadosSimulados.consultas).map((consulta) => ({
      ...consulta,
      tipo: consulta.tipo || "Consulta",
    })),
    quartos: copiarLista(dadosSimulados.quartos),
    internacoes: copiarLista(dadosSimulados.internacoes),
  };
}

function normalizar(lido) {
  if (!lido || typeof lido !== "object") return registrosIniciais();
  return {
    pacientes: copiarLista(lido.pacientes),
    profissionais: copiarLista(lido.profissionais),
    consultas: copiarLista(lido.consultas).map((consulta) => ({
      ...consulta,
      tipo: consulta.tipo || "Consulta",
    })),
    quartos: copiarLista(lido.quartos),
    internacoes: copiarLista(lido.internacoes),
  };
}

export function carregarRegistros() {
  const memoria = armazenamento();
  if (!memoria) return registrosIniciais();
  const bruto = memoria.getItem(CHAVE_ARMAZENAMENTO);
  if (!bruto) {
    const iniciais = registrosIniciais();
    memoria.setItem(CHAVE_ARMAZENAMENTO, JSON.stringify(iniciais));
    return iniciais;
  }
  try {
    return normalizar(JSON.parse(bruto));
  } catch {
    return registrosIniciais();
  }
}

export function salvarRegistros(registros) {
  const memoria = armazenamento();
  if (!memoria) return registrosIniciais();
  const normalizados = normalizar(registros);
  memoria.setItem(CHAVE_ARMAZENAMENTO, JSON.stringify(normalizados));
  return normalizados;
}

function substituirLista(nome, lista) {
  const atual = carregarRegistros();
  return salvarRegistros({ ...atual, [nome]: lista })[nome];
}

export function salvarPacientes(lista) {
  return substituirLista("pacientes", lista);
}

export function salvarProfissionais(lista) {
  return substituirLista("profissionais", lista);
}

export function salvarConsultas(lista) {
  return substituirLista("consultas", lista);
}

export function salvarQuartos(lista) {
  return substituirLista("quartos", lista);
}

export function salvarInternacoes(lista) {
  return substituirLista("internacoes", lista);
}
