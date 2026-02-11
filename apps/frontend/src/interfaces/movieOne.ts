export interface MovieOne {
    id: number;
    title: string;
    overview: string;
    posterPath: string;
    backdropPath: string;
    voteAverage: number;
    originalLanguage: string;
    isLike: boolean | null;
    genre: string[];
    actors: string[];
    director: string[];
    ottProviders: string[];
    rating: string;
    releaseDate: string;
    runtime: number;
    status: string;
    titleEn: string;
    ratingAvg: number;
}
