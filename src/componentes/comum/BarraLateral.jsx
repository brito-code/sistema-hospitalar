import "./BarraLateral.css";

export default function BarraLateral({
  itens,
  paginaAtiva,
  aberto,
  aoNavegar,
  aoFechar,
}) {
  return (
    <>
      <button
        type="button"
        className={aberto ? "barra-lateral__cobertura barra-lateral__cobertura--visivel" : "barra-lateral__cobertura"}
        aria-label="Fechar menu"
        tabIndex={aberto ? 0 : -1}
        onClick={aoFechar}
      />
      <aside className={aberto ? "barra-lateral barra-lateral--aberto" : "barra-lateral"}>
        <div className="barra-lateral__marca">
          <svg className="barra-lateral__selo" viewBox="0 0 42 42" aria-hidden="true">
            <defs>
              <linearGradient id="selo-aurora" x1="4" y1="2" x2="40" y2="42" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stopColor="#5ee0d6" />
                <stop offset="48%" stopColor="#14918a" />
                <stop offset="100%" stopColor="#083f3c" />
              </linearGradient>
            </defs>
            <rect width="42" height="42" rx="13" fill="url(#selo-aurora)" />
            <rect
              x="1.3"
              y="1.3"
              width="39.4"
              height="39.4"
              rx="12"
              fill="none"
              stroke="#ffffff"
              strokeOpacity="0.45"
              strokeWidth="1.2"
            />
            <circle cx="21" cy="21" r="13.4" fill="#042e2c" fillOpacity="0.22" />
            <path
              d="M21 7.6v26.2"
              fill="none"
              stroke="#fff"
              strokeWidth="2.35"
              strokeLinecap="round"
            />
            <path d="M16.6 7.8h8.8" fill="none" stroke="#fff" strokeWidth="2.35" strokeLinecap="round" />
            <circle cx="21" cy="7.8" r="1.85" fill="#fff" />
            <path
              d="M13.8 30.8c2.4 3.4 7.8 4.1 11.2 1.7 4-2.8 2.4-6.8-2-8.1-5-1.5-7.8-4-6.7-7.8 1.1-3.7 5.6-5 9.2-3 2 1.1 3.4 2.7 4 4.6"
              fill="none"
              stroke="#fff"
              strokeWidth="2.7"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M29.4 17.6c1.7 0 2.8 1.6 1.9 3-1.4 2.1-4 .6-3.7-1.2.2-.9.9-1.7 1.8-1.8z"
              fill="#fff"
            />
            <circle cx="30.6" cy="18.6" r="0.48" fill="#083f3c" />
          </svg>
          <div>
            <strong>Hospital Aurora</strong>
            <p>Gestão de atendimentos</p>
          </div>
        </div>
        <nav className="barra-lateral__navegacao" aria-label="Seções do sistema">
          {itens.map((item) => {
            const ativo = item.id === paginaAtiva;
            return (
              <button
                key={item.id}
                type="button"
                className={ativo ? "barra-lateral__item barra-lateral__item--ativo" : "barra-lateral__item"}
                aria-current={ativo ? "page" : undefined}
                onClick={() => aoNavegar(item.id)}
              >
                {item.rotulo}
              </button>
            );
          })}
        </nav>
      </aside>
    </>
  );
}
