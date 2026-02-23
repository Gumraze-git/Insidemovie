import React, { useEffect, useState } from "react";
import axios from "../../../api/axiosInstance";
import { ConfirmDialog } from "../../../components/ConfirmDialog";
import { useNavigate } from "react-router-dom";
import { getProblemMessage } from "../../../utils/problemDetail";

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

        if (!code) {
            navigate("/login", { replace: true });
            return;
        }

        axios
            .post("/api/v1/auth/providers/kakao/token-exchanges", { code })
            .then((res) => {
                const kakaoAccessToken = res.data.accessToken;
                signupToken = kakaoAccessToken;

                if (!kakaoAccessToken) {
                    throw new Error("카카오 토큰이 없습니다.");
                }

                return axios.post("/api/v1/auth/providers/kakao/sessions", {
                    accessToken: kakaoAccessToken,
                });
            })
            .then(() => {
                window.dispatchEvent(new Event("authChanged"));
                navigate("/", { replace: true });
            })
            .catch((error) => {
                if (error.response?.status === 400 && signupToken) {
                    navigate("/signup-kakao", {
                        state: { accessToken: signupToken },
                    });
                    return;
                }

                setDialog({
                    isOpen: true,
                    title: "로그인 오류",
                    message: getProblemMessage(
                        error,
                        "카카오 로그인 중 문제가 발생했습니다.",
                    ),
                    showCancel: false,
                    isRedButton: true,
                    onConfirm: () => {
                        setDialog({ isOpen: false });
                        navigate("/login");
                    },
                });
            });
    }, [navigate]);

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
