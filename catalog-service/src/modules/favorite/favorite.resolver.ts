import { UseGuards } from '@nestjs/common';
import { Args, Int, Mutation, Query, Resolver } from '@nestjs/graphql';
import { FavoriteObject } from './entities/favorite.entity';
import { PaginatedFavoritesObject } from './entities/paginated-favorite.entity';
import { FavoriteService } from './favorite.service';
import { FavoriteInput } from './dto/favorite.input';
import { PaginationInput } from '../../common/dto/pagination.input';
import { MediaType } from '../../common/enums/media-type.enum';
import { GqlAuthGuard } from '../../common/guards/gql-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';

@Resolver(() => FavoriteObject)
export class FavoriteResolver {
  constructor(private readonly favoriteService: FavoriteService) {}

  @Query(() => PaginatedFavoritesObject)
  favoritesByUser(
    @Args('userId') userId: string,
    @Args('pagination', { nullable: true }) pagination?: PaginationInput,
  ) {
    return this.favoriteService.findFavoritesByUser(userId, pagination);
  }

  @Query(() => Boolean)
  isFavorite(
    @Args('userId') userId: string,
    @Args('tmdbId', { type: () => Int }) tmdbId: number,
    @Args('mediaType', { type: () => MediaType }) mediaType: MediaType,
  ) {
    return this.favoriteService.isFavorite(userId, tmdbId, mediaType);
  }

  @Mutation(() => FavoriteObject)
  @UseGuards(GqlAuthGuard)
  addFavorite(
    @Args('input') input: FavoriteInput,
    @CurrentUser() userId: string,
  ) {
    return this.favoriteService.addFavorite(input, userId);
  }

  @Mutation(() => Boolean)
  @UseGuards(GqlAuthGuard)
  removeFavorite(
    @Args('input') input: FavoriteInput,
    @CurrentUser() userId: string,
  ) {
    return this.favoriteService.removeFavorite(input, userId);
  }
}
