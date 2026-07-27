import {
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, isValidObjectId } from 'mongoose';
import { Review, ReviewDocument } from './schemas/review.schema';
import { CreateReviewInput } from './dto/create-review.input';
import { UpdateReviewInput } from './dto/update-review.input';
import { ReviewFilterInput } from './dto/review-filter.input';
import { PaginationInput } from '../../common/dto/pagination.input';
import { ReviewObject } from './entities/review.entity';
import { PaginatedReviewsObject } from './entities/paginated-review.entity';
import { ContentStatsObject } from './entities/content-stats.entity';
import { MediaType } from '../../common/enums/media-type.enum';
import { ReviewSortBy } from './enums/review-sort.enum';

@Injectable()
export class ReviewService {
  constructor(
    @InjectModel(Review.name)
    private readonly reviewModel: Model<ReviewDocument>,
  ) {}

  async createReview(
    input: CreateReviewInput,
    userId: string,
  ): Promise<ReviewObject> {
    const overallRating = this.calculateAverage(
      input.ratings.map((r) => r.score),
    );

    try {
      const created = await this.reviewModel.create({
        ...input,
        userId,
        overallRating,
      });

      return this.toObject(created);
    } catch (error: any) {
      const mongoError = error as { code?: number };
      if (mongoError.code === 11000) {
        throw new ConflictException(
          'Ya existe una reseña de este usuario para este contenido.',
        );
      }
      throw error;
    }
  }

  async findReviewById(id: string): Promise<ReviewObject> {
    const doc = await this.findByIdOrThrow(id);
    return this.toObject(doc);
  }

  async findReviews(
    filter: ReviewFilterInput = {},
    sortBy: ReviewSortBy = ReviewSortBy.NEWEST,
    pagination: PaginationInput = { page: 1, limit: 10 },
  ): Promise<PaginatedReviewsObject> {
    const query = this.buildFilterQuery(filter);
    const sort = this.buildSortQuery(sortBy);
    const { page, limit } = pagination;
    const skip = (page - 1) * limit;

    const [items, total] = await Promise.all([
      this.reviewModel.find(query).sort(sort).skip(skip).limit(limit).exec(),
      this.reviewModel.countDocuments(query).exec(),
    ]);

    const totalPages = Math.max(Math.ceil(total / limit), 1);

    return {
      items: items.map((doc) => this.toObject(doc)),
      total,
      page,
      totalPages,
      hasNextPage: page < totalPages,
      hasPreviousPage: page > 1,
    };
  }

  async getContentStats(
    tmdbId: number,
    mediaType: MediaType,
  ): Promise<ContentStatsObject> {
    const result = await this.reviewModel
      .aggregate<{ _id: null; averageRating: number; totalReviews: number }>([
        { $match: { tmdbId, mediaType } },
        {
          $group: {
            _id: null,
            averageRating: { $avg: '$overallRating' },
            totalReviews: { $sum: 1 },
          },
        },
      ])
      .exec();

    if (result.length === 0) {
      return { tmdbId, mediaType, averageRating: 0, totalReviews: 0 };
    }

    return {
      tmdbId,
      mediaType,
      averageRating: Math.round(result[0].averageRating * 10) / 10,
      totalReviews: result[0].totalReviews,
    };
  }

  async updateReview(
    id: string,
    input: UpdateReviewInput,
    userId: string,
  ): Promise<ReviewObject> {
    const doc = await this.findByIdOrThrow(id);

    if (doc.userId !== userId) {
      throw new ForbiddenException(
        'No tienes permiso para modificar esta reseña.',
      );
    }

    if (input.ratings) {
      doc.ratings = input.ratings;
      doc.overallRating = this.calculateAverage(
        input.ratings.map((r) => r.score),
      );
    }

    if (input.comment !== undefined) {
      doc.comment = input.comment;
    }

    try {
      const updated = await doc.save();
      return this.toObject(updated);
    } catch (error: any) {
      const mongoError = error as { code?: number };
      if (mongoError.code === 11000) {
        throw new ConflictException(
          'Ya existe una reseña de este usuario para este contenido.',
        );
      }
      throw error;
    }
  }

  async deleteReview(id: string, userId: string): Promise<boolean> {
    const doc = await this.findByIdOrThrow(id);

    if (doc.userId !== userId) {
      throw new ForbiddenException(
        'No tienes permiso para eliminar esta reseña.',
      );
    }

    await doc.deleteOne();
    return true;
  }

  private buildFilterQuery(filter: ReviewFilterInput): Record<string, unknown> {
    const query: Record<string, unknown> = {};

    if (filter.userId) query.userId = filter.userId;
    if (filter.tmdbId) query.tmdbId = filter.tmdbId;
    if (filter.mediaType) query.mediaType = filter.mediaType;

    if (filter.hasComment === true) {
      query.comment = { $exists: true, $ne: '' };
    } else if (filter.hasComment === false) {
      query.$or = [{ comment: { $exists: false } }, { comment: '' }];
    }

    if (filter.minRating !== undefined || filter.maxRating !== undefined) {
      query.overallRating = {};
      if (filter.minRating !== undefined) {
        (query.overallRating as Record<string, number>).$gte = filter.minRating;
      }
      if (filter.maxRating !== undefined) {
        (query.overallRating as Record<string, number>).$lte = filter.maxRating;
      }
    }

    return query;
  }

  private buildSortQuery(sortBy: ReviewSortBy): Record<string, 1 | -1> {
    switch (sortBy) {
      case ReviewSortBy.OLDEST:
        return { createdAt: 1 };
      case ReviewSortBy.HIGHEST_RATING:
        return { overallRating: -1 };
      case ReviewSortBy.LOWEST_RATING:
        return { overallRating: 1 };
      case ReviewSortBy.NEWEST:
      default:
        return { createdAt: -1 };
    }
  }

  private async findByIdOrThrow(id: string): Promise<ReviewDocument> {
    if (!isValidObjectId(id)) {
      throw new NotFoundException('Reseña no encontrada.');
    }

    const doc = await this.reviewModel.findById(id).exec();
    if (!doc) {
      throw new NotFoundException('Reseña no encontrada.');
    }

    return doc;
  }

  private calculateAverage(scores: number[]): number {
    if (scores.length === 0) return 0;
    const sum = scores.reduce((acc, s) => acc + s, 0);
    return Math.round((sum / scores.length) * 10) / 10;
  }

  private toObject(doc: ReviewDocument): ReviewObject {
    const reviewDoc = doc as ReviewDocument & {
      createdAt: Date;
      updatedAt: Date;
    };

    return {
      id: doc._id.toString(),
      userId: doc.userId,
      tmdbId: doc.tmdbId,
      mediaType: doc.mediaType,
      ratings: doc.ratings,
      overallRating: doc.overallRating,
      comment: doc.comment,
      createdAt: reviewDoc.createdAt,
      updatedAt: reviewDoc.updatedAt,
    };
  }
}
