import { Field, InputType } from '@nestjs/graphql';
import {
  ArrayMinSize,
  IsOptional,
  IsString,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { RatingEntryInput } from './create-review.input';

@InputType()
export class UpdateReviewInput {
  @Field(() => [RatingEntryInput], { nullable: true })
  @IsOptional()
  @ValidateNested({ each: true })
  @ArrayMinSize(1)
  @Type(() => RatingEntryInput)
  ratings?: RatingEntryInput[];

  @Field({ nullable: true })
  @IsOptional()
  @IsString()
  comment?: string;
}
