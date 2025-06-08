export default () => ({
  apiGatewayUrl: process.env.API_GATEWAY_URL || 'http://api-gateway:8080',
  oauthClientId: process.env.OAUTH_CLIENT_ID || 'oidc-client',
  oauthClientSecret: process.env.OAUTH_CLIENT_SECRET || 'secret',
});

// It's also common to create a strongly-typed interface or class for the config
export interface AppConfig {
  apiGatewayUrl: string;
  oauthClientId: string;
  oauthClientSecret: string;
}
