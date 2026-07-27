import { Field, InputType, Int } from '@nestjs/graphql';
import {
  IsEnum,
  IsInt,
  IsOptional,
  IsString,
  Max,
  Min,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { ReviewAspect } from '../enums/review-aspect.enum';
import { MediaType } from '../../../common/enums/media-type.enum';

@InputType()
export class RatingEntryInput {
  @Field(() => ReviewAspect)
  @IsEnum(ReviewAspect)
  aspect!: ReviewAspect;

  @Field(() => Int)
  @IsInt()
  @Min(1)
  @Max(10)
  score!: number;
}

@InputType()
export class CreateReviewInput {
  @Field(() => Int)
  @IsInt()
  tmdbId!: number;

  @Field(() => MediaType)
  @IsEnum(MediaType)
  mediaType!: MediaType;

  @Field(() => [RatingEntryInput])
  @ValidateNested({ each: true })
  @Type(() => RatingEntryInput)
  ratings!: RatingEntryInput[];

  @Field({ nullable: true })
  @IsString()
  @IsOptional()
  comment?: string;
}
