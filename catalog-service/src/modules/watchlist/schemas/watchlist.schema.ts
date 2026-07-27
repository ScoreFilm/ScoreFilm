import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';
import { MediaType } from '../../../common/enums/media-type.enum';

export type WatchlistDocument = HydratedDocument<WatchlistEntry>;

@Schema({
  timestamps: { createdAt: true, updatedAt: false },
  collection: 'watchlist',
})
export class WatchlistEntry {
  @Prop({ required: true })
  userId!: string;

  @Prop({ required: true })
  tmdbId!: number;

  @Prop({ required: true, enum: MediaType })
  mediaType!: MediaType;
}

export const WatchlistSchema = SchemaFactory.createForClass(WatchlistEntry);

WatchlistSchema.index({ userId: 1, tmdbId: 1, mediaType: 1 }, { unique: true });
WatchlistSchema.index({ userId: 1 });
