/* pedidos.js - Pantalla de pedidos (ADMIN y OPERADOR)
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

let pendientes = new Set();   // ids de los pedidos que todavía no tienen envío

// ---------------------------------------------------------------- 1) pedir los datos
async function cargarLista() {
    try {
        lista = await api('/api/pedidos');
        const sinEnvio = await api('/api/pedidos?pendientes=true');
        pendientes = new Set(sinEnvio.map(p => p.idPedido));
        dibujarTabla();
    } catch (e) {
        mostrarError(e);
    }
}

// ---------------------------------------------------------------- 2) dibujar la tabla
function dibujarTabla() {
    if (lista.length === 0) {
        cuerpoTabla.innerHTML = '<tr><td colspan="9" class="text-center text-muted py-4">No se encontraron resultados</td></tr>';
        return;
    }
    cuerpoTabla.innerHTML = lista.map(item => `
        <tr>
            <td>#${item.idPedido}</td>
            <td>${esc(formatearFecha(item.fecha))}</td>
            <td><strong>${esc(item.cliente.razonSocial)}</strong></td>
            <td>${esc(item.origen)}<br><small class="text-muted">&rarr; ${esc(item.destino)}</small></td>
            <td>${esc(item.cantidad)} &times; ${esc(item.tipoCarga)}</td>
            <td>${esc(item.peso)}</td>
            <td>${esc(formatearFecha(item.fechaEstimadaEntrega))}</td>
            <td>${pendientes.has(item.idPedido) ? '<span class="badge text-bg-warning text-dark">Pendiente de envío</span>' : '<span class="badge text-bg-success">Con envío</span>'}</td>
            <td class="text-end text-nowrap">
                <button class="btn btn-sm btn-outline-primary" data-accion="editar" data-id="${item.idPedido}">Editar</button>
                <button class="btn btn-sm btn-outline-danger" data-accion="baja" data-id="${item.idPedido}">Eliminar</button>
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
    document.getElementById('modalTitulo').textContent = 'Nuevo pedido';
    formulario.fechaEstimadaEntrega.min = valorInputFecha(new Date());   // no se puede elegir una fecha pasada
    ventana.show();
}

function abrirEditar(id) {
    const item = lista.find(i => String(i.idPedido) === String(id));
    idEditando = id;
    formulario.reset();
    formulario.classList.remove('was-validated');
    document.getElementById('alertasModal').innerHTML = '';
    document.getElementById('modalTitulo').textContent = 'Editar pedido';
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
            await api('/api/pedidos', { metodo: 'POST', cuerpo: datos });
        } else {
            await api('/api/pedidos/' + idEditando, { metodo: 'PUT', cuerpo: datos });
        }
        ventana.hide();
        mostrarAlerta('success', 'Pedido guardado correctamente');
        cargarLista();
    } catch (e) {
        mostrarError(e, 'alertasModal');   // la validación del servidor (la que vale) se muestra en el formulario
    }
}

// ---------------------------------------------------------------- 5) dar de baja
async function darDeBaja(id) {
    if (!confirm('¿Confirma eliminar este pedido?')) return;
    try {
        await api('/api/pedidos/' + id, { metodo: 'DELETE' });
        mostrarAlerta('success', 'Pedido eliminado correctamente');
        cargarLista();
    } catch (e) {
        mostrarError(e);
    }
}

// ---------------------------------------------------------------- arranque de la pantalla
(async () => {
    const usuario = await iniciarPagina(['ADMIN', 'OPERADOR']);   // controla la sesión y dibuja el menú
    if (!usuario) return;
    // El selector de clientes muestra solo los clientes activos
    const clientes = await api('/api/clientes');
    document.getElementById('f_cliente_idCliente').innerHTML = '<option value="">Seleccione un cliente...</option>' +
        clientes.filter(c => c.estado === 'ACTIVO')
            .map(c => `<option value="${c.idCliente}">${esc(c.razonSocial)} (${esc(c.cuit)})</option>`).join('');

    document.getElementById('btnNuevo').addEventListener('click', abrirNuevo);
    formulario.addEventListener('submit', guardar);
    cuerpoTabla.addEventListener('click', alHacerClicEnLaTabla);
    cargarLista();
})();
