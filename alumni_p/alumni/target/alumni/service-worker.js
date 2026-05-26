/* ===========================================================================
 *  service-worker.js  -  Loyola Alumni PWA  (RNF-1)
 *
 *  Estrategia de cache:
 *    - Recursos estaticos (CSS, JS, iconos): cache-first.
 *    - Navegaciones (paginas): network-first con respaldo offline.
 *    - Servicios del backend (*Servlet): network-only (datos siempre frescos;
 *      no se cachean para no servir informacion obsoleta ni sensible).
 * ========================================================================= */

const CACHE = 'alumni-pwa-v1';

/* Recursos del "esqueleto" de la aplicacion que se precachean al instalar. */
const SHELL = [
  'index.jsp',
  'styles.css',
  'offline.html',
  'manifest.json',
  'assets/icons/icon-192.png',
  'assets/icons/icon-512.png'
];

/* Instalacion: precarga el esqueleto. */
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE)
      .then((cache) => cache.addAll(SHELL))
      .then(() => self.skipWaiting())
      .catch(() => self.skipWaiting())
  );
});

/* Activacion: elimina caches de versiones anteriores. */
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((claves) => Promise.all(
        claves.filter((c) => c !== CACHE).map((c) => caches.delete(c))
      ))
      .then(() => self.clients.claim())
  );
});

/* Intercepcion de peticiones. */
self.addEventListener('fetch', (event) => {
  const req = event.request;

  /* Solo se gestionan peticiones GET; el resto van directas a la red. */
  if (req.method !== 'GET') {
    return;
  }

  const url = new URL(req.url);

  /* Las llamadas a los servlets del backend no se cachean (network-only). */
  if (url.pathname.indexOf('Servlet') !== -1) {
    return;
  }

  /* Navegaciones: primero red, y si falla se sirve el contenido offline. */
  if (req.mode === 'navigate') {
    event.respondWith(
      fetch(req)
        .then((resp) => {
          const copia = resp.clone();
          caches.open(CACHE).then((cache) => cache.put(req, copia));
          return resp;
        })
        .catch(() => caches.match(req)
          .then((cacheada) => cacheada || caches.match('offline.html')))
    );
    return;
  }

  /* Recursos estaticos: primero cache, y si no esta se va a la red. */
  event.respondWith(
    caches.match(req).then((cacheada) => {
      if (cacheada) {
        return cacheada;
      }
      return fetch(req).then((resp) => {
        if (resp && resp.status === 200 && resp.type === 'basic') {
          const copia = resp.clone();
          caches.open(CACHE).then((cache) => cache.put(req, copia));
        }
        return resp;
      });
    })
  );
});
