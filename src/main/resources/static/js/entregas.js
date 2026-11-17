/* entregas.js - Registrar la entrega de un envío en tránsito y consultar las entregas realizadas */
(async () => {
    if (!await iniciarPagina(['ADMIN', 'CONDUCTOR'])) return;

    const form = document.getElementById('formEntrega');
    const select = document.getElementById('f_idEnvio');
    const sinEnvios = document.getElementById('sinEnTransito');

    async function cargarEnvios(preseleccion) {
        const envios = await api('/api/envios?estado=EN_TRANSITO');
        select.innerHTML = '<option value="">Seleccione un envío...</option>' + envios.map(e =>
            `<option value="${e.idEnvio}">Envío #${e.idEnvio} - ${esc(e.pedido.cliente.razonSocial)}: ${esc(e.pedido.destino)}</option>`).join('');
        form.classList.toggle('d-none', envios.length === 0);
        sinEnvios.classList.toggle('d-none', envios.length > 0);
        if (preseleccion && envios.some(e => String(e.idEnvio) === String(preseleccion))) select.value = preseleccion;
    }

    async function cargarEntregas() {
        const entregas = await api('/api/entregas');
        document.querySelector('#tablaEntregas tbody').innerHTML = entregas.length
            ? entregas.map(en => `<tr>
                <td><a href="seguimiento.html?id=${en.envio.idEnvio}">#${en.envio.idEnvio}</a></td>
                <td>${esc(en.envio.pedido.cliente.razonSocial)}</td>
                <td>${esc(formatearFecha(en.fecha))}</td>
                <td>${esc(String(en.hora).substring(0, 5))}</td>
                <td>${esc(en.personaRecibe)}</td>
                <td>${esc(en.dniRecibe)}</td>
                <td>${etiquetaEstado(en.estadoEntrega)}</td>
                <td>${esc(en.observaciones)}</td></tr>`).join('')
            : '<tr><td colspan="8" class="text-center text-muted py-4">Todavía no hay entregas registradas</td></tr>';
    }

    function valoresPorDefecto() {
        const ahora = new Date();
        form.fecha.max = valorInputFecha(ahora);
        form.fecha.value = valorInputFecha(ahora);
        form.hora.value = valorInputHora(ahora);
        form.estadoEntrega.value = 'CONFORME';
    }

    form.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        form.classList.add('was-validated');
        if (!form.checkValidity()) return;
        const datos = leerFormulario(form);
        const idEnvio = datos.idEnvio;
        delete datos.idEnvio;
        try {
            await api(`/api/envios/${idEnvio}/entrega`, { metodo: 'POST', cuerpo: datos });
            form.reset();
            form.classList.remove('was-validated');
            document.getElementById('alertasEntrega').innerHTML = '';
            mostrarAlerta('success', `Entrega del envío #${idEnvio} registrada. El envío pasó a ENTREGADO`);
            await cargarEnvios();
            valoresPorDefecto();
            await cargarEntregas();
        } catch (e) { mostrarError(e, 'alertasEntrega'); }
    });

    try {
        await cargarEnvios(new URLSearchParams(window.location.search).get('envio'));
        valoresPorDefecto();
        await cargarEntregas();
    } catch (e) { mostrarError(e); }
})();
