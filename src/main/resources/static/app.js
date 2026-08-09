const out = document.querySelector('#output');

async function show(path) {
  const response = await fetch(path, { credentials: 'same-origin' });
  const text = await response.text();
  out.textContent = `${response.status} ${response.statusText}\n${text}`;
}

async function csrf() {
  const response = await fetch('/api/csrf', { credentials: 'same-origin' });
  if (!response.ok) throw new Error(`CSRF fetch failed: ${response.status}`);
  return response.json();
}

document.querySelector('#me').onclick = () => show('/api/me');
document.querySelector('#gate').onclick = () => show('/api/model-gate');
document.querySelector('#token').onclick = () => show('/api/token-status');
document.querySelector('#logout').onclick = async () => {
  try {
    const token = await csrf();
    const response = await fetch('/logout', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { [token.headerName]: token.token }
    });
    out.textContent = `${response.status} logout completed`;
    if (response.redirected) window.location = response.url;
  } catch (e) {
    out.textContent = String(e);
  }
};
