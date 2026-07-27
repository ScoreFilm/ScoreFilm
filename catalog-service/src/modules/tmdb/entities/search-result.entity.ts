import { createUnionType } from '@nestjs/graphql';
import { Movie } from './movie.entity';
import { Tv } from './tv.entity';

export const searchResultUnion = createUnionType({
  name: 'SearchResult',
  types: () => [Movie, Tv] as const,
  resolveType(value) {
    if (value.title) {
      return Movie;
    }

    if (value.name) {
      return Tv;
    }

    return null;
  },
});
