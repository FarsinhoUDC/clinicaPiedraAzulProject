export const environment = {
  production: false,
  apiBaseUrl: 'http://192.168.0.15:8000/api',
  sessionHours: 24,
  defaultAppointmentWindowWeeks: 4,
  keycloak: {
    url:      'http://192.168.0.15:8180',
    realm:    'Piedrazul-Realm',
    clientId: 'piedrazul-frontend'
  }
};
