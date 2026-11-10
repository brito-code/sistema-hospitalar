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

## API (backend)

O backend fica em `backend/`: Spring Boot 3.5, Java 17, Spring Data JPA e H2 em arquivo (perfil `h2`). Os dados ficam em `backend/data/`, que o Git ignora.

```bash
cd backend
mvn spring-boot:run   # API em http://localhost:8080
mvn test              # testes com banco H2 próprio, recriado a cada execução
```

Com a API no ar, o console do H2 abre em `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/hospital`, usuário `sa`, senha vazia). Para usar outro banco, crie um `application-<perfil>.properties` e troque `spring.profiles.active`.

O front-end em Vite (porta 5173) já está liberado por CORS. Para outras origens, ajuste `hospital.cors.origens` no `application.properties`.

### Pacientes

| Método | Rota | O que faz | Respostas |
| --- | --- | --- | --- |
| `POST` | `/pacientes` | Cadastra um paciente | `201`, `400` (dados inválidos), `409` (CPF já cadastrado) |
| `GET` | `/pacientes` | Lista em ordem alfabética; `?nome=` filtra pelo nome | `200` |
| `GET` | `/pacientes/{id}` | Busca um paciente | `200`, `404` |
| `PUT` | `/pacientes/{id}` | Atualiza os dados | `200`, `400`, `404`, `409` |
| `DELETE` | `/pacientes/{id}` | Exclui o paciente | `204`, `404`, `409` (tem atendimentos no histórico) |

```json
{
  "nome": "Ana Clara Mendes",
  "cpf": "123.456.789-09",
  "dataNascimento": "1988-03-14",
  "telefone": "(31) 98812-3401",
  "email": "ana.mendes@email.com",
  "endereco": {
    "logradouro": "Rua da Bahia",
    "numero": "1200",
    "complemento": "Apto 301",
    "bairro": "Centro",
    "cidade": "Belo Horizonte",
    "uf": "MG",
    "cep": "30160-011"
  }
}
```

O CPF é aceito com ou sem máscara, validado pelos dígitos verificadores e guardado só com os 11 dígitos. A resposta devolve o CPF e o CEP com máscara e a idade calculada a partir da data de nascimento. Como o histórico de consultas e internações é mantido, um paciente que já teve atendimento não pode ser excluído.

### Emergências

| Método | Rota | O que faz | Respostas |
| --- | --- | --- | --- |
| `POST` | `/emergencias` | Registra a chegada do paciente (status `AGUARDANDO_AVALIACAO`) | `201`, `400`, `404` |
| `GET` | `/emergencias` | Lista; filtros `?pacienteId=` e `?status=` | `200`, `400` |
| `GET` | `/emergencias/aguardando` | Fila de espera: prioridade mais grave primeiro, empate por ordem de chegada | `200` |
| `GET` | `/emergencias/{id}` | Busca um atendimento | `200`, `404` |
| `PATCH` | `/emergencias/{id}/classificar` | Triagem: define a prioridade (`BAIXA`, `MODERADA`, `ALTA`, `CRITICA`) | `200`, `400`, `404`, `409` |
| `PATCH` | `/emergencias/{id}/iniciar` | `AGUARDANDO_AVALIACAO` → `EM_ATENDIMENTO` | `200`, `404`, `409` (sem prioridade) |
| `PATCH` | `/emergencias/{id}/concluir` | `EM_ATENDIMENTO` → `CONCLUIDO`; corpo opcional `{"observacoes": "..."}` | `200`, `404`, `409` |

```json
{
  "pacienteId": 1,
  "profissionalId": 1,
  "prioridade": "ALTA",
  "queixaPrincipal": "Dor no peito",
  "dataHoraChegada": "2026-11-10T14:30:00"
}
```

`prioridade` e `dataHoraChegada` são opcionais no cadastro (sem data, vale o momento atual), mas o atendimento só pode ser iniciado e concluído com a prioridade definida.

### Procedimentos cirúrgicos

| Método | Rota | O que faz | Respostas |
| --- | --- | --- | --- |
| `POST` | `/procedimentos` | Agenda um procedimento (status `AGENDADO`) | `201`, `400`, `404`, `409` (não é cirurgião ou horário ocupado) |
| `GET` | `/procedimentos` | Lista; filtros `?profissionalId=`, `?pacienteId=` e `?status=` | `200`, `400` |
| `GET` | `/procedimentos/{id}` | Busca um procedimento | `200`, `404` |
| `PATCH` | `/procedimentos/{id}/iniciar` | `AGENDADO` → `EM_ANDAMENTO` | `200`, `404`, `409` |
| `PATCH` | `/procedimentos/{id}/concluir` | `EM_ANDAMENTO` → `CONCLUIDO`; corpo opcional `{"observacoes": "..."}` | `200`, `404`, `409` (não iniciado) |
| `PATCH` | `/procedimentos/{id}/cancelar` | `AGENDADO` → `CANCELADO` | `200`, `404`, `409` |

```json
{
  "pacienteId": 1,
  "profissionalId": 2,
  "descricao": "Apendicectomia",
  "dataHoraAgendada": "2026-11-20T09:00:00"
}
```

O responsável precisa ser um profissional do tipo `CIRURGIAO`, e ele não pode ter outro procedimento nem uma consulta no mesmo dia e horário. Procedimento cancelado libera o horário.

### Formato dos erros

Toda resposta de erro da API tem o mesmo corpo:

```json
{
  "timestamp": "2026-11-10T10:15:30.123",
  "status": 400,
  "mensagem": "Dados invalidos",
  "caminho": "/pacientes",
  "detalhes": ["cpf: CPF invalido", "endereco.cep: deve ter 8 digitos (00000-000)"]
}
```

`400` é dado inválido na requisição, `404` é recurso inexistente, `409` é regra de negócio ou conflito com o estado atual dos dados (CPF repetido, quarto lotado, alta inválida) e `500` é erro inesperado, sem detalhes internos na resposta.
