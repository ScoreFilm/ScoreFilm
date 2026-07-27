import { Field, Int, ObjectType } from '@nestjs/graphql';
import { WatchlistEntryObject } from './watchlist-entry.entity';

@ObjectType()
export class PaginatedWatchlistObject {
  @Field(() => [WatchlistEntryObject])
  items!: WatchlistEntryObject[];

  @Field(() => Int)
  total!: number;

  @Field(() => Int)
  page!: number;

  @Field(() => Int)
  totalPages!: number;

  @Field()
  hasNextPage!: boolean;

  @Field()
  hasPreviousPage!: boolean;
}
