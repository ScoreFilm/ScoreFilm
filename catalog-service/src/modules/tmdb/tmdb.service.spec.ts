import { Test, TestingModule } from '@nestjs/testing';
import { TmdbClient } from './clients/tmdb.client';
import { TmdbNotFoundException } from './exceptions/tmdb-not-found.exception';
import { TmdbRateLimitException } from './exceptions/tmdb-rate-limit.exception';
import { TmdbUnauthorizedException } from './exceptions/tmdb-unauthorized.exception';
import { TmdbUnavailableException } from './exceptions/tmdb-unavailable.exception';
import { TmdbService } from './tmdb.service';

describe('TmdbService', () => {
  let service: TmdbService;

  const tmdbClient = {
    search: jest.fn(),
    movieDetails: jest.fn(),
    tvDetails: jest.fn(),
    trending: jest.fn(),
    popularMovies: jest.fn(),
    popularTv: jest.fn(),
    topRatedMovies: jest.fn(),
    topRatedTv: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        TmdbService,
        {
          provide: TmdbClient,
          useValue: tmdbClient,
        },
      ],
    }).compile();

    service = module.get<TmdbService>(TmdbService);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('maps search results and excludes people', async () => {
    tmdbClient.search.mockResolvedValue({
      page: 2,
      total_pages: 4,
      total_results: 3,
      results: [
        {
          media_type: 'movie',
          id: 10,
          title: 'Movie title',
          overview: 'Movie overview',
          poster_path: '/movie-poster.jpg',
          backdrop_path: '/movie-backdrop.jpg',
          release_date: '2024-05-01',
          genre_ids: [12, 18],
          vote_average: 8.4,
          original_language: 'en',
          adult: false,
        },
        {
          media_type: 'tv',
          id: 20,
          name: 'TV title',
          overview: 'TV overview',
          poster_path: '/tv-poster.jpg',
          first_air_date: '2023-06-02',
          genre_ids: [35],
          vote_average: 7.5,
        },
        { media_type: 'person', id: 30, name: 'Person name' },
      ],
    });

    await expect(service.search('title', 2)).resolves.toEqual({
      page: 2,
      totalPages: 4,
      totalResults: 3,
      results: [
        {
          id: 10,
          title: 'Movie title',
          overview: 'Movie overview',
          posterPath: '/movie-poster.jpg',
          backdropPath: '/movie-backdrop.jpg',
          releaseDate: '2024-05-01',
          genres: [
            { id: 12, name: '' },
            { id: 18, name: '' },
          ],
          voteAverage: 8.4,
          runtime: null,
          originalLanguage: 'en',
          adult: false,
        },
        {
          id: 20,
          name: 'TV title',
          overview: 'TV overview',
          posterPath: '/tv-poster.jpg',
          firstAirDate: '2023-06-02',
          genres: [{ id: 35, name: '' }],
          voteAverage: 7.5,
        },
      ],
    });
    expect(tmdbClient.search).toHaveBeenCalledWith('title', 2);
  });

  it('maps an empty search response', async () => {
    tmdbClient.search.mockResolvedValue({
      page: 1,
      total_pages: 0,
      total_results: 0,
      results: [],
    });

    await expect(service.search('missing')).resolves.toEqual({
      page: 1,
      totalPages: 0,
      totalResults: 0,
      results: [],
    });
    expect(tmdbClient.search).toHaveBeenCalledWith('missing', 1);
  });

  it('maps movie details to a Movie entity', async () => {
    tmdbClient.movieDetails.mockResolvedValue({
      id: 101,
      title: 'Movie title',
      overview: 'Movie overview',
      poster_path: '/poster.jpg',
      backdrop_path: '/backdrop.jpg',
      release_date: '2024-01-15',
      genres: [{ id: 28, name: 'Action' }],
      vote_average: 8.1,
      runtime: 120,
      original_language: 'es',
      adult: false,
    });

    await expect(service.getMovie(101)).resolves.toEqual({
      id: 101,
      title: 'Movie title',
      overview: 'Movie overview',
      posterPath: '/poster.jpg',
      backdropPath: '/backdrop.jpg',
      releaseDate: '2024-01-15',
      genres: [{ id: 28, name: 'Action' }],
      voteAverage: 8.1,
      runtime: 120,
      originalLanguage: 'es',
      adult: false,
    });
    expect(tmdbClient.movieDetails).toHaveBeenCalledWith(101);
  });

  it('maps TV details to a Tv entity', async () => {
    tmdbClient.tvDetails.mockResolvedValue({
      id: 202,
      name: 'TV title',
      overview: 'TV overview',
      poster_path: '/poster.jpg',
      first_air_date: '2022-09-20',
      genres: [{ id: 18, name: 'Drama' }],
      vote_average: 7.9,
    });

    await expect(service.getTv(202)).resolves.toEqual({
      id: 202,
      name: 'TV title',
      overview: 'TV overview',
      posterPath: '/poster.jpg',
      firstAirDate: '2022-09-20',
      genres: [{ id: 18, name: 'Drama' }],
      voteAverage: 7.9,
    });
    expect(tmdbClient.tvDetails).toHaveBeenCalledWith(202);
  });

  it.each([
    new TmdbNotFoundException(),
    new TmdbUnauthorizedException(),
    new TmdbRateLimitException(),
    new TmdbUnavailableException(),
  ])('propagates %s from the client', async (exception) => {
    tmdbClient.movieDetails.mockRejectedValue(exception);

    await expect(service.getMovie(1)).rejects.toBe(exception);
  });
});
