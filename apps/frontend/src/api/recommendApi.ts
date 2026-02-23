import axios from "./axiosInstance";

export const recommendApi = () => {
    const getRecommendMovie = async ({
        joy,
        anger,
        fear,
        disgust,
        sadness,
    }) => {
        return await axios.post("/api/v1/movie-recommendations/by-emotion", {
            joy,
            anger,
            fear,
            disgust,
            sadness,
        });
    };

    return { getRecommendMovie };
};
