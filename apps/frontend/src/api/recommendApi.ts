import axios from "./axiosInstance";

export const recommendApi = () => {
    // 맞춤 영화 조회
    const getRecommendMovie = async ({
        joy,
        anger,
        fear,
        disgust,
        sadness,
    }) => {
        return await axios.post("/api/v1/recommend/emotion", {
            joy,
            anger,
            fear,
            disgust,
            sadness,
        });
    };

    return { getRecommendMovie };
};
