import { Field, Float, Int, ObjectType } from '@nestjs/graphql';
import { Genre } from './genre.entity';

@ObjectType()
export class Movie {
  @Field(() => Int)
  id!: number;

  @Field()
  title!: string;

  @Field()
  overview!: string;

  @Field(() => String, { nullable: true })
  posterPath!: string | null;

  @Field(() => String, { nullable: true })
  backdropPath!: string | null;

  @Field(() => String, { nullable: true })
  releaseDate!: string | null;

  @Field(() => [Genre])
  genres!: Genre[];

  @Field(() => Float)
  voteAverage!: number;

  @Field(() => Int, { nullable: true })
  runtime!: number | null;

  @Field()
  originalLanguage!: string;

  @Field()
  adult!: boolean;
}
