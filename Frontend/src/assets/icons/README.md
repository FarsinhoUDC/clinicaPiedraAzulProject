# Íconos PWA — Piedrazul SPA

Debes colocar aquí los íconos de tu app en los siguientes tamaños:

| Archivo             | Uso                                      |
|---------------------|------------------------------------------|
| icon-72x72.png      | Android (pantalla de inicio)             |
| icon-96x96.png      | Android (pantalla de inicio)             |
| icon-128x128.png    | Chrome Web Store                         |
| icon-144x144.png    | Windows (tile / msapplication)           |
| icon-152x152.png    | iOS Safari (apple-touch-icon)            |
| icon-192x192.png    | Android (splash screen y launcher)       |
| icon-384x384.png    | Android (pantallas de alta densidad)     |
| icon-512x512.png    | Android (splash screen grande, Lighthouse) |

## ¿Cómo generarlos fácilmente?

1. Usa https://realfavicongenerator.net — sube tu logo y descarga el paquete.
2. O usa el CLI: npx pwa-asset-generator logo.png ./src/assets/icons

Los íconos deben ser cuadrados, idealmente con fondo de color (no transparente)
para que se vean bien como "maskable" en Android.
