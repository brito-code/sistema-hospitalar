export function digitos(valor) {
  return String(valor ?? "").replace(/\D/g, "");
}

export function formatarTelefone(valor) {
  const telefone = digitos(valor).slice(0, 11);
  if (telefone.length === 0) return "";
  if (telefone.length < 3) return `(${telefone}`;
  if (telefone.length < 7) return `(${telefone.slice(0, 2)}) ${telefone.slice(2)}`;
  if (telefone.length <= 10) {
    return `(${telefone.slice(0, 2)}) ${telefone.slice(2, 6)}-${telefone.slice(6)}`;
  }
  return `(${telefone.slice(0, 2)}) ${telefone.slice(2, 7)}-${telefone.slice(7)}`;
}

export function emailValido(valor) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(valor ?? "").trim());
}

export function formatarRegistro(valor) {
  const texto = String(valor ?? "")
    .trim()
    .toUpperCase()
    .replace(/\s+/g, " ");
  const correspondencia = texto.match(/^(CRM|COREN)[-\s]*([A-Z]{2})[-\s]*(\d{4,7})$/);
  if (!correspondencia) return String(valor ?? "").trim();
  return `${correspondencia[1]}-${correspondencia[2]} ${correspondencia[3]}`;
}

export function registroValido(valor) {
  return /^(CRM|COREN)-[A-Z]{2} \d{4,7}$/.test(formatarRegistro(valor));
}
