import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';
import { MediaType } from '../../../common/enums/media-type.enum';

export type FavoriteDocument = HydratedDocument<Favorite>;

@Schema({
  timestamps: { createdAt: true, updatedAt: false },
  collection: 'favorites',
})
export class Favorite {
  @Prop({ required: true })
  userId!: string;

  @Prop({ required: true })
  tmdbId!: number;

  @Prop({ required: true, enum: MediaType })
  mediaType!: MediaType;
}

export const FavoriteSchema = SchemaFactory.createForClass(Favorite);

FavoriteSchema.index({ userId: 1, tmdbId: 1, mediaType: 1 }, { unique: true });
FavoriteSchema.index({ userId: 1 });
