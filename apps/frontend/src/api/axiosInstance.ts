import axios from "axios";

// const baseURL = "http://localhost:8080";
const baseURL = "http://52.79.175.149:8080";
const axiosInstance = axios.create({
    baseURL: baseURL,
});

const token = localStorage.getItem("refreshToken");

const reissue = async (refreshToken: string | null) => {
    return axios.post(`${baseURL}/api/v1/member/reissue`, null, {
        headers: {
            "Authorization-Refresh": `Bearer ${refreshToken}`,
        },
    });
};

const getToken = async (): Promise<void> => {
    try {
        const res = await reissue(token);
        if (res.data.status !== 200) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
        } else {
            const accessToken: string = res.data.data.accessToken;
            const refreshToken: string = res.data.data.refreshToken;
            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);
        }
    } catch (error) {
        console.error("Error during token reissue:", error);
    }
};

axiosInstance.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");
    if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            console.log(error.response);
        }
        if (error.response?.status === 401) {
            getToken();
        }
        return Promise.reject(error);
    },
);

export default axiosInstance;
