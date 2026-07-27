import { Field, Float, Int, ObjectType } from '@nestjs/graphql';
import { Genre } from './genre.entity';

@ObjectType()
export class Tv {
  @Field(() => Int)
  id!: number;

  @Field()
  name!: string;

  @Field()
  overview!: string;

  @Field(() => String, { nullable: true })
  posterPath!: string | null;

  @Field(() => String, { nullable: true })
  firstAirDate!: string | null;

  @Field(() => [Genre])
  genres!: Genre[];

  @Field(() => Float)
  voteAverage!: number;
}
