# Sistema de Informação Hospitalar

**Pontifícia Universidade Católica de Minas Gerais**  
**Bacharelado em Engenharia de Software**  
**Programação Modular**

Hospitais de médio porte ainda dependem de registros manuais para pacientes, profissionais, consultas, internações e quartos. Esse controle disperso dificulta a organização dos atendimentos, aumenta o risco de erro e não oferece uma visão confiável para a rotina administrativa e médica.

Este projeto é a interface de um **Sistema de Informação Hospitalar**: um sistema para centralizar e gerenciar as informações dos atendimentos. A interface é modular, em React, e os dados de demonstração ficam simulados até a integração com a API.

## Objetivo

Disponibilizar o gerenciamento das operações do hospital, cobrindo:

- cadastro e consulta de pacientes, com histórico de atendimentos;
- cadastro de profissionais da saúde;
- agendamento e controle de consultas;
- controle de internações e de quartos;
- respeito às regras de disponibilidade e de integridade dos dados.

O sistema completo previsto no trabalho também inclui modelagem orientada a objetos, arquitetura em camadas, API REST com Spring Boot, persistência em banco de dados, tratamento de exceções e testes. Nesta etapa, a aplicação entrega a interface e os dados iniciais simulados.

## Escopo

O sistema deve permitir:

- gerenciamento de pacientes;
- gerenciamento de profissionais da saúde;
- agendamento e controle de consultas;
- controle de internações;
- gerenciamento de quartos hospitalares;
- controle de disponibilidade de atendimento;
- registro do histórico de atendimentos;
- consulta de informações médicas e administrativas.

## Regras de negócio

1. Um paciente pode ter várias consultas e internações ao longo do tempo.
2. Cada consulta está ligada a um paciente e a um profissional responsável.
3. Um profissional não pode ter dois atendimentos no mesmo dia e horário.
4. Uma internação está ligada a um paciente e a um quarto disponível.
5. Um quarto não pode ultrapassar a capacidade máxima de ocupação.
6. O histórico de consultas e internações do paciente é mantido.
7. As operações respeitam a disponibilidade dos recursos e a integridade dos dados.

## Informações de cada entidade

| Entidade | Dados mínimos |
| --- | --- |
| Paciente | Nome, CPF, data de nascimento, telefone, endereço e e-mail |
| Profissional da saúde | Nome, registro profissional (CRM/COREN), especialidade, telefone e e-mail |
| Consulta | Paciente, profissional responsável, data, horário, motivo e observações médicas |
| Internação | Paciente, profissional responsável, quarto, data de entrada, data prevista de alta, data efetiva de alta e observações |
| Quarto | Número de identificação, andar, capacidade máxima e situação (`DISPONIVEL` ou `OCUPADO`) |

O detalhe do paciente reúne, numa linha do tempo, as consultas e as internações daquele paciente.

## Interface

A tela é feita com **React** e **Vite**. Estilos e lógica ficam fora do `index.html`: cada parte da interface tem seu componente e seu arquivo CSS.

```text
src/
├── recursos/estilos/          # Estilos globais e variáveis de tema
├── componentes/
│   ├── comum/                 # Cabeçalho e barra lateral
│   └── pacientes/             # Lista, formulário, detalhes e formatação
├── paginas/                   # Painel, pacientes, profissionais, consultas e quartos
├── servicos/
│   └── dadosSimulados.js      # Dados iniciais de pacientes, profissionais, consultas, quartos e internações
├── App.jsx
└── main.jsx
```

A navegação entre as seções fica no estado da aplicação. A tela de pacientes lista, cadastra, edita, detalha e exclui registros a partir desses dados simulados. As demais seções estão preparadas na navegação.

## Como executar

É necessário ter o [Node.js](https://nodejs.org/) instalado.

```bash
npm install
npm run dev
```

O Vite informa o endereço local, em geral `http://localhost:5173/`.

| Comando | Uso |
| --- | --- |
| `npm run dev` | Sobe a aplicação em desenvolvimento |
| `npm run build` | Gera a versão de produção em `dist/` |
| `npm run preview` | Serve a versão gerada |
| `npm run lint` | Verifica o código com o ESLint |
