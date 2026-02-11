import axios from "./axiosInstance";

export const matchApi = () => {
    // 대결 영화 조회
    const getWeeklyMatchMovie = async () => {
        return await axios.get("/api/v1/match/weekly-match");
    };

    // 역대 우승 영화 조회
    const getPastMatchMovie = async () => {
        return await axios.get("/api/v1/match/winners");
    };

    // 영화 대결 투표
    const voteMatch = async ({ movieId }) => {
        return await axios.post(`/api/v1/match/vote/${movieId}`);
    };

    return { getWeeklyMatchMovie, getPastMatchMovie, voteMatch };
};
