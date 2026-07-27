import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { WatchlistEntry, WatchlistDocument } from './schemas/watchlist.schema';
import { WatchlistInput } from './dto/watchlist.input';
import { WatchlistEntryObject } from './entities/watchlist-entry.entity';
import { PaginatedWatchlistObject } from './entities/paginated-watchlist.entity';
import { PaginationInput } from '../../common/dto/pagination.input';
import { MediaType } from '../../common/enums/media-type.enum';

@Injectable()
export class WatchlistService {
  constructor(
    @InjectModel(WatchlistEntry.name)
    private readonly watchlistModel: Model<WatchlistDocument>,
  ) {}

  async addToWatchlist(
    input: WatchlistInput,
    userId: string,
  ): Promise<WatchlistEntryObject> {
    try {
      const created = await this.watchlistModel.create({ ...input, userId });
      return this.toObject(created);
    } catch (error: any) {
      const mongoError = error as { code?: number };
      if (mongoError.code === 11000) {
        throw new ConflictException('Este contenido ya está en tu watchlist.');
      }
      throw error;
    }
  }

  async removeFromWatchlist(
    input: WatchlistInput,
    userId: string,
  ): Promise<boolean> {
    const result = await this.watchlistModel.findOneAndDelete({
      userId,
      tmdbId: input.tmdbId,
      mediaType: input.mediaType,
    });

    if (!result) {
      throw new NotFoundException('Este contenido no está en tu watchlist.');
    }

    return true;
  }

  async findWatchlistByUser(
    userId: string,
    pagination: PaginationInput = { page: 1, limit: 10 },
  ): Promise<PaginatedWatchlistObject> {
    const { page, limit } = pagination;
    const skip = (page - 1) * limit;

    const [items, total] = await Promise.all([
      this.watchlistModel
        .find({ userId })
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit)
        .exec(),
      this.watchlistModel.countDocuments({ userId }).exec(),
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

  async isInWatchlist(
    userId: string,
    tmdbId: number,
    mediaType: MediaType,
  ): Promise<boolean> {
    const doc = await this.watchlistModel
      .findOne({ userId, tmdbId, mediaType })
      .select({ _id: 1 })
      .exec();
    return doc !== null;
  }

  private toObject(doc: WatchlistDocument): WatchlistEntryObject {
    const wDoc = doc as WatchlistDocument & { createdAt: Date };
    return {
      id: doc._id.toString(),
      userId: doc.userId,
      tmdbId: doc.tmdbId,
      mediaType: doc.mediaType,
      createdAt: wDoc.createdAt,
    };
  }
}
