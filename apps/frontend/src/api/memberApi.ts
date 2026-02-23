import axios from "./axiosInstance";

export const memberApi = () => {
    const login = async ({ email, password }) => {
        return await axios.post("/api/v1/auth/sessions", {
            email,
            password,
        });
    };

    const signup = async ({ email, password, checkedPassword, nickname }) => {
        return await axios.post("/api/v1/users", {
            email,
            password,
            checkedPassword,
            nickname,
        });
    };

    const signupKakao = async ({ accessToken, nickname }) => {
        return await axios.post("/api/v1/users/social/kakao", {
            accessToken,
            nickname,
        });
    };

    const emailAuth = async ({ mail }) => {
        return await axios.post("/api/v1/email-verifications", {
            email: mail,
        });
    };

    const checkAuthNumber = async ({ mail, code }) => {
        return await axios.post("/api/v1/email-verifications/confirm", {
            email: mail,
            code: Number(code),
        });
    };

    const checkNickname = async ({ nickname }) => {
        return await axios.get("/api/v1/users/nickname-availability", {
            params: {
                nickname,
            },
        });
    };

    const changeNickname = async ({ nickname }) => {
        return await axios.patch("/api/v1/users/me/profile", {
            nickname,
        });
    };

    const changePassword = async ({
        password,
        newPassword,
        confirmNewPassword,
    }) => {
        return await axios.put("/api/v1/users/me/password", {
            password,
            newPassword,
            confirmNewPassword,
        });
    };

    const logout = async () => {
        return await axios.delete("/api/v1/auth/sessions/current");
    };

    const profile = async () => {
        return await axios.get("/api/v1/users/me");
    };

    const registerEmotions = async ({
        userId,
        joy,
        sadness,
        fear,
        anger,
        disgust,
    }) => {
        return await axios.post(`/api/v1/users/${userId}/emotion-summary`, {
            joy,
            sadness,
            fear,
            anger,
            disgust,
        });
    };

    const editEmotions = async ({ joy, sadness, fear, anger, disgust }) => {
        return await axios.patch("/api/v1/users/me/emotion-summary", {
            joy,
            sadness,
            fear,
            anger,
            disgust,
        });
    };

    const getMyAverageEmotions = async () => {
        return await axios.get("/api/v1/users/me/emotion-summary");
    };

    const getMyLikedMovies = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/users/me/liked-movies", {
            params: {
                page,
                pageSize,
            },
        });
    };

    const getMyVisitedMovies = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/users/me/watched-movies", {
            params: {
                page,
                pageSize,
            },
        });
    };

    const getMyReviews = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/users/me/reviews", {
            params: {
                page,
                pageSize,
            },
        });
    };

    return {
        login,
        signup,
        signupKakao,
        emailAuth,
        checkAuthNumber,
        checkNickname,
        changeNickname,
        changePassword,
        logout,
        profile,
        registerEmotions,
        getMyAverageEmotions,
        getMyLikedMovies,
        getMyVisitedMovies,
        getMyReviews,
        editEmotions,
    };
};
