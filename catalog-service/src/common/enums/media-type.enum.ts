import { registerEnumType } from '@nestjs/graphql';

export enum MediaType {
  MOVIE = 'movie',
  TV = 'tv',
}

registerEnumType(MediaType, {
  name: 'MediaType',
});
