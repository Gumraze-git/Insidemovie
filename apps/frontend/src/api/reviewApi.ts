import axios from "./axiosInstance";

export const reviewApi = () => {
    // 리뷰 목록 조회
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

    // 내 리뷰 단건 조회
    const getMyReview = async ({ movieId }) => {
        return await axios.get(`/api/v1/movies/${movieId}/reviews/my-review`);
    };

    // 리뷰 좋아요 토글
    const likeReview = async ({ reviewId }) => {
        return await axios.post(`/api/v1/reviews/${reviewId}/like`);
    };

    // 리뷰 등록
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

    // 리뷰 수정
    const modifyReview = async ({
        reviewId,
        content,
        rating,
        spoiler,
        watchedAt,
    }) => {
        return await axios.put(`/api/v1/reviews/${reviewId}`, {
            content,
            rating,
            spoiler,
            watchedAt,
        });
    };

    // 리뷰 삭제
    const deleteReview = async ({ reviewId }) => {
        return await axios.delete(`/api/v1/reviews/${reviewId}`);
    };

    // 리뷰 신고
    const reportReview = async ({ reviewId, reason }) => {
        return await axios.post(`/api/v1/report/${reviewId}`, null, {
            params: { reason },
        });
    };

    return {
        getReviewList,
        getMyReview,
        likeReview,
        createReview,
        modifyReview,
        deleteReview,
        reportReview,
    };
};
