/* vehiculos.js - Pantalla de vehículos (ADMIN y OPERADOR)
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

// ---------------------------------------------------------------- 1) pedir los datos
async function cargarLista() {
    try {
        const texto = document.getElementById('buscador').value.trim();
        lista = await api('/api/vehiculos' + (texto ? '?q=' + encodeURIComponent(texto) : ''));
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
            <td><strong>${esc(item.patente)}</strong></td>
            <td>${esc(item.marca)} ${esc(item.modelo)}</td>
            <td>${esc(item.anio)}</td>
            <td>${esc(item.tipoVehiculo.nombre)}</td>
            <td>${esc(item.capacidad)}</td>
            <td>${etiquetaEstado(item.estado)}</td>
            <td class="text-end text-nowrap">
                <button class="btn btn-sm btn-outline-primary" data-accion="editar" data-id="${item.idVehiculo}">Editar</button>
                <button class="btn btn-sm btn-outline-danger" data-accion="baja" data-id="${item.idVehiculo}">Dar de baja</button>
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
    document.getElementById('modalTitulo').textContent = 'Nuevo vehículo';
    formulario.estado.value = 'DISPONIBLE';
    ventana.show();
}

function abrirEditar(id) {
    const item = lista.find(i => String(i.idVehiculo) === String(id));
    idEditando = id;
    formulario.reset();
    formulario.classList.remove('was-validated');
    document.getElementById('alertasModal').innerHTML = '';
    document.getElementById('modalTitulo').textContent = 'Editar vehículo';
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
            await api('/api/vehiculos', { metodo: 'POST', cuerpo: datos });
        } else {
            await api('/api/vehiculos/' + idEditando, { metodo: 'PUT', cuerpo: datos });
        }
        ventana.hide();
        mostrarAlerta('success', 'Vehículo guardado correctamente');
        cargarLista();
    } catch (e) {
        mostrarError(e, 'alertasModal');   // la validación del servidor (la que vale) se muestra en el formulario
    }
}

// ---------------------------------------------------------------- 5) dar de baja
async function darDeBaja(id) {
    if (!confirm('¿Confirma dar de baja este vehículo? Quedará inactivo.')) return;
    try {
        await api('/api/vehiculos/' + id, { metodo: 'DELETE' });
        mostrarAlerta('success', 'Vehículo dado de baja (queda inactivo)');
        cargarLista();
    } catch (e) {
        mostrarError(e);
    }
}

// ---------------------------------------------------------------- arranque de la pantalla
(async () => {
    const usuario = await iniciarPagina(['ADMIN', 'OPERADOR']);   // controla la sesión y dibuja el menú
    if (!usuario) return;
    // El selector de tipos se llena con los tipos que hay en la base de datos
    await llenarSelect('#f_tipoVehiculo_idTipo', '/api/tipos-vehiculo', t => t.idTipo, t => t.nombre, 'Seleccione un tipo...');

    document.getElementById('btnNuevo').addEventListener('click', abrirNuevo);
    formulario.addEventListener('submit', guardar);
    cuerpoTabla.addEventListener('click', alHacerClicEnLaTabla);
    document.getElementById('buscador').addEventListener('input', cargarLista);   // busca mientras se escribe
    cargarLista();
})();
