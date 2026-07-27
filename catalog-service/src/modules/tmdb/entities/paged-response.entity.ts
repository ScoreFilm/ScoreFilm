import { Type } from '@nestjs/common';
import { Field, Int, ObjectType } from '@nestjs/graphql';
import { Movie } from './movie.entity';
import { Tv } from './tv.entity';
import { searchResultUnion } from './search-result.entity';

@ObjectType()
export class PaginatedMovies extends Paginated(Movie) {}

@ObjectType()
export class PaginatedTV extends Paginated(Tv) {}

@ObjectType()
export class PaginatedSearchResults extends Paginated(
  searchResultUnion as any,
) {}

export function Paginated<T>(classRef: Type<T>) {
  @ObjectType({ isAbstract: true })
  abstract class PaginatedType {
    @Field(() => Int)
    page!: number;

    @Field(() => Int)
    totalPages!: number;

    @Field(() => Int)
    totalResults!: number;

    @Field(() => [classRef])
    results!: T[];
  }

  return PaginatedType;
}
