import { registerEnumType } from '@nestjs/graphql';

export enum ReviewSortBy {
  NEWEST = 'NEWEST',
  OLDEST = 'OLDEST',
  HIGHEST_RATING = 'HIGHEST_RATING',
  LOWEST_RATING = 'LOWEST_RATING',
}

registerEnumType(ReviewSortBy, {
  name: 'ReviewSortBy',
});
