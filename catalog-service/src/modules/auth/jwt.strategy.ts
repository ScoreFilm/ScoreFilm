import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { decode } from 'jsonwebtoken';
import { createJwksClient } from '../../config/jwt.config';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(configService: ConfigService) {
    const jwksClient = createJwksClient(
      configService.get<string>('jwt.jwksUrl'),
    );

    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      algorithms: ['RS256'],
      secretOrKeyProvider: (_request, rawJwtToken, done) => {
        const decodedToken = decode(rawJwtToken, { complete: true });
        const kid =
          decodedToken && typeof decodedToken !== 'string'
            ? decodedToken.header.kid
            : undefined;

        if (!kid) {
          done(new Error('JWT header does not contain a kid'));
          return;
        }

        jwksClient
          .getSigningKey(kid)
          .then((key) => done(null, key.getPublicKey()))
          .catch((error: unknown) => done(error as Error));
      },
    });
  }

  validate(payload: any) {
    return {
      userId: payload.sub,
      ...payload,
    };
  }
}
