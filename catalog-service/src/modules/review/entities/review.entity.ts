import { Field, Float, ID, Int, ObjectType } from '@nestjs/graphql';
import { RatingObject } from './rating.entity';
import { MediaType } from '../../../common/enums/media-type.enum';
import { Movie } from '../../tmdb/entities/movie.entity';
import { Tv } from '../../tmdb/entities/tv.entity';
import { searchResultUnion } from '../../tmdb/entities/search-result.entity';

@ObjectType()
export class ReviewObject {
  @Field(() => ID)
  id!: string;

  @Field()
  userId!: string;

  @Field(() => Int)
  tmdbId!: number;

  @Field(() => MediaType)
  mediaType!: MediaType;

  @Field(() => [RatingObject])
  ratings!: RatingObject[];

  @Field(() => Float)
  overallRating!: number;

  @Field({ nullable: true })
  comment?: string;

  @Field()
  createdAt!: Date;

  @Field()
  updatedAt!: Date;

  @Field(() => searchResultUnion, { nullable: true })
  movie?: Movie | Tv;
}
