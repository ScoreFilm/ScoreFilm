export default () => ({
  port: parseInt(process.env.PORT ?? '3001', 10),
  mongodb: {
    uri:
      process.env.MONGODB_URI ?? 'mongodb://localhost:27017/scorefilm_catalog',
  },
  jwt: {
    jwksUrl:
      process.env.AUTH_SERVICE_JWKS_URL ??
      'http://localhost:8080/.well-known/jwks.json',
  },
  tmdb: {
    apiKey: process.env.TMDB_API_KEY,
  },
});
