import { Args, Int, Query, Resolver } from '@nestjs/graphql';
import { TmdbService } from './tmdb.service';
import { Movie } from './entities/movie.entity';
import { Tv } from './entities/tv.entity';
import {
  PaginatedMovies,
  PaginatedTV,
  PaginatedSearchResults,
} from './entities/paged-response.entity';
import { TrendingMediaType } from '../../common/enums/trending-media-type.enum';
import { TimeWindow } from '../../common/enums/time-window.enum';

@Resolver()
export class TmdbResolver {
  constructor(private readonly tmdbService: TmdbService) {}

  @Query(() => Movie)
  movie(@Args('id', { type: () => Int }) id: number) {
    return this.tmdbService.getMovie(id);
  }

  @Query(() => Tv)
  tv(@Args('id', { type: () => Int }) id: number) {
    return this.tmdbService.getTv(id);
  }

  @Query(() => PaginatedSearchResults)
  search(
    @Args('query') query: string,
    @Args('page', { type: () => Int, nullable: true }) page?: number,
  ) {
    return this.tmdbService.search(query, page);
  }

  @Query(() => PaginatedSearchResults)
  trending(
    @Args('mediaType', { type: () => TrendingMediaType })
    mediaType: TrendingMediaType,
    @Args('timeWindow', { type: () => TimeWindow }) timeWindow: TimeWindow,
  ) {
    return this.tmdbService.getTrending(mediaType, timeWindow);
  }

  @Query(() => PaginatedMovies)
  popularMovies(
    @Args('page', { type: () => Int, nullable: true }) page?: number,
  ) {
    return this.tmdbService.getPopularMovies(page);
  }

  @Query(() => PaginatedTV)
  popularTv(@Args('page', { type: () => Int, nullable: true }) page?: number) {
    return this.tmdbService.getPopularTv(page);
  }

  @Query(() => PaginatedMovies)
  topRatedMovies(
    @Args('page', { type: () => Int, nullable: true }) page?: number,
  ) {
    return this.tmdbService.getTopRatedMovies(page);
  }

  @Query(() => PaginatedTV)
  topRatedTv(@Args('page', { type: () => Int, nullable: true }) page?: number) {
    return this.tmdbService.getTopRatedTv(page);
  }
}
