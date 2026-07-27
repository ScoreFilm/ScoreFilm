import { HttpStatus } from '@nestjs/common';
import { TmdbException } from './tmdb.exception';

export class TmdbNotFoundException extends TmdbException {
  constructor() {
    super('TMDB resource not found', HttpStatus.NOT_FOUND);
  }
}
