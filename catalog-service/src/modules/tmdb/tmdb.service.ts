import { Injectable } from '@nestjs/common';
import { TmdbClient } from './clients/tmdb.client';
import {
  toMovie,
  toTv,
  toSearchResult,
  toPagedResponse,
} from './mappers/tmdb.mapper';
import { Movie } from './entities/movie.entity';
import { Tv } from './entities/tv.entity';
import { TrendingMediaType } from '../../common/enums/trending-media-type.enum';
import { TimeWindow } from '../../common/enums/time-window.enum';

@Injectable()
export class TmdbService {
  constructor(private readonly tmdbClient: TmdbClient) {}

  async getMovie(id: number): Promise<Movie> {
    const raw = await this.tmdbClient.movieDetails(id);
    return toMovie(raw);
  }

  async getTv(id: number): Promise<Tv> {
    const raw = await this.tmdbClient.tvDetails(id);
    return toTv(raw);
  }

  async search(query: string, page = 1) {
    const raw = await this.tmdbClient.search(query, page);
    return toPagedResponse(raw, toSearchResult);
  }

  async getTrending(mediaType: TrendingMediaType, timeWindow: TimeWindow) {
    const raw = await this.tmdbClient.trending(mediaType, timeWindow);
    return toPagedResponse(raw, toSearchResult);
  }

  async getPopularMovies(page = 1) {
    const raw = await this.tmdbClient.popularMovies(page);
    return toPagedResponse(raw, toMovie);
  }

  async getPopularTv(page = 1) {
    const raw = await this.tmdbClient.popularTv(page);
    return toPagedResponse(raw, toTv);
  }

  async getTopRatedMovies(page = 1) {
    const raw = await this.tmdbClient.topRatedMovies(page);
    return toPagedResponse(raw, toMovie);
  }

  async getTopRatedTv(page = 1) {
    const raw = await this.tmdbClient.topRatedTv(page);
    return toPagedResponse(raw, toTv);
  }
}
