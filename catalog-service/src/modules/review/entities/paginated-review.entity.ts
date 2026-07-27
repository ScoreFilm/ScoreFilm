import { Field, Int, ObjectType } from '@nestjs/graphql';
import { ReviewObject } from './review.entity';

@ObjectType()
export class PaginatedReviewsObject {
  @Field(() => [ReviewObject])
  items!: ReviewObject[];

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
