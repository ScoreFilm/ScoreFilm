import { HttpStatus } from '@nestjs/common';
import { TmdbException } from './tmdb.exception';

export class TmdbUnavailableException extends TmdbException {
  constructor() {
    super('TMDB service is unavailable', HttpStatus.SERVICE_UNAVAILABLE);
  }
}
