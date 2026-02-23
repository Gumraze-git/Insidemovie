import axios from "./axiosInstance";

export const movieApi = () => {
    const searchTitle = async ({ title, page, pageSize }) => {
        return await axios.get("/api/v1/movies/search/title", {
            params: {
                title,
                page,
                pageSize,
            },
        });
    };

    const getPopularMoviesByGenre = async ({ genre, page, pageSize }) => {
        return await axios.get("/api/v1/movies/recommend/popular", {
            params: {
                genre,
                page,
                pageSize,
            },
        });
    };

    const getLatestMoviesByGenre = async ({ genre, page, pageSize }) => {
        return await axios.get("/api/v1/movies/recommend/latest", {
            params: {
                genre,
                page,
                pageSize,
            },
        });
    };

    const getPopularMovies = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/movies/popular", {
            params: {
                page,
                pageSize,
            },
        });
    };

    const getMovieEmotions = async ({ movieId }) => {
        return await axios.get(`/api/v1/movies/${movieId}/emotions`);
    };

    const getMovieDetail = async ({ movieId }) => {
        return await axios.get(`/api/v1/movies/${movieId}`);
    };

    const likeMovie = async ({ movieId }) => {
        return await axios.put(`/api/v1/movies/${movieId}/likes/me`);
    };

    const unlikeMovie = async ({ movieId }) => {
        return await axios.delete(`/api/v1/movies/${movieId}/likes/me`);
    };

    return {
        searchTitle,
        getPopularMoviesByGenre,
        getLatestMoviesByGenre,
        getPopularMovies,
        getMovieEmotions,
        getMovieDetail,
        likeMovie,
        unlikeMovie,
    };
};
