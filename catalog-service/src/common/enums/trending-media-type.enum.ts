import { registerEnumType } from '@nestjs/graphql';

export enum TrendingMediaType {
  MOVIE = 'movie',
  TV = 'tv',
  ALL = 'all',
}

registerEnumType(TrendingMediaType, { name: 'TrendingMediaType' });
