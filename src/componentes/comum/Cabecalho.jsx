import "./Cabecalho.css";

export default function Cabecalho({ titulo, resumo, aoAbrirMenu }) {
  return (
    <header className="cabecalho">
      <button
        type="button"
        className="cabecalho__menu"
        onClick={aoAbrirMenu}
        aria-label="Abrir menu de navegação"
      >
        <span className="cabecalho__menu-barra" />
        <span className="cabecalho__menu-barra" />
        <span className="cabecalho__menu-barra" />
      </button>
      <div>
        <p className="cabecalho__sistema">Sistema de Informação Hospitalar</p>
        <h1 className="cabecalho__titulo">{titulo}</h1>
        {resumo ? <p className="cabecalho__resumo">{resumo}</p> : null}
      </div>
    </header>
  );
}
