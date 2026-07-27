import {
  ConflictException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Favorite, FavoriteDocument } from './schemas/favorite.schema';
import { FavoriteInput } from './dto/favorite.input';
import { FavoriteObject } from './entities/favorite.entity';
import { PaginatedFavoritesObject } from './entities/paginated-favorite.entity';
import { PaginationInput } from '../../common/dto/pagination.input';
import { MediaType } from '../../common/enums/media-type.enum';

@Injectable()
export class FavoriteService {
  constructor(
    @InjectModel(Favorite.name)
    private readonly favoriteModel: Model<FavoriteDocument>,
  ) {}

  async addFavorite(
    input: FavoriteInput,
    userId: string,
  ): Promise<FavoriteObject> {
    try {
      const created = await this.favoriteModel.create({ ...input, userId });
      return this.toObject(created);
    } catch (error: any) {
      const mongoError = error as { code?: number };
      if (mongoError.code === 11000) {
        throw new ConflictException('Este contenido ya está en favoritos.');
      }
      throw error;
    }
  }

  async removeFavorite(input: FavoriteInput, userId: string): Promise<boolean> {
    const result = await this.favoriteModel.findOneAndDelete({
      userId,
      tmdbId: input.tmdbId,
      mediaType: input.mediaType,
    });

    if (!result) {
      throw new NotFoundException(
        'Este contenido no está en la lista de favoritos.',
      );
    }

    return true;
  }

  async findFavoritesByUser(
    userId: string,
    pagination: PaginationInput = { page: 1, limit: 10 },
  ): Promise<PaginatedFavoritesObject> {
    const { page, limit } = pagination;
    const skip = (page - 1) * limit;

    const [items, total] = await Promise.all([
      this.favoriteModel
        .find({ userId })
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit)
        .exec(),
      this.favoriteModel.countDocuments({ userId }).exec(),
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

  async isFavorite(
    userId: string,
    tmdbId: number,
    mediaType: MediaType,
  ): Promise<boolean> {
    const doc = await this.favoriteModel
      .findOne({ userId, tmdbId, mediaType })
      .select({ _id: 1 })
      .exec();
    return doc !== null;
  }

  private toObject(doc: FavoriteDocument): FavoriteObject {
    const favDoc = doc as FavoriteDocument & { createdAt: Date };
    return {
      id: doc._id.toString(),
      userId: doc.userId,
      tmdbId: doc.tmdbId,
      mediaType: doc.mediaType,
      createdAt: favDoc.createdAt,
    };
  }
}
