/* usuarios.js - Pantalla de usuarios (solo ADMIN)
 *
 * Cómo funciona esta pantalla (todas las pantallas con tabla se arman igual):
 *   1. cargarLista()  pide los datos a la API y llama a dibujarTabla().
 *   2. dibujarTabla() arma las filas de la tabla con los datos.
 *   3. abrirNuevo() / abrirEditar() abren la ventana (modal) con el formulario.
 *   4. guardar()      manda el formulario a la API (POST si es nuevo, PUT si se edita).
 *   5. darDeBaja()    llama a DELETE para dar de baja el registro.
 */

let lista = [];          // los datos que llegaron de la API
let idEditando = null;   // null = estamos creando uno nuevo; un número = estamos editando ese id

const cuerpoTabla = document.querySelector('#tabla tbody');
const formulario = document.getElementById('formulario');
const ventana = new bootstrap.Modal(document.getElementById('modalForm'));

const selectRol = document.getElementById('f_rol');
const grupoConductor = document.getElementById('grupoConductor');
const selectConductor = document.getElementById('f_conductor_idConductor');
const campoPassword = document.getElementById('f_password');

// El selector de conductor solo se ve (y es obligatorio) cuando el rol es CONDUCTOR
function actualizarConductor() {
    const esConductor = selectRol.value === 'CONDUCTOR';
    grupoConductor.classList.toggle('d-none', !esConductor);
    selectConductor.required = esConductor;
    if (!esConductor) selectConductor.value = '';
}

// ---------------------------------------------------------------- 1) pedir los datos
async function cargarLista() {
    try {
        lista = await api('/api/usuarios');
        dibujarTabla();
    } catch (e) {
        mostrarError(e);
    }
}

// ---------------------------------------------------------------- 2) dibujar la tabla
function dibujarTabla() {
    if (lista.length === 0) {
        cuerpoTabla.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">No se encontraron resultados</td></tr>';
        return;
    }
    cuerpoTabla.innerHTML = lista.map(item => `
        <tr>
            <td><strong>${esc(item.username)}</strong></td>
            <td>${esc(item.apellido)}, ${esc(item.nombre)}</td>
            <td><span class="badge text-bg-dark">${esc(item.rol)}</span></td>
            <td>${item.conductor ? esc(item.conductor.apellido) + ', ' + esc(item.conductor.nombre) : '-'}</td>
            <td>${etiquetaEstado(item.estado)}</td>
            <td class="text-end text-nowrap">
                <button class="btn btn-sm btn-outline-primary" data-accion="editar" data-id="${item.idUsuario}">Editar</button>
                <button class="btn btn-sm btn-outline-danger" data-accion="baja" data-id="${item.idUsuario}">Dar de baja</button>
            </td>
        </tr>`).join('');
}

// Cuando se hace clic en un botón de la tabla, se mira cuál fue (editar o baja) y de qué fila
function alHacerClicEnLaTabla(evento) {
    const boton = evento.target.closest('button[data-accion]');
    if (!boton) return;
    if (boton.dataset.accion === 'editar') abrirEditar(boton.dataset.id);
    if (boton.dataset.accion === 'baja') darDeBaja(boton.dataset.id);
}

// ---------------------------------------------------------------- 3) abrir el formulario
function abrirNuevo() {
    idEditando = null;
    formulario.reset();
    formulario.classList.remove('was-validated');
    document.getElementById('alertasModal').innerHTML = '';
    document.getElementById('modalTitulo').textContent = 'Nuevo usuario';
    formulario.estado.value = 'ACTIVO';
    campoPassword.required = true;             // al crear, la contraseña es obligatoria
    campoPassword.placeholder = '';
    actualizarConductor();
    ventana.show();
}

function abrirEditar(id) {
    const item = lista.find(i => String(i.idUsuario) === String(id));
    idEditando = id;
    formulario.reset();
    formulario.classList.remove('was-validated');
    document.getElementById('alertasModal').innerHTML = '';
    document.getElementById('modalTitulo').textContent = 'Editar usuario';
    llenarFormulario(formulario, item);   // pone los datos del registro en el formulario
    campoPassword.required = false;            // al editar, si se deja vacía se conserva la actual
    campoPassword.placeholder = 'Dejar vacío para no cambiarla';
    actualizarConductor();
    ventana.show();
}

// ---------------------------------------------------------------- 4) guardar
async function guardar(evento) {
    evento.preventDefault();
    formulario.classList.add('was-validated');
    if (!formulario.checkValidity()) return;   // validación del navegador (campos obligatorios, formatos...)

    const datos = leerFormulario(formulario);   // el formulario convertido en objeto
    try {
        if (idEditando === null) {
            await api('/api/usuarios', { metodo: 'POST', cuerpo: datos });
        } else {
            await api('/api/usuarios/' + idEditando, { metodo: 'PUT', cuerpo: datos });
        }
        ventana.hide();
        mostrarAlerta('success', 'Usuario guardado correctamente');
        cargarLista();
    } catch (e) {
        mostrarError(e, 'alertasModal');   // la validación del servidor (la que vale) se muestra en el formulario
    }
}

// ---------------------------------------------------------------- 5) dar de baja
async function darDeBaja(id) {
    if (!confirm('¿Confirma dar de baja este usuario? Ya no podrá iniciar sesión.')) return;
    try {
        await api('/api/usuarios/' + id, { metodo: 'DELETE' });
        mostrarAlerta('success', 'Usuario dado de baja (ya no puede iniciar sesión)');
        cargarLista();
    } catch (e) {
        mostrarError(e);
    }
}

// ---------------------------------------------------------------- arranque de la pantalla
(async () => {
    const usuario = await iniciarPagina(['ADMIN']);   // controla la sesión y dibuja el menú
    if (!usuario) return;
    await llenarSelect('#f_conductor_idConductor', '/api/conductores', c => c.idConductor,
        c => c.apellido + ', ' + c.nombre + ' (DNI ' + c.dni + ')', 'Seleccione un conductor...');
    selectRol.addEventListener('change', actualizarConductor);

    document.getElementById('btnNuevo').addEventListener('click', abrirNuevo);
    formulario.addEventListener('submit', guardar);
    cuerpoTabla.addEventListener('click', alHacerClicEnLaTabla);
    cargarLista();
})();
