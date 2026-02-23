import axios from "./axiosInstance";

export const matchApi = () => {
    const getWeeklyMatchMovie = async () => {
        return await axios.get("/api/v1/matches/current");
    };

    const getPastMatchMovie = async () => {
        return await axios.get("/api/v1/matches/winners");
    };

    const voteMatch = async ({ movieId }) => {
        return await axios.post("/api/v1/matches/current/votes", {
            movieId,
        });
    };

    return { getWeeklyMatchMovie, getPastMatchMovie, voteMatch };
};
