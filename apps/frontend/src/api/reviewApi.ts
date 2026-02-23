import axios from "./axiosInstance";

export const reviewApi = () => {
    const getReviewList = async ({ movieId, sort, page, size }) => {
        return await axios.get(`/api/v1/movies/${movieId}/reviews`, {
            params: {
                movieId,
                sort,
                page,
                size,
            },
        });
    };

    const getMyReview = async ({ movieId }) => {
        return await axios.get(`/api/v1/movies/${movieId}/reviews/mine`);
    };

    const likeReview = async ({ reviewId }) => {
        return await axios.put(`/api/v1/reviews/${reviewId}/likes/me`);
    };

    const unlikeReview = async ({ reviewId }) => {
        return await axios.delete(`/api/v1/reviews/${reviewId}/likes/me`);
    };

    const createReview = async ({
        movieId,
        content,
        rating,
        spoiler,
        watchedAt,
    }) => {
        return await axios.post(`/api/v1/movies/${movieId}/reviews`, {
            content,
            rating,
            spoiler,
            watchedAt,
        });
    };

    const modifyReview = async ({
        reviewId,
        content,
        rating,
        spoiler,
        watchedAt,
    }) => {
        return await axios.patch(`/api/v1/reviews/${reviewId}`, {
            content,
            rating,
            spoiler,
            watchedAt,
        });
    };

    const deleteReview = async ({ reviewId }) => {
        return await axios.delete(`/api/v1/reviews/${reviewId}`);
    };

    const reportReview = async ({ reviewId, reason }) => {
        return await axios.post(`/api/v1/reviews/${reviewId}/reports`, {
            reason,
        });
    };

    return {
        getReviewList,
        getMyReview,
        likeReview,
        unlikeReview,
        createReview,
        modifyReview,
        deleteReview,
        reportReview,
    };
};
