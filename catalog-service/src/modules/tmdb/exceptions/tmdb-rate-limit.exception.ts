import { HttpStatus } from '@nestjs/common';
import { TmdbException } from './tmdb.exception';

export class TmdbRateLimitException extends TmdbException {
  constructor() {
    super('TMDB rate limit exceeded', HttpStatus.TOO_MANY_REQUESTS);
  }
}
