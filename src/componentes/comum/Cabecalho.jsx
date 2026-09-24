import "./Cabecalho.css";

export default function Cabecalho({ titulo, resumo }) {
  return (
    <header className="cabecalho">
      <div>
        <p className="cabecalho__sistema">Sistema de Informação Hospitalar</p>
        <h1 className="cabecalho__titulo">{titulo}</h1>
        {resumo ? <p className="cabecalho__resumo">{resumo}</p> : null}
      </div>
    </header>
  );
}
