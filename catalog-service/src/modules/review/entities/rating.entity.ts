import { Field, Int, ObjectType } from '@nestjs/graphql';
import { ReviewAspect } from '../enums/review-aspect.enum';

@ObjectType()
export class RatingObject {
  @Field(() => ReviewAspect)
  aspect!: ReviewAspect;

  @Field(() => Int)
  score!: number;
}
