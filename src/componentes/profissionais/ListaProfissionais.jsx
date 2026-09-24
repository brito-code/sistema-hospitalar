import "./ListaProfissionais.css";

export default function ListaProfissionais({
  profissionais,
  carregando,
  buscaAtiva,
  aoCadastrar,
  aoEditar,
  aoDetalhar,
  aoExcluir,
}) {
  if (carregando) {
    return (
      <section className="lista-profissionais" aria-busy="true" aria-live="polite">
        <p className="lista-profissionais__estado">Carregando profissionais...</p>
      </section>
    );
  }

  if (!Array.isArray(profissionais) || profissionais.length === 0) {
    return (
      <section className="lista-profissionais">
        <div className="lista-profissionais__estado">
          <h2>{buscaAtiva ? "Nenhum profissional encontrado" : "Nenhum profissional cadastrado"}</h2>
          <p>
            {buscaAtiva
              ? "Ajuste o termo da busca para localizar outro registro."
              : "Cadastre o primeiro profissional da equipe de saúde."}
          </p>
          {buscaAtiva ? null : (
            <button type="button" className="lista-profissionais__novo" onClick={aoCadastrar}>
              Cadastrar profissional
            </button>
          )}
        </div>
      </section>
    );
  }

  return (
    <section className="lista-profissionais">
      <div className="lista-profissionais__rolagem">
        <table className="lista-profissionais__tabela">
          <caption>Profissionais cadastrados</caption>
          <thead>
            <tr>
              <th scope="col" className="lista-profissionais__nome">Nome</th>
              <th scope="col" className="lista-profissionais__registro">CRM/COREN</th>
              <th scope="col" className="lista-profissionais__especialidade">Especialidade</th>
              <th scope="col" className="lista-profissionais__telefone">Telefone</th>
              <th scope="col" className="lista-profissionais__email">E-mail</th>
              <th scope="col" className="lista-profissionais__operacoes">Ações</th>
            </tr>
          </thead>
          <tbody>
            {profissionais.map((profissional) => (
              <tr key={profissional.id}>
                <td className="lista-profissionais__nome">{profissional.nome || "—"}</td>
                <td className="lista-profissionais__registro">{profissional.registroProfissional || "—"}</td>
                <td className="lista-profissionais__especialidade">{profissional.especialidade || "—"}</td>
                <td className="lista-profissionais__telefone">{profissional.telefone || "—"}</td>
                <td className="lista-profissionais__email">{profissional.email || "—"}</td>
                <td className="lista-profissionais__operacoes">
                  <div className="lista-profissionais__acoes">
                    <button type="button" onClick={() => aoDetalhar(profissional)}>
                      Detalhar
                    </button>
                    <button type="button" onClick={() => aoEditar(profissional)}>
                      Editar
                    </button>
                    <button
                      type="button"
                      className="lista-profissionais__excluir"
                      onClick={() => aoExcluir(profissional)}
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
