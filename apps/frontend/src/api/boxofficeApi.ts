import axios from "./axiosInstance";

export const boxofficeApi = () => {
    // 주간 박스오피스 조회
    const getWeeklyBoxOffice = async () => {
        return await axios.get("/api/v1/boxoffice/weekly");
    };

    // 주간 박스오피스 영화 상세 조회
    const getWeeklyBoxOfficeDetail = async () => {
        return await axios.get("/api/v1/boxoffice/weekly/detail");
    };

    // 일간 박스오피스 조회
    const getDailyBoxOffice = async () => {
        return await axios.get("/api/v1/boxoffice/daily");
    };

    // 일간 박스오피스 영화 상세 조회
    const getDailyBoxOfficeDetail = async () => {
        return await axios.get("/api/v1/boxoffice/daily/detail");
    };

    return {
        getWeeklyBoxOffice,
        getWeeklyBoxOfficeDetail,
        getDailyBoxOffice,
        getDailyBoxOfficeDetail,
    };
};
