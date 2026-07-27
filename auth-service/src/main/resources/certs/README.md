# JWT RSA Keys

RSA private keys must not be committed to source control. Anyone with the
private key can sign tokens trusted by this service. Store keys in a secret
manager or secure deployment volume and keep local copies outside public
repositories.

The application expects these files by default in this directory:

- `jwt-private.pem` - RSA private key used to sign JWTs.
- `jwt-public.pem` - RSA public key exposed through the JWKS endpoint.

Override their locations with `JWT_PRIVATE_KEY_PATH` and
`JWT_PUBLIC_KEY_PATH` when keys are mounted elsewhere.

## Generate a key pair

Run the following commands from this directory. OpenSSL must be installed.

### Linux

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
openssl rsa -pubout -in jwt-private.pem -out jwt-public.pem
chmod 600 jwt-private.pem
```

### macOS

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
openssl rsa -pubout -in jwt-private.pem -out jwt-public.pem
chmod 600 jwt-private.pem
```

### Windows Git Bash

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-private.pem
openssl rsa -pubout -in jwt-private.pem -out jwt-public.pem
```
