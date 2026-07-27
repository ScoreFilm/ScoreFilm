import { Field, ID, Int, ObjectType } from '@nestjs/graphql';
import { MediaType } from '../../../common/enums/media-type.enum';

@ObjectType()
export class WatchlistEntryObject {
  @Field(() => ID)
  id!: string;

  @Field()
  userId!: string;

  @Field(() => Int)
  tmdbId!: number;

  @Field(() => MediaType)
  mediaType!: MediaType;

  @Field()
  createdAt!: Date;
}
