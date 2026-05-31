export const environment = {
  production: true,
  apiBaseUrl: 'https://clinicapiedraazulproject-production.up.railway.app/api',
  sessionHours: 24,
  defaultAppointmentWindowWeeks: 4,
  keycloak: {
    url: 'https://keycloak-production-9978.up.railway.app',
    realm: 'Piedrazul-Realm',
    clientId: 'piedrazul-frontend'
  }
};