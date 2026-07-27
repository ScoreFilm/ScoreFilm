import { HttpException, HttpStatus } from '@nestjs/common';

export class TmdbException extends HttpException {
  constructor(message: string, status: HttpStatus) {
    super(message, status);
  }
}
