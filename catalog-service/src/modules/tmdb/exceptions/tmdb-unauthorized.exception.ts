import { HttpStatus } from '@nestjs/common';
import { TmdbException } from './tmdb.exception';

export class TmdbUnauthorizedException extends TmdbException {
  constructor() {
    super('TMDB authorization failed', HttpStatus.UNAUTHORIZED);
  }
}
