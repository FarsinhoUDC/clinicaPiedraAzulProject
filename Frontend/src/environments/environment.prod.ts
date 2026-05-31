export const environment = {
  production: true,
  apiBaseUrl: 'https://clinicapiedraazulproject-production.up.railway.app/api',
  sessionHours: 24,
  defaultAppointmentWindowWeeks: 4,
  keycloak: {
    url:      'http://192.168.0.15:8180',
    realm: 'Piedrazul-Realm',
    clientId: 'piedrazul-frontend'
  }
};