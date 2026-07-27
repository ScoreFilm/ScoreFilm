import { Injectable, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { AxiosRequestConfig, AxiosResponse, isAxiosError } from 'axios';
import { firstValueFrom } from 'rxjs';
import { TrendingMediaType } from '../../../common/enums/trending-media-type.enum';
import { TimeWindow } from '../../../common/enums/time-window.enum';
import { TmdbNotFoundException } from '../exceptions/tmdb-not-found.exception';
import { TmdbRateLimitException } from '../exceptions/tmdb-rate-limit.exception';
import { TmdbUnauthorizedException } from '../exceptions/tmdb-unauthorized.exception';
import { TmdbUnavailableException } from '../exceptions/tmdb-unavailable.exception';

@Injectable()
export class TmdbClient {
  private readonly logger = new Logger(TmdbClient.name);

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {}

  private getRequestConfig() {
    return {
      headers: {
        Authorization: `Bearer ${this.configService.get<string>(
          'TMDB_READ_ACCESS_TOKEN',
        )}`,
      },
    };
  }

  private async get(url: string): Promise<AxiosResponse<any>> {
    try {
      return await firstValueFrom(
        this.httpService.get(
          url,
          this.getRequestConfig() as AxiosRequestConfig,
        ),
      );
    } catch (error: unknown) {
      this.handleError(error);
    }
  }

  private handleError(error: unknown): never {
    const stack = error instanceof Error ? error.stack : undefined;

    if (!isAxiosError(error)) {
      this.logger.error('TMDB request failed', stack);
      throw new TmdbUnavailableException();
    }

    const status = error.response?.status;

    if (status === 401) {
      this.logger.fatal('TMDB authorization failed', stack);
      throw new TmdbUnauthorizedException();
    }

    this.logger.error('TMDB request failed', stack);

    if (status === 404) {
      throw new TmdbNotFoundException();
    }

    if (status === 429) {
      throw new TmdbRateLimitException();
    }

    throw new TmdbUnavailableException();
  }

  async search(query: string, page = 1): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>(
        'TMDB_BASE_URL',
      )}/search/multi?query=${encodeURIComponent(query)}&page=${page}`,
    );

    return response.data;
  }

  async movieDetails(id: number): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>('TMDB_BASE_URL')}/movie/${id}`,
    );

    return response.data;
  }

  async tvDetails(id: number): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>('TMDB_BASE_URL')}/tv/${id}`,
    );

    return response.data;
  }

  async trending(
    mediaType: TrendingMediaType,
    timeWindow: TimeWindow,
  ): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>(
        'TMDB_BASE_URL',
      )}/trending/${mediaType}/${timeWindow}`,
    );

    return response.data;
  }

  async popularMovies(page = 1): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>(
        'TMDB_BASE_URL',
      )}/movie/popular?page=${page}`,
    );

    return response.data;
  }

  async popularTv(page = 1): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>(
        'TMDB_BASE_URL',
      )}/tv/popular?page=${page}`,
    );

    return response.data;
  }

  async topRatedMovies(page = 1): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>(
        'TMDB_BASE_URL',
      )}/movie/top_rated?page=${page}`,
    );

    return response.data;
  }

  async topRatedTv(page = 1): Promise<any> {
    const response = await this.get(
      `${this.configService.get<string>(
        'TMDB_BASE_URL',
      )}/tv/top_rated?page=${page}`,
    );

    return response.data;
  }
}

export default TmdbClient;
