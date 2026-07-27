import { Field, Int, ObjectType } from '@nestjs/graphql';
import { FavoriteObject } from './favorite.entity';

@ObjectType()
export class PaginatedFavoritesObject {
  @Field(() => [FavoriteObject])
  items!: FavoriteObject[];

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
