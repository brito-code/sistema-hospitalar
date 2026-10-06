import { formatarData, nomePorId } from "../consultas/formatoConsulta";
import { ocupacaoAtiva } from "../quartos/formatoQuarto";

export function dataDeHoje(agora = new Date()) {
  const mes = String(agora.getMonth() + 1).padStart(2, "0");
  const dia = String(agora.getDate()).padStart(2, "0");
  return `${agora.getFullYear()}-${mes}-${dia}`;
}

export function montarPainel({ pacientes, profissionais, consultas, quartos, internacoes, hoje }) {
  const listaConsultas = Array.isArray(consultas) ? consultas : [];
  const listaQuartos = Array.isArray(quartos) ? quartos : [];
  const listaInternacoes = Array.isArray(internacoes) ? internacoes : [];
  const leitos = listaQuartos.reduce((total, quarto) => total + (Number(quarto.capacidadeMaxima) || 0), 0);
  const leitosOcupados = listaQuartos.reduce((total, quarto) => total + ocupacaoAtiva(listaInternacoes, quarto.id), 0);
  const consultasDeHoje = listaConsultas
    .filter((consulta) => consulta.data === hoje && consulta.situacao === "Agendada")
    .sort((a, b) => String(a.horario).localeCompare(String(b.horario)));
  const ocupacao = listaQuartos
    .map((quarto) => ({
      id: quarto.id,
      numero: quarto.numeroIdentificacao,
      andar: quarto.andar,
      capacidade: Number(quarto.capacidadeMaxima) || 0,
      ocupacao: ocupacaoAtiva(listaInternacoes, quarto.id),
      situacao: quarto.situacao,
    }))
    .sort((a, b) => String(a.numero).localeCompare(String(b.numero), "pt-BR", { numeric: true }));

  const alertas = [];
  listaConsultas
    .filter((consulta) => consulta.situacao === "Agendada" && consulta.data && consulta.data <= hoje)
    .sort((a, b) => String(a.data).localeCompare(String(b.data)) || String(a.horario).localeCompare(String(b.horario)))
    .forEach((consulta) => {
      alertas.push({
        id: `consulta-${consulta.id}`,
        gravidade: consulta.data < hoje ? "atraso" : "pendente",
        titulo: consulta.data < hoje ? "Consulta agendada em atraso" : "Consulta de hoje ainda agendada",
        detalhe: `${formatarData(consulta.data)} · ${consulta.horario || "—"} · ${nomePorId(pacientes, consulta.pacienteId) || "Paciente"} · ${nomePorId(profissionais, consulta.profissionalId) || "Profissional"}`,
      });
    });
  listaInternacoes
    .filter((internacao) => !internacao.dataEfetivaAlta)
    .sort((a, b) => String(a.dataPrevistaAlta).localeCompare(String(b.dataPrevistaAlta)))
    .forEach((internacao) => {
      const quarto = listaQuartos.find((item) => String(item.id) === String(internacao.quartoId));
      const atrasada = internacao.dataPrevistaAlta && internacao.dataPrevistaAlta < hoje;
      alertas.push({
        id: `internacao-${internacao.id}`,
        gravidade: atrasada ? "atraso" : "pendente",
        titulo: atrasada ? "Internação com alta prevista em atraso" : "Internação em aberto",
        detalhe: `${nomePorId(pacientes, internacao.pacienteId) || "Paciente"} · quarto ${quarto?.numeroIdentificacao || "—"} · previsão ${formatarData(internacao.dataPrevistaAlta)}`,
      });
    });

  return {
    totalPacientes: Array.isArray(pacientes) ? pacientes.length : 0,
    consultasHoje: consultasDeHoje.length,
    leitosOcupados,
    leitos,
    taxaOcupacao: leitos > 0 ? Math.round((leitosOcupados / leitos) * 100) : 0,
    profissionaisAtivos: Array.isArray(profissionais) ? profissionais.length : 0,
    consultasDeHoje,
    ocupacao,
    alertas,
  };
}
