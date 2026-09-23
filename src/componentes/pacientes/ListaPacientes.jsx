import { formatarData } from "./formatoPaciente";
import "./ListaPacientes.css";

export default function ListaPacientes({
  pacientes,
  carregando,
  buscaAtiva,
  aoCadastrar,
  aoEditar,
  aoDetalhar,
  aoExcluir,
}) {
  if (carregando) {
    return (
      <section className="lista-pacientes" aria-busy="true" aria-live="polite">
        <p className="lista-pacientes__estado">Carregando pacientes...</p>
      </section>
    );
  }

  if (!Array.isArray(pacientes) || pacientes.length === 0) {
    return (
      <section className="lista-pacientes">
        <div className="lista-pacientes__estado">
          <h2>{buscaAtiva ? "Nenhum paciente encontrado" : "Nenhum paciente cadastrado"}</h2>
          <p>
            {buscaAtiva
              ? "Ajuste o termo da busca para localizar outro registro."
              : "Cadastre o primeiro paciente para começar o prontuário."}
          </p>
          {buscaAtiva ? null : (
            <button type="button" className="lista-pacientes__novo" onClick={aoCadastrar}>
              Cadastrar paciente
            </button>
          )}
        </div>
      </section>
    );
  }

  return (
    <section className="lista-pacientes">
      <div className="lista-pacientes__rolagem">
        <table className="lista-pacientes__tabela">
          <caption>Pacientes cadastrados</caption>
          <thead>
            <tr>
              <th scope="col" className="lista-pacientes__nome">Nome</th>
              <th scope="col" className="lista-pacientes__cpf">CPF</th>
              <th scope="col">Nascimento</th>
              <th scope="col" className="lista-pacientes__telefone">Telefone</th>
              <th scope="col">E-mail</th>
              <th scope="col" className="lista-pacientes__endereco">Endereço</th>
              <th scope="col" className="lista-pacientes__operacoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            {pacientes.map((paciente) => (
              <tr key={paciente.id}>
                <td className="lista-pacientes__nome">{paciente.nome || "—"}</td>
                <td className="lista-pacientes__cpf">{paciente.cpf || "—"}</td>
                <td>{formatarData(paciente.dataNascimento)}</td>
                <td className="lista-pacientes__telefone">{paciente.telefone || "—"}</td>
                <td>{paciente.email || "—"}</td>
                <td className="lista-pacientes__endereco">{paciente.endereco || "—"}</td>
                <td className="lista-pacientes__operacoes">
                  <div className="lista-pacientes__acoes">
                    <button type="button" onClick={() => aoDetalhar(paciente)}>
                      Detalhar
                    </button>
                    <button type="button" onClick={() => aoEditar(paciente)}>
                      Editar
                    </button>
                    <button
                      type="button"
                      className="lista-pacientes__excluir"
                      onClick={() => aoExcluir(paciente)}
                    >
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
