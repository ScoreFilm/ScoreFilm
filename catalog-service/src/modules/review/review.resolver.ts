import {
  Args,
  ID,
  Int,
  Mutation,
  Parent,
  Query,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';
import { UseGuards } from '@nestjs/common';
import { ReviewObject } from './entities/review.entity';
import { PaginatedReviewsObject } from './entities/paginated-review.entity';
import { ContentStatsObject } from './entities/content-stats.entity';
import { ReviewService } from './review.service';
import { CreateReviewInput } from './dto/create-review.input';
import { UpdateReviewInput } from './dto/update-review.input';
import { ReviewFilterInput } from './dto/review-filter.input';
import { PaginationInput } from '../../common/dto/pagination.input';
import { ReviewSortBy } from './enums/review-sort.enum';
import { MediaType } from '../../common/enums/media-type.enum';
import { GqlAuthGuard } from '../../common/guards/gql-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { TmdbService } from '../tmdb/tmdb.service';
import { Movie } from '../tmdb/entities/movie.entity';
import { Tv } from '../tmdb/entities/tv.entity';
import { searchResultUnion } from '../tmdb/entities/search-result.entity';

@Resolver(() => ReviewObject)
export class ReviewResolver {
  constructor(
    private readonly reviewService: ReviewService,
    private readonly tmdbService: TmdbService,
  ) {}

  @Query(() => String)
  catalogHealth(): string {
    return 'catalog-service ok';
  }

  @Query(() => ReviewObject)
  findReviewById(@Args('id', { type: () => ID }) id: string) {
    return this.reviewService.findReviewById(id);
  }

  @Query(() => PaginatedReviewsObject)
  reviews(
    @Args('filter', { nullable: true }) filter?: ReviewFilterInput,
    @Args('sortBy', { type: () => ReviewSortBy, nullable: true })
    sortBy?: ReviewSortBy,
    @Args('pagination', { nullable: true }) pagination?: PaginationInput,
  ) {
    return this.reviewService.findReviews(filter, sortBy, pagination);
  }

  @Query(() => ContentStatsObject)
  contentStats(
    @Args('tmdbId', { type: () => Int }) tmdbId: number,
    @Args('mediaType', { type: () => MediaType }) mediaType: MediaType,
  ) {
    return this.reviewService.getContentStats(tmdbId, mediaType);
  }

  @Mutation(() => ReviewObject)
  @UseGuards(GqlAuthGuard)
  createReview(
    @Args('input') input: CreateReviewInput,
    @CurrentUser() userId: string,
  ) {
    return this.reviewService.createReview(input, userId);
  }

  @Mutation(() => ReviewObject)
  @UseGuards(GqlAuthGuard)
  updateReview(
    @Args('id', { type: () => ID }) id: string,
    @Args('input') input: UpdateReviewInput,
    @CurrentUser() userId: string,
  ) {
    return this.reviewService.updateReview(id, input, userId);
  }

  @Mutation(() => Boolean)
  @UseGuards(GqlAuthGuard)
  deleteReview(
    @Args('id', { type: () => ID }) id: string,
    @CurrentUser() userId: string,
  ) {
    return this.reviewService.deleteReview(id, userId);
  }

  @ResolveField(() => searchResultUnion, { nullable: true })
  async movie(@Parent() review: ReviewObject): Promise<Movie | Tv | null> {
    if (review.mediaType === MediaType.MOVIE) {
      return this.tmdbService.getMovie(review.tmdbId);
    }
    if (review.mediaType === MediaType.TV) {
      return this.tmdbService.getTv(review.tmdbId);
    }
    return null;
  }
}
