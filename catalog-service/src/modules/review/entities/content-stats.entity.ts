import { Field, Float, Int, ObjectType } from '@nestjs/graphql';
import { MediaType } from '../../../common/enums/media-type.enum';

@ObjectType()
export class ContentStatsObject {
  @Field(() => Int)
  tmdbId!: number;

  @Field(() => MediaType)
  mediaType!: MediaType;

  @Field(() => Float)
  averageRating!: number;

  @Field(() => Int)
  totalReviews!: number;
}
