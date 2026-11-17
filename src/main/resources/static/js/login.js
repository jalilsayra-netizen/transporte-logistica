/* login.js - Inicio de sesión: envía usuario y contraseña a POST /api/auth/login */
const formLogin = document.getElementById('formLogin');

formLogin.addEventListener('submit', async (ev) => {
    ev.preventDefault();
    formLogin.classList.add('was-validated');
    if (!formLogin.checkValidity()) return;

    const boton = document.getElementById('btnIngresar');
    boton.disabled = true;
    try {
        const respuesta = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            credentials: 'same-origin',
            body: new URLSearchParams({
                username: document.getElementById('username').value.trim(),
                password: document.getElementById('password').value
            })
        });
        if (respuesta.ok) {
            window.location.href = 'dashboard.html';
            return;
        }
        const datos = await respuesta.json().catch(() => ({}));
        mostrarAlerta('danger', datos.error || 'No se pudo iniciar sesión');
    } catch (e) {
        mostrarAlerta('danger', 'No se pudo conectar con el servidor');
    }
    boton.disabled = false;
});

/* En el login no se carga api.js, por eso esta función está acá */
function mostrarAlerta(tipo, mensaje) {
    document.getElementById('alertas').innerHTML =
        `<div class="alert alert-${tipo}" role="alert">${mensaje}</div>`;
}
