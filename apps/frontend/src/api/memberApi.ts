import axios from "./axiosInstance";

export const memberApi = () => {
    // 이메일 로그인
    const login = async ({ email, password }) => {
        return await axios.post("/api/v1/member/login", {
            email,
            password,
        });
    };

    // 이메일 회원가입
    const signup = async ({ email, password, checkedPassword, nickname }) => {
        return await axios.post("/api/v1/member/signup", {
            email,
            password,
            checkedPassword,
            nickname,
        });
    };

    // 카카오 회원가입
    const signupKakao = async ({ accessToken, nickname }) => {
        return await axios.post("/api/v1/member/kakao-signup", {
            accessToken,
            nickname,
        });
    };

    // 이메일 인증
    const emailAuth = async ({ mail }) => {
        return await axios.post("/api/v1/mail/send", null, {
            params: { mail },
        });
    };

    // 인증번호 확인
    const checkAuthNumber = async ({ mail, code }) => {
        return await axios.get("/api/v1/mail/check", {
            params: { mail, code },
        });
    };

    // 닉네임 중복 확인
    const checkNickname = async ({ nickname }) => {
        return await axios.get("/api/v1/member/check-nickname", {
            params: {
                nickname,
            },
        });
    };

    // 닉네임 변경
    const changeNickname = async ({ nickname }) => {
        return await axios.put("/api/v1/member/nickname", {
            nickname,
        });
    };

    // 비밀번호 변경
    const changePassword = async ({
        password,
        newPassword,
        confirmNewPassword,
    }) => {
        return await axios.put("/api/v1/member/password", {
            password,
            newPassword,
            confirmNewPassword,
        });
    };

    // 로그아웃
    const logout = async () => {
        return await axios.post("/api/v1/member/logout");
    };

    // 사용자 정보 조회
    const profile = async () => {
        return await axios.get("/api/v1/member/profile");
    };

    // 초기 사용자의 감정 상태 등록
    const registerEmotions = async ({
        memberId,
        joy,
        sadness,
        fear,
        anger,
        disgust,
    }) => {
        return await axios.post("/api/v1/member/signup/emotion", {
            memberId,
            joy,
            sadness,
            fear,
            anger,
            disgust,
        });
    };

    // 감정 상태 수정
    const editEmotions = async ({
        joy,
        sadness,
        fear,
        anger,
        disgust,
        repEmotion,
    }) => {
        return await axios.patch("/api/v1/member/emotion/update", {
            joy,
            sadness,
            fear,
            anger,
            disgust,
            repEmotion,
        });
    };

    // 나의 감정 평균 조회
    const getMyAverageEmotions = async () => {
        return await axios.get("/api/v1/member/emotion-summary");
    };

    // 내가 좋아요 한 영화 목록 조회
    const getMyLikedMovies = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/member/my-movie", {
            params: {
                page,
                pageSize,
            },
        });
    };

    // 내가 관람 한 영화 목록 조회
    const getMyVisitedMovies = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/member/my-watch-movie", {
            params: {
                page,
                pageSize,
            },
        });
    };

    // 내가 작성 한 리뷰 목록 조회
    const getMyReviews = async ({ page, pageSize }) => {
        return await axios.get("/api/v1/member/my-review", {
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
