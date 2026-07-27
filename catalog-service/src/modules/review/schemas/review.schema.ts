import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';
import { RatingEntry, RatingEntrySchema } from './rating.schema';
import { MediaType } from '../../../common/enums/media-type.enum';

export type ReviewDocument = HydratedDocument<Review>;

@Schema({ timestamps: true, collection: 'reviews' })
export class Review {
  @Prop({ required: true })
  userId!: string;

  @Prop({ required: true })
  tmdbId!: number;

  @Prop({ required: true, enum: MediaType })
  mediaType!: MediaType;

  @Prop({ type: [RatingEntrySchema], default: [] })
  ratings!: RatingEntry[];

  @Prop({ required: true, min: 1, max: 10 })
  overallRating!: number;

  @Prop()
  comment?: string;
}

export const ReviewSchema = SchemaFactory.createForClass(Review);

ReviewSchema.index({ userId: 1, tmdbId: 1, mediaType: 1 }, { unique: true });
ReviewSchema.index({ tmdbId: 1, mediaType: 1 });
