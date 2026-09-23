/**
 * Dados iniciais do Sistema de Informação Hospitalar.
 * Servem de semente para a camada simulada de API (localStorage).
 *
 * Os registros já respeitam as regras de negócio:
 * - nenhum profissional possui duas consultas no mesmo dia e horário;
 * - internações ativas (sem data efetiva de alta) não ultrapassam a capacidade do quarto;
 * - quartos com ocupação igual à capacidade estão OCUPADOS; os demais, DISPONIVEIS.
 */

export const pacientes = [
  {
    id: 1,
    nome: "Ana Clara Mendes",
    cpf: "123.456.789-09",
    dataNascimento: "1988-03-14",
    telefone: "(31) 98812-3401",
    endereco: "Rua da Bahia, 1200, Centro, Belo Horizonte - MG",
    email: "ana.mendes@email.com",
  },
  {
    id: 2,
    nome: "Bruno Henrique Costa",
    cpf: "987.654.321-00",
    dataNascimento: "1975-11-02",
    telefone: "(31) 99720-1184",
    endereco: "Av. Afonso Pena, 3500, Funcionários, Belo Horizonte - MG",
    email: "bruno.costa@email.com",
  },
  {
    id: 3,
    nome: "Camila Souza Ribeiro",
    cpf: "456.789.123-64",
    dataNascimento: "1996-07-21",
    telefone: "(31) 98455-9023",
    endereco: "Rua Fernandes Tourinho, 450, Savassi, Belo Horizonte - MG",
    email: "camila.ribeiro@email.com",
  },
  {
    id: 4,
    nome: "Diego Alves Ferreira",
    cpf: "321.654.987-91",
    dataNascimento: "1962-01-30",
    telefone: "(31) 3331-7780",
    endereco: "Rua Padre Eustáquio, 890, Padre Eustáquio, Belo Horizonte - MG",
    email: "diego.ferreira@email.com",
  },
  {
    id: 5,
    nome: "Elisa Martins Prado",
    cpf: "159.753.486-25",
    dataNascimento: "2014-09-08",
    telefone: "(31) 99102-6647",
    endereco: "Rua Grão Pará, 220, Santa Efigênia, Belo Horizonte - MG",
    email: "elisa.prado@email.com",
  },
  {
    id: 6,
    nome: "Felipe Gomes Duarte",
    cpf: "753.159.852-37",
    dataNascimento: "1991-12-19",
    telefone: "(31) 98640-2215",
    endereco: "Av. do Contorno, 6100, Lourdes, Belo Horizonte - MG",
    email: "felipe.duarte@email.com",
  },
];

export const profissionais = [
  {
    id: 1,
    nome: "Dra. Mariana Lopes",
    registroProfissional: "CRM-MG 45231",
    especialidade: "Cardiologia",
    telefone: "(31) 3224-1001",
    email: "mariana.lopes@hospital.edu.br",
  },
  {
    id: 2,
    nome: "Dr. Rafael Nunes",
    registroProfissional: "CRM-MG 38902",
    especialidade: "Clínica Médica",
    telefone: "(31) 3224-1002",
    email: "rafael.nunes@hospital.edu.br",
  },
  {
    id: 3,
    nome: "Enf. Patrícia Oliveira",
    registroProfissional: "COREN-MG 214567",
    especialidade: "Enfermagem",
    telefone: "(31) 3224-1003",
    email: "patricia.oliveira@hospital.edu.br",
  },
  {
    id: 4,
    nome: "Dr. Lucas Andrade",
    registroProfissional: "CRM-MG 51044",
    especialidade: "Ortopedia",
    telefone: "(31) 3224-1004",
    email: "lucas.andrade@hospital.edu.br",
  },
  {
    id: 5,
    nome: "Dra. Beatriz Campos",
    registroProfissional: "CRM-MG 27890",
    especialidade: "Pediatria",
    telefone: "(31) 3224-1005",
    email: "beatriz.campos@hospital.edu.br",
  },
];

export const consultas = [
  {
    id: 1,
    pacienteId: 1,
    profissionalId: 1,
    data: "2026-09-24",
    horario: "08:00",
    motivo: "Acompanhamento de hipertensão arterial",
    observacoesMedicas: "Pressão 138x86 mmHg. Manter losartana e retorno em 30 dias.",
  },
  {
    id: 2,
    pacienteId: 5,
    profissionalId: 1,
    data: "2026-09-24",
    horario: "09:30",
    motivo: "Avaliação de sopro cardíaco",
    observacoesMedicas: "Solicitado ecocardiograma. Sem sinais de descompensação.",
  },
  {
    id: 3,
    pacienteId: 2,
    profissionalId: 2,
    data: "2026-09-24",
    horario: "08:00",
    motivo: "Consulta de rotina e renovação de receitas",
    observacoesMedicas: "Paciente estável. Orientado sobre alimentação e atividade física.",
  },
  {
    id: 4,
    pacienteId: 4,
    profissionalId: 2,
    data: "2026-09-24",
    horario: "14:00",
    motivo: "Dor abdominal e náuseas há três dias",
    observacoesMedicas: "Abdome flácido, sem defesa. Solicitados hemograma e ultrassom.",
  },
  {
    id: 5,
    pacienteId: 3,
    profissionalId: 1,
    data: "2026-09-25",
    horario: "10:00",
    motivo: "Palpitações aos esforços",
    observacoesMedicas: "Eletrocardiograma sem alterações agudas. Retorno com Holter.",
  },
  {
    id: 6,
    pacienteId: 6,
    profissionalId: 4,
    data: "2026-09-23",
    horario: "15:00",
    motivo: "Dor no joelho direito após queda",
    observacoesMedicas: "Suspeita de entorse. Radiografia sem fratura. Imobilização e gelo.",
  },
  {
    id: 7,
    pacienteId: 5,
    profissionalId: 5,
    data: "2026-09-26",
    horario: "11:00",
    motivo: "Consulta pediátrica de rotina",
    observacoesMedicas: "Desenvolvimento adequado para a idade. Calendário vacinal em dia.",
  },
];

export const quartos = [
  {
    id: 1,
    numeroIdentificacao: "101",
    andar: 1,
    capacidadeMaxima: 2,
    situacao: "OCUPADO",
  },
  {
    id: 2,
    numeroIdentificacao: "102",
    andar: 1,
    capacidadeMaxima: 1,
    situacao: "DISPONIVEL",
  },
  {
    id: 3,
    numeroIdentificacao: "201",
    andar: 2,
    capacidadeMaxima: 1,
    situacao: "OCUPADO",
  },
  {
    id: 4,
    numeroIdentificacao: "202",
    andar: 2,
    capacidadeMaxima: 2,
    situacao: "DISPONIVEL",
  },
  {
    id: 5,
    numeroIdentificacao: "301",
    andar: 3,
    capacidadeMaxima: 3,
    situacao: "DISPONIVEL",
  },
  {
    id: 6,
    numeroIdentificacao: "302",
    andar: 3,
    capacidadeMaxima: 2,
    situacao: "DISPONIVEL",
  },
];

export const internacoes = [
  {
    id: 1,
    pacienteId: 1,
    profissionalId: 1,
    quartoId: 1,
    dataEntrada: "2026-09-20",
    dataPrevistaAlta: "2026-09-27",
    dataEfetivaAlta: null,
    observacoes: "Internação para ajuste de medicação anti-hipertensiva e monitoramento.",
  },
  {
    id: 2,
    pacienteId: 2,
    profissionalId: 2,
    quartoId: 1,
    dataEntrada: "2026-09-21",
    dataPrevistaAlta: "2026-09-28",
    dataEfetivaAlta: null,
    observacoes: "Investigação de dor abdominal. Aguardando resultado de exames.",
  },
  {
    id: 3,
    pacienteId: 6,
    profissionalId: 4,
    quartoId: 3,
    dataEntrada: "2026-09-22",
    dataPrevistaAlta: "2026-09-26",
    dataEfetivaAlta: null,
    observacoes: "Observação ortopédica após entorse de joelho. Repouso e analgesia.",
  },
  {
    id: 4,
    pacienteId: 4,
    profissionalId: 2,
    quartoId: 2,
    dataEntrada: "2026-08-10",
    dataPrevistaAlta: "2026-08-15",
    dataEfetivaAlta: "2026-08-14",
    observacoes: "Alta em bom estado geral após tratamento de infecção urinária.",
  },
  {
    id: 5,
    pacienteId: 3,
    profissionalId: 5,
    quartoId: 5,
    dataEntrada: "2026-07-01",
    dataPrevistaAlta: "2026-07-08",
    dataEfetivaAlta: "2026-07-07",
    observacoes: "Internação breve por desidratação. Alta com orientação alimentar.",
  },
];

const dadosSimulados = {
  pacientes,
  profissionais,
  consultas,
  quartos,
  internacoes,
};

export default dadosSimulados;
