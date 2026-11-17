/* seguimiento.js - Estado actual e historial de un envío (los 3 roles; el conductor solo ve los suyos) */
(async () => {
    if (!await iniciarPagina(['ADMIN', 'OPERADOR', 'CONDUCTOR'])) return;

    const select = document.getElementById('selEnvio');
    const detalle = document.getElementById('detalle');
    const aviso = document.getElementById('sinSeleccion');

    async function mostrar(id) {
        if (!id) {
            detalle.classList.add('d-none');
            aviso.classList.remove('d-none');
            return;
        }
        try {
            const [e, historial] = await Promise.all([api('/api/envios/' + id), api(`/api/envios/${id}/historial`)]);
            const p = e.pedido;
            document.getElementById('tituloEnvio').innerHTML = `Envío Nº ${e.idEnvio} ${etiquetaEstado(e.estado.nombre)}`;
            document.getElementById('datosEnvio').innerHTML = [
                ['Cliente', esc(p.cliente.razonSocial)],
                ['Origen', esc(p.origen)],
                ['Destino', esc(p.destino)],
                ['Carga', `${esc(p.cantidad)} × ${esc(p.tipoCarga)} (${esc(p.peso)} kg)`],
                ['Vehículo', e.vehiculo ? `${esc(e.vehiculo.patente)} - ${esc(e.vehiculo.marca)} ${esc(e.vehiculo.modelo)}` : '<span class="text-muted">Sin asignar</span>'],
                ['Conductor', e.conductor ? `${esc(e.conductor.apellido)}, ${esc(e.conductor.nombre)}` : '<span class="text-muted">Sin asignar</span>'],
                ['Salida', esc(formatearFechaHora(e.fechaSalida))],
                ['Llegada estimada', esc(formatearFechaHora(e.fechaEstimadaLlegada))],
                ['Itinerario', esc(e.itinerario)],
                ['Obs. de ruta', esc(e.observacionesRuta) || '-']
            ].map(([k, v]) => `<dt class="col-sm-4">${k}</dt><dd class="col-sm-8">${v}</dd>`).join('');

            document.getElementById('historial').innerHTML = historial.map(h => `
                <li class="estado-${esc(h.estado.nombre)}">
                    <div class="fw-semibold">${esc(formatearFechaHora(h.fecha))} - ${esc(h.estado.nombre.replace('_', ' '))}</div>
                    ${h.observaciones ? `<div>${esc(h.observaciones)}</div>` : ''}
                    <div class="small text-muted">Registrado por: ${esc(h.usuario)}</div>
                </li>`).join('');
            aviso.classList.add('d-none');
            detalle.classList.remove('d-none');
        } catch (err) { mostrarError(err); }
    }

    try {
        const envios = await api('/api/envios');
        select.innerHTML = '<option value="">Seleccione...</option>' + envios.map(e =>
            `<option value="${e.idEnvio}">Envío Nº ${e.idEnvio} - ${esc(e.pedido.cliente.razonSocial)} (${esc(e.estado.nombre.replace('_', ' '))})</option>`).join('');
        const idUrl = new URLSearchParams(window.location.search).get('id');
        if (idUrl && envios.some(e => String(e.idEnvio) === idUrl)) {
            select.value = idUrl;
            mostrar(idUrl);
        }
    } catch (e) { mostrarError(e); }

    select.addEventListener('change', () => mostrar(select.value));
})();
