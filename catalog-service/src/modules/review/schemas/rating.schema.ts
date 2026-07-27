import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { ReviewAspect } from '../enums/review-aspect.enum';

@Schema({ _id: false })
export class RatingEntry {
  @Prop({ required: true, enum: ReviewAspect })
  aspect!: ReviewAspect;

  @Prop({ required: true, min: 1, max: 10 })
  score!: number;
}

export const RatingEntrySchema = SchemaFactory.createForClass(RatingEntry);
