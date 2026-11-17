/* =====================================================================
 * api.js - Funciones que usan TODAS las pantallas.
 *
 *   api(url, opciones)       -> llama a la API del backend y devuelve el JSON
 *   iniciarPagina(roles)     -> verifica que haya sesión y dibuja el menú
 *   mostrarAlerta / mostrarError -> mensajes de Bootstrap (verde / rojo)
 *   leerFormulario / llenarFormulario -> pasan datos entre un <form> y un objeto
 *   formatearFecha, etiquetaEstado, esc ... -> ayudas para mostrar datos
 * ===================================================================== */

/* ---------------------------------------------------------------- llamadas a la API */

// Error que lanza api() cuando el servidor responde con un problema
class ErrorApi extends Error {
    constructor(mensaje, campos) {
        super(mensaje);
        this.campos = campos || null; // detalle por campo cuando falla una validación
    }
}

/**
 * Hace una llamada a la API y devuelve la respuesta ya convertida a objeto.
 * Ejemplos:
 *   const clientes = await api('/api/clientes');
 *   await api('/api/clientes', { metodo: 'POST', cuerpo: { razonSocial: 'ACME' } });
 */
async function api(url, { metodo = 'GET', cuerpo = null } = {}) {
    const opciones = { method: metodo, headers: {}, credentials: 'same-origin' };
    if (cuerpo !== null) {
        opciones.headers['Content-Type'] = 'application/json';
        opciones.body = JSON.stringify(cuerpo);
    }
    const respuesta = await fetch(url, opciones);

    if (respuesta.status === 401) { // no hay sesión: volver al login
        window.location.href = 'login.html';
        throw new ErrorApi('Debe iniciar sesión');
    }
    const texto = await respuesta.text();
    let datos = null;
    if (texto) {
        try { datos = JSON.parse(texto); } catch (e) { datos = null; }
    }
    if (!respuesta.ok) {
        throw new ErrorApi((datos && datos.error) || 'Error inesperado del servidor', datos && datos.campos);
    }
    return datos;
}

/* ---------------------------------------------------------------- mensajes */

// Escapa un texto antes de ponerlo en el HTML (evita que alguien inyecte código)
function esc(valor) {
    if (valor === null || valor === undefined) return '';
    return String(valor)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

// tipo: 'success' (verde), 'danger' (rojo), 'warning' (amarillo), 'info' (celeste)
function mostrarAlerta(tipo, mensaje, contenedor = 'alertas') {
    const caja = document.getElementById(contenedor);
    caja.innerHTML = `<div class="alert alert-${tipo} alert-dismissible fade show" role="alert">
        ${mensaje}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button></div>`;
    if (tipo === 'success') {
        setTimeout(() => { caja.innerHTML = ''; }, 4500);
    }
}

// Muestra el mensaje de un error de la API (y el detalle por campo si lo hay)
function mostrarError(error, contenedor = 'alertas') {
    let html = `<strong>${esc(error.message)}</strong>`;
    if (error.campos) {
        html += '<ul class="mb-0 mt-1">' +
            Object.values(error.campos).map(m => `<li>${esc(m)}</li>`).join('') + '</ul>';
    }
    mostrarAlerta('danger', html, contenedor);
}

/* ---------------------------------------------------------------- formularios */

/**
 * Convierte un <form> en un objeto listo para mandar a la API.
 * Si un campo se llama "cliente.idCliente", arma un objeto adentro de otro:
 *   { cliente: { idCliente: 5 } }
 * Los campos numéricos se convierten a número y los vacíos de fecha/lista a null.
 */
function leerFormulario(form) {
    const objeto = {};
    for (const campo of form.elements) {
        if (!campo.name) continue;
        let valor = campo.value;
        const esNumero = campo.type === 'number' || campo.dataset.numero === 'true';
        const vacioEspecial = campo.tagName === 'SELECT' || ['date', 'datetime-local', 'time'].includes(campo.type);
        if (valor === '' && (esNumero || vacioEspecial)) {
            valor = null;
        } else if (esNumero) {
            valor = Number(valor);
        }
        const partes = campo.name.split('.');
        if (partes.length > 1 && valor === null) continue; // objeto anidado vacío: no se manda
        let destino = objeto;
        for (let i = 0; i < partes.length - 1; i++) {
            destino[partes[i]] = destino[partes[i]] || {};
            destino = destino[partes[i]];
        }
        destino[partes[partes.length - 1]] = valor;
    }
    return objeto;
}

// Lo contrario: pone los datos de un objeto adentro de los campos de un <form>
function llenarFormulario(form, objeto) {
    for (const campo of form.elements) {
        if (!campo.name) continue;
        let valor = campo.name.split('.').reduce((o, clave) => (o == null ? undefined : o[clave]), objeto);
        if (valor === undefined || valor === null) valor = '';
        if (campo.type === 'datetime-local') valor = String(valor).substring(0, 16);
        if (campo.type === 'time') valor = String(valor).substring(0, 5);
        campo.value = valor;
    }
}

// Llena un <select> con datos de la API. valor(d) = lo que se guarda, texto(d) = lo que se ve
async function llenarSelect(selector, url, valor, texto, primeraOpcion = 'Seleccione...') {
    const datos = await api(url);
    document.querySelector(selector).innerHTML = `<option value="">${esc(primeraOpcion)}</option>` +
        datos.map(d => `<option value="${esc(valor(d))}">${esc(texto(d))}</option>`).join('');
    return datos;
}

/* ---------------------------------------------------------------- fechas y estados */

function dos(n) { return String(n).padStart(2, '0'); }

// '2026-11-08' -> '08/11/2026'
function formatearFecha(iso) {
    if (!iso) return '';
    const [a, m, d] = String(iso).substring(0, 10).split('-');
    return `${d}/${m}/${a}`;
}

// '2026-11-08T14:30:00' -> '08/11/2026 14:30'
function formatearFechaHora(iso) {
    if (!iso) return '';
    return `${formatearFecha(iso)} ${String(iso).substring(11, 16)}`;
}

// Valores con el formato que piden los <input type="date | time | datetime-local">
function valorInputFecha(f) { return `${f.getFullYear()}-${dos(f.getMonth() + 1)}-${dos(f.getDate())}`; }
function valorInputHora(f) { return `${dos(f.getHours())}:${dos(f.getMinutes())}`; }
function valorInputFechaHora(f) { return `${valorInputFecha(f)}T${valorInputHora(f)}`; }

// Colores de Bootstrap para cada estado
const COLORES = {
    PREPARANDO: 'warning text-dark', EN_TRANSITO: 'primary', ENTREGADO: 'success', CANCELADO: 'danger',
    DISPONIBLE: 'success', EN_MANTENIMIENTO: 'warning text-dark', INACTIVO: 'secondary',
    ACTIVO: 'success', DE_LICENCIA: 'info text-dark', CONFORME: 'success', CON_OBSERVACIONES: 'warning text-dark'
};

// Devuelve el estado como una "etiqueta" de color (badge de Bootstrap)
function etiquetaEstado(estado) {
    if (!estado) return '';
    return `<span class="badge text-bg-${COLORES[estado] || 'secondary'}">${esc(estado.replace('_', ' '))}</span>`;
}

/* ---------------------------------------------------------------- sesión y menú */

// Qué roles ven cada opción del menú
const MENU = [
    { pagina: 'dashboard.html',   texto: 'Dashboard',   roles: ['ADMIN', 'OPERADOR', 'CONDUCTOR'] },
    { pagina: 'clientes.html',    texto: 'Clientes',    roles: ['ADMIN', 'OPERADOR'] },
    { pagina: 'vehiculos.html',   texto: 'Vehículos',   roles: ['ADMIN', 'OPERADOR'] },
    { pagina: 'conductores.html', texto: 'Conductores', roles: ['ADMIN'] },
    { pagina: 'pedidos.html',     texto: 'Pedidos',     roles: ['ADMIN', 'OPERADOR'] },
    { pagina: 'envios.html',      texto: 'Envíos',      roles: ['ADMIN', 'OPERADOR', 'CONDUCTOR'] },
    { pagina: 'seguimiento.html', texto: 'Seguimiento', roles: ['ADMIN', 'OPERADOR', 'CONDUCTOR'] },
    { pagina: 'entregas.html',    texto: 'Entregas',    roles: ['ADMIN', 'CONDUCTOR'] },
    { pagina: 'usuarios.html',    texto: 'Usuarios',    roles: ['ADMIN'] }
];

function dibujarMenu(usuario) {
    const paginaActual = window.location.pathname.split('/').pop();
    const opciones = MENU.filter(m => m.roles.includes(usuario.rol)).map(m =>
        `<li class="nav-item"><a class="nav-link ${m.pagina === paginaActual ? 'active' : ''}" href="${m.pagina}">${m.texto}</a></li>`).join('');
    document.getElementById('navbar').innerHTML = `
    <nav class="navbar navbar-expand-lg navbar-dark navbar-marca">
      <div class="container">
        <a class="navbar-brand fw-bold" href="dashboard.html">&#128666; Transporte y Logística</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#menuPrincipal"
                aria-controls="menuPrincipal" aria-expanded="false" aria-label="Mostrar menú">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="menuPrincipal">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0">${opciones}</ul>
          <span class="navbar-text me-3 small">${esc(usuario.nombre)} <span class="badge text-bg-light">${esc(usuario.rol)}</span></span>
          <button class="btn btn-outline-light btn-sm" id="btnSalir">Salir</button>
        </div>
      </div>
    </nav>`;
    document.getElementById('btnSalir').addEventListener('click', async () => {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' });
        window.location.href = 'login.html';
    });
}

/**
 * Se llama al comenzar cada pantalla: comprueba que haya una sesión iniciada,
 * dibuja el menú y verifica que el rol pueda ver la pantalla.
 * Devuelve los datos del usuario, o null si no tiene permiso.
 * (El control de permisos que vale es el del backend; esto solo mejora la experiencia.)
 */
async function iniciarPagina(rolesPermitidos) {
    const usuario = await api('/api/auth/me');
    dibujarMenu(usuario);
    if (!rolesPermitidos.includes(usuario.rol)) {
        document.querySelector('main').innerHTML =
            '<div class="alert alert-warning">No tiene permisos para ver esta pantalla. ' +
            '<a href="dashboard.html" class="alert-link">Volver al inicio</a></div>';
        return null;
    }
    return usuario;
}
