import { Field, InputType, Int } from '@nestjs/graphql';
import { IsEnum, IsInt, IsPositive, IsString } from 'class-validator';
import { MediaType } from '../../../common/enums/media-type.enum';

@InputType()
export class FavoriteInput {
  @Field(() => Int)
  @IsInt()
  @IsPositive()
  tmdbId!: number;

  @Field(() => MediaType)
  @IsEnum(MediaType)
  mediaType!: MediaType;
}
