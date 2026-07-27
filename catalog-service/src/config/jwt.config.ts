import { JwksClient } from 'jwks-rsa';

export const DEFAULT_AUTH_SERVICE_JWKS_URL =
  'http://localhost:8080/.well-known/jwks.json';

/**
 * Creates a JWKS client used to retrieve the public key matching a token's
 * `kid`.  The client caches keys and rate-limits requests to the auth service.
 */
export const createJwksClient = (jwksUri?: string): JwksClient =>
  new JwksClient({
    jwksUri:
      jwksUri ??
      process.env.AUTH_SERVICE_JWKS_URL ??
      DEFAULT_AUTH_SERVICE_JWKS_URL,
    cache: true,
    rateLimit: true,
  });
