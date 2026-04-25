export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  sessionHours: 24,
  defaultAppointmentWindowWeeks: 4,
  keycloak: {
    url:      'http://localhost:8180',
    realm:    'Piedrazul-Realm',
    clientId: 'piedrazul-frontend'
  }
};
