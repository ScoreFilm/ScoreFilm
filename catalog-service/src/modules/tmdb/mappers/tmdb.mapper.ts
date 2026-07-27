import { Genre } from '../entities/genre.entity';
import { Movie } from '../entities/movie.entity';
import { Tv } from '../entities/tv.entity';

export function toGenre(raw: any): Genre {
  return {
    id: raw.id,
    name: raw.name,
  };
}

export function toMovie(raw: any): Movie {
  return {
    id: raw.id,
    title: raw.title,
    overview: raw.overview,
    posterPath: raw.poster_path ?? null,
    backdropPath: raw.backdrop_path ?? null,
    releaseDate: raw.release_date ?? null,
    // List endpoints only include genre IDs, so names are unavailable there.
    genres: raw.genre_ids
      ? raw.genre_ids.map((id: number) => ({ id, name: '' }))
      : (raw.genres ?? []).map(toGenre),
    voteAverage: raw.vote_average,
    runtime: raw.runtime ?? null,
    originalLanguage: raw.original_language,
    adult: raw.adult,
  };
}

export function toTv(raw: any): Tv {
  return {
    id: raw.id,
    name: raw.name,
    overview: raw.overview,
    posterPath: raw.poster_path ?? null,
    firstAirDate: raw.first_air_date ?? null,
    // List endpoints only include genre IDs, so names are unavailable there.
    genres: raw.genre_ids
      ? raw.genre_ids.map((id: number) => ({ id, name: '' }))
      : raw.genres.map(toGenre),
    voteAverage: raw.vote_average,
  };
}

export function toSearchResult(raw: any): Movie | Tv | null {
  if (raw.media_type === 'person') {
    return null;
  }
  return raw.title ? toMovie(raw) : toTv(raw);
}

export function toPagedResponse<T>(
  raw: any,
  itemMapper: (item: any) => T | null,
): { page: number; totalPages: number; totalResults: number; results: T[] } {
  return {
    page: raw.page,
    totalPages: raw.total_pages,
    totalResults: raw.total_results,
    results: raw.results.map(itemMapper).filter((item: T | null): item is T => item !== null),
  };
}
