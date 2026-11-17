/* conductores.js - Pantalla de conductores (solo ADMIN)
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

const hoy = valorInputFecha(new Date());   // fecha de hoy, para marcar las licencias vencidas

// ---------------------------------------------------------------- 1) pedir los datos
async function cargarLista() {
    try {
        const texto = document.getElementById('buscador').value.trim();
        lista = await api('/api/conductores' + (texto ? '?q=' + encodeURIComponent(texto) : ''));
        dibujarTabla();
    } catch (e) {
        mostrarError(e);
    }
}

// ---------------------------------------------------------------- 2) dibujar la tabla
function dibujarTabla() {
    if (lista.length === 0) {
        cuerpoTabla.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No se encontraron resultados</td></tr>';
        return;
    }
    cuerpoTabla.innerHTML = lista.map(item => `
        <tr>
            <td>${esc(item.dni)}</td>
            <td><strong>${esc(item.apellido)}</strong>, ${esc(item.nombre)}</td>
            <td>${esc(item.licencia)}</td>
            <td>${esc(formatearFecha(item.vencimientoLicencia))} ${item.vencimientoLicencia < hoy ? '<span class="badge text-bg-danger">Vencida</span>' : ''}</td>
            <td>${esc(item.telefono)}</td>
            <td>${etiquetaEstado(item.estado)}</td>
            <td class="text-end text-nowrap">
                <button class="btn btn-sm btn-outline-primary" data-accion="editar" data-id="${item.idConductor}">Editar</button>
                <button class="btn btn-sm btn-outline-danger" data-accion="baja" data-id="${item.idConductor}">Dar de baja</button>
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
    document.getElementById('modalTitulo').textContent = 'Nuevo conductor';
    formulario.estado.value = 'ACTIVO';
    ventana.show();
}

function abrirEditar(id) {
    const item = lista.find(i => String(i.idConductor) === String(id));
    idEditando = id;
    formulario.reset();
    formulario.classList.remove('was-validated');
    document.getElementById('alertasModal').innerHTML = '';
    document.getElementById('modalTitulo').textContent = 'Editar conductor';
    llenarFormulario(formulario, item);   // pone los datos del registro en el formulario
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
            await api('/api/conductores', { metodo: 'POST', cuerpo: datos });
        } else {
            await api('/api/conductores/' + idEditando, { metodo: 'PUT', cuerpo: datos });
        }
        ventana.hide();
        mostrarAlerta('success', 'Conductor guardado correctamente');
        cargarLista();
    } catch (e) {
        mostrarError(e, 'alertasModal');   // la validación del servidor (la que vale) se muestra en el formulario
    }
}

// ---------------------------------------------------------------- 5) dar de baja
async function darDeBaja(id) {
    if (!confirm('¿Confirma dar de baja a este conductor? Quedará inactivo.')) return;
    try {
        await api('/api/conductores/' + id, { metodo: 'DELETE' });
        mostrarAlerta('success', 'Conductor dado de baja (queda inactivo)');
        cargarLista();
    } catch (e) {
        mostrarError(e);
    }
}

// ---------------------------------------------------------------- arranque de la pantalla
(async () => {
    const usuario = await iniciarPagina(['ADMIN']);   // controla la sesión y dibuja el menú
    if (!usuario) return;

    document.getElementById('btnNuevo').addEventListener('click', abrirNuevo);
    formulario.addEventListener('submit', guardar);
    cuerpoTabla.addEventListener('click', alHacerClicEnLaTabla);
    document.getElementById('buscador').addEventListener('input', cargarLista);   // busca mientras se escribe
    cargarLista();
})();
