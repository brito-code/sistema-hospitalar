export function digitos(valor) {
  return String(valor ?? "").replace(/\D/g, "");
}

export function formatarCpf(valor) {
  const cpf = digitos(valor).slice(0, 11);
  if (cpf.length <= 3) return cpf;
  if (cpf.length <= 6) return `${cpf.slice(0, 3)}.${cpf.slice(3)}`;
  if (cpf.length <= 9) {
    return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6)}`;
  }
  return `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}`;
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

export function formatarData(iso) {
  if (typeof iso !== "string" || !iso) return "—";
  const [ano, mes, dia] = iso.split("-");
  if (!ano || !mes || !dia) return iso;
  return `${dia}/${mes}/${ano}`;
}

export function cpfValido(valor) {
  const cpf = digitos(valor);
  if (cpf.length !== 11 || /^(\d)\1+$/.test(cpf)) return false;

  const calcularDigito = (base) => {
    let soma = 0;
    for (let indice = 0; indice < base.length; indice += 1) {
      soma += Number(base[indice]) * (base.length + 1 - indice);
    }
    const resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  };

  const primeiro = calcularDigito(cpf.slice(0, 9));
  const segundo = calcularDigito(cpf.slice(0, 10));
  return primeiro === Number(cpf[9]) && segundo === Number(cpf[10]);
}

export function emailValido(valor) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(valor ?? "").trim());
}
