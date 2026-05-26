<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="Red de antiguos alumnos de la Universidad Loyola Andalucia: busqueda de perfiles, eventos y actividades.">
  <meta name="theme-color" content="#001f5b">
  <title>Loyola Alumni</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/styles.css">
  <!-- PWA: manifiesto e iconos (RNF-1) -->
  <link rel="manifest" href="<%= request.getContextPath() %>/manifest.json">
  <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/assets/icons/favicon-64.png">
  <link rel="apple-touch-icon" href="<%= request.getContextPath() %>/assets/icons/icon-192.png">
</head>
<body>
  <!-- Accesibilidad WCAG 2.1 AA: enlace para saltar la navegacion (RNF-6) -->
  <a class="skip-link" href="#contenidoPrincipal">Saltar al contenido principal</a>
  <div id="toast" class="toast" role="status" aria-live="polite"></div>

  <section id="authView" class="auth-shell">
    <div class="auth-hero">
      <img src="<%= request.getContextPath() %>/assets/logo-loyola.png" alt="Universidad Loyola" class="hero-logo">
      <div class="hero-copy">
        <p class="eyebrow">Comunidad Alumni</p>
        <h1>Conecta con la red profesional de la Universidad Loyola.</h1>
        <p>
          Encuentra antiguos alumnos, actualiza tu perfil, participa en eventos y
          mantente cerca de tu universidad desde un unico espacio.
        </p>
      </div>
    </div>

    <aside class="login-card" aria-label="Inicio de sesion">
      <div class="login-header">
        <h2>Entrar</h2>
        <p>Usa tus credenciales de la comunidad Loyola.</p>
      </div>

      <form id="loginForm" class="form-stack">
        <label>Usuario
          <input name="usuario" autocomplete="username" value="mgarcia" required>
        </label>
        <label>Contraseña
          <input name="contrasenia" type="password" autocomplete="current-password" value="Loyola2026!" required>
        </label>
        <button class="primary" type="submit">Iniciar sesion</button>
      </form>

      <div class="demo-access" aria-label="Accesos de prueba">
        <span>Accesos disponibles (contrasena: Loyola2026!)</span>
        <button type="button" data-user="mgarcia" data-pass="Loyola2026!">Alumni</button>
        <button type="button" data-user="afernandez" data-pass="Loyola2026!">PDI</button>
        <button type="button" data-user="lmarin" data-pass="Loyola2026!">PTGAS</button>
        <button type="button" data-user="phidalgo" data-pass="Loyola2026!">Admin</button>
      </div>
    </aside>
  </section>

  <section id="appView" class="app-shell" hidden>
    <header class="topbar">
      <button type="button" class="mobile-menu" id="menuButton" aria-label="Abrir menu">☰</button>
      <img src="<%= request.getContextPath() %>/assets/logo-loyola.png" alt="Universidad Loyola" class="top-logo">
      <nav id="mainNav" class="main-nav" aria-label="Navegacion principal">
        <button type="button" class="nav-item active" data-view="home">Inicio</button>
        <button type="button" class="nav-item" data-view="directory">Directorio</button>
        <button type="button" class="nav-item" data-view="events">Eventos</button>
        <button type="button" class="nav-item" data-view="profile">Mi perfil</button>
        <button type="button" class="nav-item admin-only" data-view="admin">Admin</button>
      </nav>
      <div class="user-chip">
        <span id="userInitials">AL</span>
        <div>
          <strong id="userName">Usuario</strong>
          <small id="userRole">ALUMNI</small>
        </div>
      </div>
      <button type="button" class="ghost" id="logoutButton">Salir</button>
    </header>

    <main class="main-layout" id="contenidoPrincipal" tabindex="-1">
      <section class="view active-view" id="homeView">
        <div class="welcome-band">
          <div>
            <p class="eyebrow">Bienvenido</p>
            <h2 id="welcomeTitle">Tu espacio Alumni</h2>
            <p id="welcomeText">
              Explora la comunidad, encuentra contactos y revisa los proximos eventos.
            </p>
          </div>
          <div class="stats-grid">
            <article>
              <strong id="alumniCount">0</strong>
              <span>Alumni visibles</span>
            </article>
            <article>
              <strong id="eventCount">0</strong>
              <span>Eventos activos</span>
            </article>
            <article>
              <strong id="roleBadge">ALUMNI</strong>
              <span>Rol actual</span>
            </article>
          </div>
        </div>

        <div class="content-grid">
          <article class="panel">
            <div class="panel-title">
              <div>
                <p class="eyebrow">Directorio</p>
                <h3>Alumni destacados</h3>
              </div>
              <button type="button" class="link-button" data-open-view="directory">Ver todos</button>
            </div>
            <div id="homeAlumniList" class="mini-list"></div>
          </article>

          <article class="panel">
            <div class="panel-title">
              <div>
                <p class="eyebrow">Agenda</p>
                <h3>Proximos eventos</h3>
              </div>
              <button type="button" class="link-button" data-open-view="events">Ver agenda</button>
            </div>
            <div id="homeEventList" class="mini-list"></div>
          </article>
        </div>
      </section>

      <section class="view" id="directoryView">
        <div class="section-head">
          <div>
            <p class="eyebrow">Red Alumni</p>
            <h2>Directorio profesional</h2>
            <p>Busca por nombre, promocion, campus, facultad, ciudad o intereses.</p>
          </div>
        </div>

        <form id="searchForm" class="filter-bar">
          <input name="texto" placeholder="Buscar alumni, titulacion, empresa...">
          <select name="facultad">
            <option value="">Facultad</option>
            <option>INGENIERIA</option>
            <option>DERECHO</option>
            <option>ADE</option>
            <option>ECONOMICAS</option>
            <option>MEDICINA</option>
            <option>BIOLOGIA</option>
            <option>HUMANISMO</option>
            <option>MATEMATICAS</option>
          </select>
          <select name="campus">
            <option value="">Campus</option>
            <option>SEVILLA</option>
            <option>CORDOBA</option>
            <option>GRANADA</option>
          </select>
          <input name="promocion" type="number" placeholder="Promocion">
          <button class="primary" type="submit">Buscar</button>
        </form>

        <div id="directoryResults" class="card-grid"></div>
      </section>

      <section class="view" id="alumniProfileView">
        <button type="button" class="ghost back-button" id="backToDirectory">Volver al directorio</button>
        <div id="alumniProfileDetail"></div>
      </section>

      <section class="view" id="eventsView">
        <div class="section-head split-head">
          <div>
            <p class="eyebrow">Agenda Alumni</p>
            <h2>Eventos y encuentros</h2>
            <p>Inscribete en actividades o publica nuevas propuestas para la comunidad.</p>
          </div>
          <button type="button" class="primary" id="toggleEventForm">Proponer evento</button>
        </div>

        <form id="eventForm" class="panel event-form" hidden>
          <input name="id" type="hidden">
          <div class="panel-title">
            <div>
              <p class="eyebrow">Nueva propuesta</p>
              <h3 id="eventFormTitle">Crear evento</h3>
            </div>
          </div>
          <div class="two-cols">
            <label>Nombre
              <input name="nombre" required>
            </label>
            <label>Lugar
              <input name="lugar" required>
            </label>
          </div>
          <label>Descripcion
            <textarea name="descripcion" rows="3" required></textarea>
          </label>
          <div class="three-cols">
            <label>Apertura
              <input name="fechaApertura" type="date" required>
            </label>
            <label>Limite
              <input name="fechaLimite" type="date" required>
            </label>
            <label>Fecha
              <input name="fechaEvento" type="date" required>
            </label>
          </div>
          <div class="two-cols">
            <label>Ponente
              <input name="ponente" required>
            </label>
            <label>Capacidad maxima
              <input name="capacidadMaxima" type="number" min="1" placeholder="Ej. 80">
            </label>
          </div>
          <button class="primary" type="submit">Guardar evento</button>
        </form>

        <div id="eventResults" class="event-list"></div>
      </section>

      <section class="view" id="profileView">
        <div class="section-head">
          <div>
            <p class="eyebrow">Perfil personal</p>
            <h2>Actualiza tu informacion</h2>
            <p>Tu perfil ayuda a otros alumni a encontrarte y contactar contigo.</p>
          </div>
        </div>

        <div id="profileNotice" class="empty-state" hidden>
          El perfil editable esta disponible para usuarios Alumni.
        </div>

        <form id="profileForm" class="panel profile-form">
          <div class="two-cols">
            <label>Nombre
              <input name="nombre">
            </label>
            <label>Apellidos
              <input name="apellidos">
            </label>
          </div>
          <div class="two-cols">
            <label>Email
              <input name="email" type="email">
            </label>
            <label>Telefono
              <input name="telefono" type="number">
            </label>
          </div>
          <div class="three-cols">
            <label>Titulacion
              <input name="titulacion">
            </label>
            <label>Promocion
              <input name="promocion" type="number">
            </label>
            <label>Ciudad
              <input name="ciudad">
            </label>
          </div>
          <div class="two-cols">
            <label>Campus
              <select name="campus">
                <option>SEVILLA</option>
                <option>CORDOBA</option>
                <option>GRANADA</option>
              </select>
            </label>
            <label>Facultad
              <select name="facultad">
                <option>INGENIERIA</option>
                <option>DERECHO</option>
                <option>ADE</option>
                <option>ECONOMICAS</option>
                <option>MEDICINA</option>
                <option>BIOLOGIA</option>
                <option>HUMANISMO</option>
                <option>MATEMATICAS</option>
              </select>
            </label>
          </div>
          <label>Trabajo actual
            <input name="trabajoDescripcion">
          </label>
          <label>Hobbies e intereses
            <input name="hobbies">
          </label>
          <button class="primary" type="submit">Guardar cambios</button>
        </form>
      </section>

      <section class="view" id="adminView">
        <div class="section-head split-head">
          <div>
            <p class="eyebrow">Administracion</p>
            <h2>Gestion de usuarios</h2>
            <p>Crea, modifica y elimina cuentas de la comunidad Alumni.</p>
          </div>
          <button type="button" class="primary" id="refreshUsers">Actualizar</button>
        </div>

        <form id="adminUserForm" class="panel profile-form">
          <div class="panel-title">
            <div>
              <p class="eyebrow">RF-6</p>
              <h3 id="adminFormTitle">Crear usuario</h3>
            </div>
            <button type="button" class="ghost" id="resetAdminForm">Limpiar</button>
          </div>
          <div class="three-cols">
            <label>Rol
              <select name="rol">
                <option>ALUMNI</option>
                <option>PDI</option>
                <option>PTGAS</option>
                <option>ADMIN</option>
              </select>
            </label>
            <label>Usuario
              <input name="usuario" required>
            </label>
            <label>DNI
              <input name="dni">
            </label>
          </div>
          <div class="three-cols">
            <label>Contraseña
              <input name="contrasenia" type="password" placeholder="Se conserva si se deja vacia">
            </label>
            <label>Nombre
              <input name="nombre" required>
            </label>
            <label>Apellidos
              <input name="apellidos" required>
            </label>
          </div>
          <div class="two-cols">
            <label>Email
              <input name="email" type="email" required>
            </label>
            <label>Telefono
              <input name="telefono" type="number">
            </label>
          </div>
          <div class="three-cols">
            <label>Titulacion
              <input name="titulacion">
            </label>
            <label>Promocion
              <input name="promocion" type="number">
            </label>
            <label>Ciudad
              <input name="ciudad">
            </label>
          </div>
          <div class="two-cols">
            <label>Campus
              <select name="campus">
                <option>SEVILLA</option>
                <option>CORDOBA</option>
                <option>GRANADA</option>
              </select>
            </label>
            <label>Facultad
              <select name="facultad">
                <option>INGENIERIA</option>
                <option>DERECHO</option>
                <option>ADE</option>
                <option>ECONOMICAS</option>
                <option>MEDICINA</option>
                <option>BIOLOGIA</option>
                <option>HUMANISMO</option>
                <option>MATEMATICAS</option>
              </select>
            </label>
          </div>
          <div class="two-cols">
            <label>Trabajo / puesto
              <input name="trabajoDescripcion">
            </label>
            <label>Hobbies, proyectos o permisos
              <input name="hobbies" placeholder="Separados por comas si aplica">
            </label>
          </div>
          <button class="primary" type="submit">Guardar usuario</button>
        </form>

        <div id="adminUsers" class="user-table"></div>
      </section>
    </main>
  </section>

  <script>
    const API_BASE = '<%= request.getContextPath() %>';
    const state = {
      session: null,
      alumni: [],
      events: [],
      users: []
    };

    const authView = document.getElementById('authView');
    const appView = document.getElementById('appView');
    const toast = document.getElementById('toast');

    function showToast(message, type) {
      toast.textContent = message;
      toast.className = 'toast show ' + (type || 'info');
      window.clearTimeout(showToast.timer);
      showToast.timer = window.setTimeout(() => toast.className = 'toast', 3200);
    }

    async function api(path, options) {
      const response = await fetch(API_BASE + path, {
        headers: { 'Content-Type': 'application/json' },
        ...(options || {})
      });
      const payload = await response.json();
      if (!response.ok || payload.success === false) {
        throw new Error(payload.error || 'No se pudo completar la accion.');
      }
      return payload;
    }

    function objectFromForm(form) {
      return Object.fromEntries(new FormData(form).entries());
    }

    function clean(object) {
      const result = {};
      Object.keys(object).forEach((key) => {
        if (object[key] !== null && object[key] !== undefined && object[key] !== '') {
          result[key] = object[key];
        }
      });
      return result;
    }

    function initials(name, surname) {
      return ((name || 'A').charAt(0) + (surname || 'L').charAt(0)).toUpperCase();
    }

    function avatarMarkup(alumni, extraClass) {
      const classes = extraClass ? 'avatar-image ' + extraClass : 'avatar-image';
      const fallbackClass = extraClass ? 'avatar ' + extraClass : 'avatar';
      const fullName = ((alumni.nombre || '') + ' ' + (alumni.apellidos || '')).trim() || 'Alumni';
      if (alumni.fotoPerfil) {
        return `<img class="${escapeHtml(classes)}" src="${escapeHtml(API_BASE + alumni.fotoPerfil)}" alt="Foto de ${escapeHtml(fullName)}" loading="lazy">`;
      }
      return `<div class="${escapeHtml(fallbackClass)}">${initials(alumni.nombre, alumni.apellidos)}</div>`;
    }

    function activeUserName() {
      if (!state.session) {
        return '';
      }
      return state.session.nombre + ' ' + state.session.apellidos;
    }

    function setView(name) {
      document.querySelectorAll('.view').forEach((view) => view.classList.remove('active-view'));
      document.getElementById(name + 'View').classList.add('active-view');
      document.querySelectorAll('.nav-item').forEach((item) => {
        item.classList.toggle('active', item.dataset.view === name);
      });
      document.getElementById('mainNav').classList.remove('open');
    }

    async function loadInitialData() {
      await Promise.all([loadDirectory(), loadEvents()]);
      if (state.session.rol === 'ADMIN') {
        await loadUsers();
      }
      renderHome();
    }

    async function loadDirectory(params) {
      const query = params ? '?' + new URLSearchParams(clean(params)).toString() : '';
      const payload = await api('/BuscarAlumniServlet' + query);
      state.alumni = payload.resultados || [];
      renderDirectory();
      return state.alumni;
    }

    async function loadEvents() {
      const payload = await api('/EventoServlet');
      state.events = payload.eventos || [];
      renderEvents();
      return state.events;
    }

    async function loadUsers() {
      const payload = await api('/UsuarioAdminServlet');
      state.users = payload.usuarios || [];
      renderUsers();
    }

    function renderShell() {
      const userInitials = document.getElementById('userInitials');
      const sessionProfile = state.session.perfilAlumni || {};
      userInitials.textContent = initials(state.session.nombre, state.session.apellidos);
      userInitials.classList.remove('has-photo');
      userInitials.style.backgroundImage = '';
      if (sessionProfile.fotoPerfil) {
        userInitials.textContent = '';
        userInitials.classList.add('has-photo');
        userInitials.style.backgroundImage = 'url("' + API_BASE + sessionProfile.fotoPerfil + '")';
      }
      document.getElementById('userName').textContent = activeUserName();
      document.getElementById('userRole').textContent = state.session.rol;
      document.getElementById('roleBadge').textContent = state.session.rol;
      document.getElementById('welcomeTitle').textContent = 'Hola, ' + state.session.nombre;
      document.getElementById('welcomeText').textContent = messageForRole(state.session.rol);
      document.getElementById('toggleEventForm').textContent = canPublishEvents() ? 'Publicar evento' : 'Proponer evento';
      document.getElementById('eventFormTitle').textContent = canPublishEvents() ? 'Publicar evento' : 'Proponer evento';
      document.querySelectorAll('.admin-only').forEach((node) => {
        node.hidden = state.session.rol !== 'ADMIN';
      });
      fillProfile();
    }

    function messageForRole(role) {
      if (role === 'ADMIN') {
        return 'Gestiona usuarios y eventos de la comunidad alumni desde un entorno centralizado.';
      }
      if (role === 'PDI' || role === 'PTGAS') {
        return 'Consulta la red alumni y publica encuentros que acerquen universidad, empresas y egresados.';
      }
      return 'Actualiza tu perfil, encuentra companeros y apuntate a actividades de la comunidad.';
    }

    function renderHome() {
      document.getElementById('alumniCount').textContent = state.alumni.length;
      document.getElementById('eventCount').textContent = state.events.length;
      document.getElementById('homeAlumniList').innerHTML = state.alumni.slice(0, 3).map(alumniMini).join('') || emptyState('No hay alumni visibles.');
      document.getElementById('homeEventList').innerHTML = state.events.slice(0, 3).map(eventMini).join('') || emptyState('No hay eventos publicados.');
    }

    function renderDirectory() {
      const container = document.getElementById('directoryResults');
      container.innerHTML = state.alumni.map(alumniCard).join('') || emptyState('No se encontraron alumni con esos filtros.');
      container.querySelectorAll('[data-view-profile]').forEach((button) => {
        button.addEventListener('click', () => viewAlumniProfile(button.dataset.viewProfile));
      });
      renderHome();
    }

    function renderEvents() {
      const container = document.getElementById('eventResults');
      container.innerHTML = state.events.map(eventCard).join('') || emptyState('Todavia no hay eventos disponibles.');
      container.querySelectorAll('[data-enroll-event]').forEach((button) => {
        button.addEventListener('click', () => enrollEvent(Number(button.dataset.enrollEvent)));
      });
      container.querySelectorAll('[data-edit-event]').forEach((button) => {
        button.addEventListener('click', () => editEvent(Number(button.dataset.editEvent)));
      });
      container.querySelectorAll('[data-delete-event]').forEach((button) => {
        button.addEventListener('click', () => deleteEvent(Number(button.dataset.deleteEvent)));
      });
      renderHome();
    }

    function renderUsers() {
      const container = document.getElementById('adminUsers');
      if (!state.users.length) {
        container.innerHTML = emptyState('No hay usuarios registrados.');
        return;
      }
      container.innerHTML = state.users.map((user) => `
        <article class="user-row">
          <div class="avatar">${initials(user.nombre, user.apellidos)}</div>
          <div>
            <strong>${escapeHtml(user.nombre)} ${escapeHtml(user.apellidos)}</strong>
            <span>${escapeHtml(user.email || '')}</span>
          </div>
          <span class="role-pill">${escapeHtml(user.rol)}</span>
          <button type="button" class="ghost small" data-edit-user="${escapeHtml(user.usuario)}">Editar</button>
          <button type="button" class="danger small" data-delete-user="${escapeHtml(user.usuario)}" ${user.usuario === state.session.usuario ? 'disabled' : ''}>Eliminar</button>
        </article>
      `).join('');
      container.querySelectorAll('[data-edit-user]').forEach((button) => {
        button.addEventListener('click', () => editUser(button.dataset.editUser));
      });
      container.querySelectorAll('[data-delete-user]').forEach((button) => {
        button.addEventListener('click', () => deleteUser(button.dataset.deleteUser));
      });
    }

    function fillProfile() {
      const form = document.getElementById('profileForm');
      const notice = document.getElementById('profileNotice');
      const profile = state.session.perfilAlumni;
      if (!profile) {
        notice.hidden = false;
        form.hidden = true;
        return;
      }
      notice.hidden = true;
      form.hidden = false;
      form.nombre.value = profile.nombre || '';
      form.apellidos.value = profile.apellidos || '';
      form.email.value = profile.email || '';
      form.telefono.value = profile.telefono || '';
      form.titulacion.value = profile.titulacion || '';
      form.promocion.value = profile.promocion || '';
      form.ciudad.value = profile.ciudad || '';
      form.campus.value = profile.campus || 'SEVILLA';
      form.facultad.value = profile.facultad || 'INGENIERIA';
      form.trabajoDescripcion.value = profile.trabajo ? (profile.trabajo.descripcion || '') : '';
      form.hobbies.value = profile.hobbies || '';
    }

    function alumniCard(alumni) {
      const trabajo = alumni.trabajo || {};
      return `
        <article class="person-card">
          <div class="person-top">
            ${avatarMarkup(alumni, '')}
            <div>
              <h3>${escapeHtml(alumni.nombre)} ${escapeHtml(alumni.apellidos)}</h3>
              <p>${escapeHtml(alumni.titulacion || 'Alumni Loyola')}</p>
            </div>
          </div>
          <dl>
            <div><dt>Promocion</dt><dd>${escapeHtml(alumni.promocion || '-')}</dd></div>
            <div><dt>Campus</dt><dd>${escapeHtml(alumni.campus || '-')}</dd></div>
            <div><dt>Ciudad</dt><dd>${escapeHtml(alumni.ciudad || '-')}</dd></div>
            <div><dt>Trabajo</dt><dd>${escapeHtml(trabajo.descripcion || '-')}</dd></div>
          </dl>
          <p class="interests">${escapeHtml(alumni.hobbies || 'Sin intereses publicados')}</p>
          <div class="person-actions">
            <button type="button" class="primary" data-view-profile="${escapeHtml(alumni.usuario)}">Ver perfil</button>
            <a class="email-link" href="mailto:${escapeHtml(alumni.email || '')}">${escapeHtml(alumni.email || '')}</a>
          </div>
        </article>
      `;
    }

    function renderAlumniProfile(alumni) {
      const trabajo = alumni.trabajo || {};
      const contactoVisible = alumni.mostrarContacto !== false;
      document.getElementById('alumniProfileDetail').innerHTML = `
        <article class="profile-hero-card">
          <div class="profile-hero-main">
            ${avatarMarkup(alumni, 'profile-avatar')}
            <div>
              <p class="eyebrow">Perfil Alumni</p>
              <h2>${escapeHtml(alumni.nombre)} ${escapeHtml(alumni.apellidos)}</h2>
              <p>${escapeHtml(alumni.titulacion || 'Alumni Universidad Loyola')}</p>
              <div class="event-meta">
                <span>${escapeHtml(alumni.campus || 'Campus no indicado')}</span>
                <span>Promocion ${escapeHtml(alumni.promocion || alumni.anioGraduacion || '-')}</span>
                <span>${escapeHtml(alumni.ciudadResidencia || alumni.ciudad || 'Ciudad no indicada')}</span>
              </div>
            </div>
          </div>
          <div class="profile-contact">
            ${contactoVisible ? `<a class="primary contact-button" href="mailto:${escapeHtml(alumni.email || '')}">Contactar</a>` : '<span class="role-pill">Contacto privado</span>'}
            <span>${contactoVisible ? escapeHtml(alumni.email || '') : 'Este alumni no muestra su contacto.'}</span>
          </div>
        </article>

        <div class="profile-grid">
          <article class="panel">
            <p class="eyebrow">Formacion</p>
            <dl class="detail-list">
              <div><dt>Titulacion</dt><dd>${escapeHtml(alumni.titulacion || '-')}</dd></div>
              <div><dt>Facultad</dt><dd>${escapeHtml(alumni.facultad || '-')}</dd></div>
              <div><dt>Campus</dt><dd>${escapeHtml(alumni.campus || '-')}</dd></div>
              <div><dt>Anio graduacion</dt><dd>${escapeHtml(alumni.anioGraduacion || alumni.promocion || '-')}</dd></div>
            </dl>
          </article>

          <article class="panel">
            <p class="eyebrow">Experiencia</p>
            <dl class="detail-list">
              <div><dt>Puesto actual</dt><dd>${escapeHtml(alumni.trabajoActual || trabajo.descripcion || '-')}</dd></div>
              <div><dt>Empresa / lugar</dt><dd>${escapeHtml(trabajo.lugar || '-')}</dd></div>
              <div><dt>Ciudad trabajo</dt><dd>${escapeHtml(trabajo.ciudad || '-')}</dd></div>
              <div><dt>Inicio</dt><dd>${escapeHtml(trabajo.fechaInicio || '-')}</dd></div>
            </dl>
          </article>

          <article class="panel wide-panel">
            <p class="eyebrow">Intereses y comunidad</p>
            <p class="profile-text">${escapeHtml(alumni.hobbies || 'Este alumni aun no ha publicado intereses.')}</p>
          </article>
        </div>
      `;
    }

    function eventCard(evento) {
      const inscritos = evento.inscritos ? evento.inscritos.length : 0;
      const canEnroll = state.session && state.session.rol === 'ALUMNI';
      const canEdit = canPublishEvents();
      const canDelete = state.session && state.session.rol === 'ADMIN';
      return `
        <article class="event-card">
          <div>
            <p class="event-date">${formatDate(evento.fechaEvento)}</p>
            <h3>${escapeHtml(evento.nombre)}</h3>
            <p>${escapeHtml(evento.descripcion || '')}</p>
          </div>
          <div class="event-meta">
            <span>${escapeHtml(evento.lugar || 'Lugar por confirmar')}</span>
            <span>${inscritos} inscritos</span>
          </div>
          <div class="event-actions">
            ${canEnroll ? `<button type="button" class="primary" data-enroll-event="${evento.id}">Inscribirme</button>` : ''}
            ${canEdit ? `<button type="button" class="ghost" data-edit-event="${evento.id}">Modificar</button>` : ''}
            ${canDelete ? `<button type="button" class="danger" data-delete-event="${evento.id}">Eliminar</button>` : ''}
          </div>
        </article>
      `;
    }

    function alumniMini(alumni) {
      return `
        <div class="mini-row">
          ${avatarMarkup(alumni, 'small-avatar')}
          <div>
            <strong>${escapeHtml(alumni.nombre)} ${escapeHtml(alumni.apellidos)}</strong>
            <span>${escapeHtml(alumni.titulacion || '')} - ${escapeHtml(alumni.ciudad || '')}</span>
          </div>
        </div>
      `;
    }

    function eventMini(evento) {
      return `
        <div class="mini-row">
          <div class="date-box">${day(evento.fechaEvento)}<small>${month(evento.fechaEvento)}</small></div>
          <div>
            <strong>${escapeHtml(evento.nombre)}</strong>
            <span>${escapeHtml(evento.lugar || '')}</span>
          </div>
        </div>
      `;
    }

    function emptyState(text) {
      return `<div class="empty-state">${escapeHtml(text)}</div>`;
    }

    async function enrollEvent(id) {
      try {
        await api('/InscripcionServlet', {
          method: 'POST',
          body: JSON.stringify({ usuario: state.session.usuario, tipo: 'evento', id: id })
        });
        showToast('Inscripcion realizada correctamente.', 'success');
        await loadEvents();
      } catch (error) {
        showToast(error.message, 'error');
      }
    }

    async function deleteUser(usuario) {
      if (!window.confirm('Eliminar la cuenta de ' + usuario + '?')) {
        return;
      }
      try {
        await api('/UsuarioAdminServlet', {
          method: 'DELETE',
          body: JSON.stringify({ adminUsuario: state.session.usuario, usuario: usuario })
        });
        showToast('Usuario eliminado.', 'success');
        await Promise.all([loadUsers(), loadDirectory()]);
      } catch (error) {
        showToast(error.message, 'error');
      }
    }

    async function viewAlumniProfile(usuario) {
      try {
        const response = await api('/PerfilServlet?usuario=' + encodeURIComponent(usuario));
        renderAlumniProfile(response.perfil);
        setView('alumniProfile');
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } catch (error) {
        showToast(error.message, 'error');
      }
    }

    function editUser(usuario) {
      const user = state.users.find((item) => item.usuario === usuario);
      if (!user) {
        return;
      }
      const form = document.getElementById('adminUserForm');
      document.getElementById('adminFormTitle').textContent = 'Modificar usuario';
      form.rol.value = user.rol || 'ALUMNI';
      form.usuario.value = user.usuario || '';
      form.usuario.readOnly = true;
      form.dni.value = user.dni || '';
      form.contrasenia.value = '';
      form.nombre.value = user.nombre || '';
      form.apellidos.value = user.apellidos || '';
      form.email.value = user.email || '';
      form.telefono.value = user.telefono || '';
      const perfil = user.perfilAlumni || {};
      form.titulacion.value = perfil.titulacion || user.titulacion || '';
      form.promocion.value = perfil.promocion || '';
      form.ciudad.value = perfil.ciudad || '';
      form.campus.value = perfil.campus || user.campus || 'SEVILLA';
      form.facultad.value = perfil.facultad || user.facultad || 'INGENIERIA';
      form.trabajoDescripcion.value = perfil.trabajo ? (perfil.trabajo.descripcion || '') : '';
      form.hobbies.value = perfil.hobbies || (user.proyectos ? user.proyectos.join(', ') : '') || (user.permisos ? user.permisos.join(', ') : '');
      setView('admin');
      form.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function resetAdminForm() {
      const form = document.getElementById('adminUserForm');
      document.getElementById('adminFormTitle').textContent = 'Crear usuario';
      form.reset();
      form.usuario.readOnly = false;
      form.rol.value = 'ALUMNI';
      form.campus.value = 'SEVILLA';
      form.facultad.value = 'INGENIERIA';
    }

    function canPublishEvents() {
      return state.session && ['PDI', 'PTGAS', 'ADMIN'].includes(state.session.rol);
    }

    function resetEventForm() {
      const form = document.getElementById('eventForm');
      form.reset();
      form.elements['id'].value = '';
      document.getElementById('eventFormTitle').textContent = canPublishEvents() ? 'Publicar evento' : 'Proponer evento';
      form.querySelector('button[type="submit"]').textContent = canPublishEvents() ? 'Guardar evento' : 'Enviar propuesta';
    }

    function editEvent(id) {
      const evento = state.events.find((item) => Number(item.id) === Number(id));
      if (!evento) {
        return;
      }
      const form = document.getElementById('eventForm');
      form.hidden = false;
      form.elements['id'].value = evento.id;
      form.nombre.value = evento.nombre || '';
      form.lugar.value = evento.lugar || '';
      form.descripcion.value = evento.descripcion || '';
      form.fechaApertura.value = evento.fechaApertura || '';
      form.fechaLimite.value = evento.fechaLimite || '';
      form.fechaEvento.value = evento.fechaEvento || '';
      form.ponente.value = evento.ponente || '';
      form.capacidadMaxima.value = evento.capacidadMaxima || '';
      document.getElementById('eventFormTitle').textContent = 'Modificar evento';
      form.querySelector('button[type="submit"]').textContent = 'Guardar cambios';
      form.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    async function deleteEvent(id) {
      if (!window.confirm('Eliminar este evento?')) {
        return;
      }
      try {
        await api('/EventoServlet', {
          method: 'DELETE',
          body: JSON.stringify({ usuario: state.session.usuario, id: id })
        });
        showToast('Evento eliminado.', 'success');
        await loadEvents();
      } catch (error) {
        showToast(error.message, 'error');
      }
    }

    function formatDate(value) {
      if (!value) {
        return 'Fecha por confirmar';
      }
      return new Date(value + 'T00:00:00').toLocaleDateString('es-ES', {
        day: '2-digit',
        month: 'long',
        year: 'numeric'
      });
    }

    function day(value) {
      if (!value) {
        return '--';
      }
      return new Date(value + 'T00:00:00').toLocaleDateString('es-ES', { day: '2-digit' });
    }

    function month(value) {
      if (!value) {
        return '';
      }
      return new Date(value + 'T00:00:00').toLocaleDateString('es-ES', { month: 'short' });
    }

    function escapeHtml(value) {
      return String(value === null || value === undefined ? '' : value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
    }

    document.querySelectorAll('[data-user]').forEach((button) => {
      button.addEventListener('click', () => {
        const form = document.getElementById('loginForm');
        form.usuario.value = button.dataset.user;
        form.contrasenia.value = button.dataset.pass;
      });
    });

    document.getElementById('loginForm').addEventListener('submit', async (event) => {
      event.preventDefault();
      try {
        const payload = await api('/LoginServlet', {
          method: 'POST',
          body: JSON.stringify(objectFromForm(event.currentTarget))
        });
        state.session = payload.usuario;
        authView.hidden = true;
        appView.hidden = false;
        renderShell();
        await loadInitialData();
        setView('home');
        showToast('Sesion iniciada correctamente.', 'success');
      } catch (error) {
        showToast(error.message, 'error');
      }
    });

    document.querySelectorAll('.nav-item').forEach((button) => {
      button.addEventListener('click', () => setView(button.dataset.view));
    });

    document.querySelectorAll('[data-open-view]').forEach((button) => {
      button.addEventListener('click', () => setView(button.dataset.openView));
    });

    document.getElementById('menuButton').addEventListener('click', () => {
      document.getElementById('mainNav').classList.toggle('open');
    });

    document.getElementById('backToDirectory').addEventListener('click', () => {
      setView('directory');
    });

    document.getElementById('logoutButton').addEventListener('click', () => {
      state.session = null;
      authView.hidden = false;
      appView.hidden = true;
      showToast('Sesion cerrada.', 'info');
    });

    document.getElementById('searchForm').addEventListener('submit', async (event) => {
      event.preventDefault();
      try {
        await loadDirectory(objectFromForm(event.currentTarget));
        showToast('Busqueda actualizada.', 'success');
      } catch (error) {
        showToast(error.message, 'error');
      }
    });

    document.getElementById('toggleEventForm').addEventListener('click', () => {
      const form = document.getElementById('eventForm');
      if (form.hidden) {
        resetEventForm();
      }
      form.hidden = !form.hidden;
    });

    document.getElementById('eventForm').addEventListener('submit', async (event) => {
      event.preventDefault();
      const form = event.currentTarget;
      const data = objectFromForm(form);
      const payload = clean({
        id: data.id,
        usuario: state.session.usuario,
        nombre: data.nombre,
        descripcion: data.descripcion,
        lugar: data.lugar,
        ponente: data.ponente,
        fechaApertura: data.fechaApertura,
        fechaLimite: data.fechaLimite,
        fechaEvento: data.fechaEvento,
        capacidadMaxima: data.capacidadMaxima,
        organizadorNombre: state.session ? activeUserName() : 'Universidad Loyola',
        organizadorId: state.session ? state.session.usuario : 'LOYOLA'
      });
      try {
        const response = await api('/EventoServlet', {
          method: data.id ? 'PUT' : 'POST',
          body: JSON.stringify(payload)
        });
        form.hidden = true;
        if (response.propuesta) {
          showToast(response.mensaje || 'Propuesta enviada correctamente.', 'success');
        } else {
          showToast(data.id ? 'Evento modificado correctamente.' : 'Evento publicado correctamente.', 'success');
          await loadEvents();
        }
      } catch (error) {
        showToast(error.message, 'error');
      }
    });

    document.getElementById('profileForm').addEventListener('submit', async (event) => {
      event.preventDefault();
      const form = event.currentTarget;
      if (!state.session.perfilAlumni) {
        return;
      }
      const data = objectFromForm(form);
      const payload = {
        usuario: state.session.usuario,
        nombre: data.nombre,
        apellidos: data.apellidos,
        email: data.email,
        telefono: data.telefono,
        titulacion: data.titulacion,
        promocion: data.promocion,
        ciudad: data.ciudad,
        campus: data.campus,
        facultad: data.facultad,
        hobbies: data.hobbies,
        trabajo: {
          descripcion: data.trabajoDescripcion,
          ciudad: data.ciudad
        }
      };
      try {
        const response = await api('/PerfilServlet', {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        state.session.perfilAlumni = response.perfil;
        state.session.nombre = response.perfil.nombre;
        state.session.apellidos = response.perfil.apellidos;
        state.session.email = response.perfil.email;
        state.session.telefono = response.perfil.telefono;
        renderShell();
        await loadDirectory();
        showToast('Perfil actualizado.', 'success');
      } catch (error) {
        showToast(error.message, 'error');
      }
    });

    document.getElementById('refreshUsers').addEventListener('click', async () => {
      try {
        await loadUsers();
        showToast('Usuarios actualizados.', 'success');
      } catch (error) {
        showToast(error.message, 'error');
      }
    });

    document.getElementById('resetAdminForm').addEventListener('click', resetAdminForm);

    document.getElementById('adminUserForm').addEventListener('submit', async (event) => {
      event.preventDefault();
      const form = event.currentTarget;
      const data = objectFromForm(form);
      const payload = clean({
        adminUsuario: state.session.usuario,
        rol: data.rol,
        usuario: data.usuario,
        dni: data.dni,
        contrasenia: data.contrasenia,
        nombre: data.nombre,
        apellidos: data.apellidos,
        email: data.email,
        telefono: data.telefono,
        titulacion: data.titulacion,
        promocion: data.promocion,
        ciudad: data.ciudad,
        campus: data.campus,
        facultad: data.facultad,
        trabajoDescripcion: data.trabajoDescripcion,
        hobbies: data.hobbies,
        proyectos: data.hobbies,
        permisos: data.hobbies
      });
      try {
        await api('/UsuarioAdminServlet', {
          method: form.usuario.readOnly ? 'PUT' : 'POST',
          body: JSON.stringify(payload)
        });
        showToast(form.usuario.readOnly ? 'Usuario modificado.' : 'Usuario creado.', 'success');
        resetAdminForm();
        await Promise.all([loadUsers(), loadDirectory()]);
      } catch (error) {
        showToast(error.message, 'error');
      }
    });
  </script>
  <!-- PWA: registro del Service Worker (RNF-1) -->
  <script>
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', function () {
        navigator.serviceWorker
          .register('<%= request.getContextPath() %>/service-worker.js')
          .catch(function (e) { console.warn('No se pudo registrar el Service Worker:', e); });
      });
    }
  </script>
</body>
</html>
