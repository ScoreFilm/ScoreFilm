import { UseGuards } from '@nestjs/common';
import { Args, Int, Mutation, Query, Resolver } from '@nestjs/graphql';
import { WatchlistEntryObject } from './entities/watchlist-entry.entity';
import { PaginatedWatchlistObject } from './entities/paginated-watchlist.entity';
import { WatchlistService } from './watchlist.service';
import { WatchlistInput } from './dto/watchlist.input';
import { PaginationInput } from '../../common/dto/pagination.input';
import { MediaType } from '../../common/enums/media-type.enum';
import { GqlAuthGuard } from '../../common/guards/gql-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';

@Resolver(() => WatchlistEntryObject)
export class WatchlistResolver {
  constructor(private readonly watchlistService: WatchlistService) {}

  @Query(() => PaginatedWatchlistObject)
  watchlistByUser(
    @Args('userId') userId: string,
    @Args('pagination', { nullable: true }) pagination?: PaginationInput,
  ) {
    return this.watchlistService.findWatchlistByUser(userId, pagination);
  }

  @Query(() => Boolean)
  isInWatchlist(
    @Args('userId') userId: string,
    @Args('tmdbId', { type: () => Int }) tmdbId: number,
    @Args('mediaType', { type: () => MediaType }) mediaType: MediaType,
  ) {
    return this.watchlistService.isInWatchlist(userId, tmdbId, mediaType);
  }

  @Mutation(() => WatchlistEntryObject)
  @UseGuards(GqlAuthGuard)
  addToWatchlist(
    @Args('input') input: WatchlistInput,
    @CurrentUser() userId: string,
  ) {
    return this.watchlistService.addToWatchlist(input, userId);
  }

  @Mutation(() => Boolean)
  @UseGuards(GqlAuthGuard)
  removeFromWatchlist(
    @Args('input') input: WatchlistInput,
    @CurrentUser() userId: string,
  ) {
    return this.watchlistService.removeFromWatchlist(input, userId);
  }
}
