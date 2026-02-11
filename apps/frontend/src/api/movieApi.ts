import axios from "./axiosInstance";

export const movieApi = () => {
    // 영화 타이틀 검색
    const searchTitle = async ({ title, page, pageSize }) => {
        return await axios.get("/api/v1/movies/search/title", {
            params: {
                title,
                page,
                pageSize,
            },
        });
    };

    // 영화 장르별 평점순 추천
    const getPopularMoviesByGenre = async ({ genre, page, pageSize }) => {
        return await axios.get("/api/v1/movies/recommend/popular", {
            params: {
                genre,
                page,
                pageSize,
            },
        });
    };

    // 영화 장르별 최신순 추천
    const getLatestMoviesByGenre = async ({ genre, page, pageSize }) => {
        return await axios.get("/api/v1/movies/recommend/latest", {
            params: {
                genre,
                page,
                pageSize,
            },
        });
    };

    // 인기순 정렬 영화 목록 제공
    const getPopularMovies = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/movies/popular", {
            params: {
                page,
                pageSize,
            },
        });
    };

    // 영화에 저장된 감정 상태 값 조회
    const getMovieEmotions = async ({ movieId }) => {
        return await axios.get(`/api/v1/movies/emotions/${movieId}`);
    };

    // 영화 상세 조회
    const getMovieDetail = async ({ movieId }) => {
        return await axios.get(`/api/v1/movies/detail/${movieId}`);
    };

    // 영화 좋아요 클릭
    const likeMovie = async ({ movieId }) => {
        return await axios.post(`/api/v1/movies/${movieId}/like-movie`);
    };

    return {
        searchTitle,
        getPopularMoviesByGenre,
        getLatestMoviesByGenre,
        getPopularMovies,
        getMovieEmotions,
        getMovieDetail,
        likeMovie,
    };
};
