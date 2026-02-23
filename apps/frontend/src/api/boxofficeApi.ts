import axios from "./axiosInstance";

export const boxofficeApi = () => {
    const getWeeklyBoxOffice = async () => {
        return await axios.get("/api/v1/boxoffice/weekly");
    };

    const getWeeklyBoxOfficeDetail = async ({ movieId, weekGb = "0" }) => {
        return await axios.get(`/api/v1/boxoffice/weekly/movies/${movieId}`, {
            params: { weekGb },
        });
    };

    const getDailyBoxOffice = async () => {
        return await axios.get("/api/v1/boxoffice/daily");
    };

    const getDailyBoxOfficeDetail = async ({ movieId }) => {
        return await axios.get(`/api/v1/boxoffice/daily/movies/${movieId}`);
    };

    return {
        getWeeklyBoxOffice,
        getWeeklyBoxOfficeDetail,
        getDailyBoxOffice,
        getDailyBoxOfficeDetail,
    };
};
