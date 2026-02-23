import axios, { type AxiosError, type InternalAxiosRequestConfig } from "axios";

const baseURL = "";

const axiosInstance = axios.create({
    baseURL,
    withCredentials: true,
});

type RetryableRequestConfig = InternalAxiosRequestConfig & {
    _retry?: boolean;
};

let refreshPromise: Promise<void> | null = null;

const refreshSession = async (): Promise<void> => {
    await axios.post(`${baseURL}/api/v1/auth/sessions/refresh`, null, {
        withCredentials: true,
    });
};

axiosInstance.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const status = error.response?.status;
        const originalRequest = error.config as RetryableRequestConfig | undefined;

        if (!originalRequest || status !== 401 || originalRequest._retry) {
            return Promise.reject(error);
        }

        const requestUrl = originalRequest.url ?? "";
        if (requestUrl.includes("/api/v1/auth/sessions")) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        if (!refreshPromise) {
            refreshPromise = refreshSession().finally(() => {
                refreshPromise = null;
            });
        }

        try {
            await refreshPromise;
            return axiosInstance(originalRequest);
        } catch (refreshError) {
            return Promise.reject(refreshError);
        }
    },
);

export default axiosInstance;
