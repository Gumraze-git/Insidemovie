import React, { useEffect, useState } from "react";
import axios from "../../../api/axiosInstance";
import { ConfirmDialog } from "../../../components/ConfirmDialog";
import { useNavigate } from "react-router-dom";

interface DialogState {
    isOpen: boolean;
    title?: string;
    message?: string;
    showCancel?: boolean;
    isRedButton?: boolean;
    onConfirm?: () => void;
    onCancel?: () => void;
}

const KakaoRedirect: React.FC = () => {
    const navigate = useNavigate();
    const [dialog, setDialog] = useState<DialogState>({ isOpen: false });

    useEffect(() => {
        let signupToken: string | null = null;
        const code = new URL(window.location.href).searchParams.get("code");

        if (code) {
            // 인가 코드로 accessToken 받기
            axios
                .get(`/api/v1/member/kakao-accesstoken?code=${code}`)
                .then((res) => {
                    const kakaoAccessToken = res.data.data.accessToken;
                    signupToken = kakaoAccessToken;

                    if (!kakaoAccessToken) {
                        setDialog({
                            isOpen: true,
                            title: "카카오 토큰 없음",
                            message: "카카오 로그인에 실패했습니다.",
                            showCancel: false,
                            isRedButton: true,
                            onConfirm: () => {
                                setDialog({ isOpen: false });
                                navigate("/login");
                            },
                        });
                        return null;
                        // alert("카카오 토큰이 없습니다.");
                        // return navigate("/login");
                    }

                    // 카카오 accessToken을 서버로 보내서 자체 JWT 받기
                    return axios.post("/api/v1/member/kakao-login", {
                        accessToken: kakaoAccessToken,
                    });
                })
                .then((res) => {
                    if (!res) return; // 위에서 오류 발생한 경우

                    const { accessToken, refreshToken } = res.data.data;
                    if (accessToken && refreshToken) {
                        localStorage.setItem("accessToken", accessToken);
                        localStorage.setItem("refreshToken", refreshToken);
                        localStorage.setItem("authority", "ROLE_USER");

                        navigate("/", { replace: true });
                        window.location.replace("/");
                    } else {
                        setDialog({
                            isOpen: true,
                            title: "JWT 발급 실패",
                            message: "다시 로그인해주세요.",
                            showCancel: false,
                            isRedButton: true,
                            onConfirm: () => {
                                setDialog({ isOpen: false });
                                navigate("/login");
                            },
                        });
                        // alert("JWT 토큰 발급 실패");
                        // navigate("/login");
                    }
                })
                .catch((error) => {
                    if (error.response?.status === 400) {
                        // 신규 회원: 추가 정보 입력 페이지로 이동
                        navigate("/signup-kakao", {
                            state: { accessToken: signupToken },
                        });
                        return;
                    } else {
                        setDialog({
                            isOpen: true,
                            title: "로그인 오류",
                            message: "카카오 로그인 중 문제가 발생했습니다.",
                            showCancel: false,
                            isRedButton: true,
                            onConfirm: () => {
                                setDialog({ isOpen: false });
                                navigate("/login");
                            },
                        });
                    }
                    // console.error("카카오 로그인 실패", err);
                    // alert("로그인 중 오류가 발생했습니다.");
                    // navigate("/login");
                });
        }
    }, []);

    return (
        <ConfirmDialog
            className={"w-full max-w-md"}
            isOpen={dialog.isOpen}
            title={dialog.title}
            message={dialog.message}
            showCancel={dialog.showCancel}
            isRedButton={dialog.isRedButton}
            onConfirm={dialog.onConfirm}
            onCancel={dialog.onCancel}
        />
    );
};

export default KakaoRedirect;
