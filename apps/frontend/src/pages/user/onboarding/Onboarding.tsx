import * as React from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import TransparentBox from "../../../components/TransparentBox";
import Logo from "@assets/insidemovie_white.png";
import MovieEmotionStep from "../../../components/onboarding/MovieEmotionStep";
import { memberApi } from "../../../api/memberApi";
import { ConfirmDialog } from "../../../components/ConfirmDialog";
import { getProblemMessage } from "../../../utils/problemDetail";

const Onboarding: React.FC = () => {
    const navigate = useNavigate();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [message, setMessage] = useState("");

    return (
        <div className="min-h-screen flex items-center justify-center px-4">
            <TransparentBox
                className="w-full max-w-[500px] h-fit"
                padding="px-8 py-10"
            >
                <img
                    src={Logo}
                    alt="INSIDE MOVIE"
                    className="w-20 mx-auto mb-8"
                />
                <p className="text-white text-center mb-6 text-sm opacity-80">
                    감정 취향을 설정하면 추천 정확도가 높아집니다.
                </p>

                <MovieEmotionStep
                    completeButtonText={isSubmitting ? "저장 중..." : "온보딩 완료"}
                    onComplete={async ({ emotionAverages }) => {
                        if (isSubmitting) {
                            return;
                        }
                        setIsSubmitting(true);
                        try {
                            await memberApi().editEmotions({
                                joy: emotionAverages.joy,
                                sadness: emotionAverages.sad,
                                anger: emotionAverages.angry,
                                fear: emotionAverages.fear,
                                disgust: emotionAverages.disgust,
                            });
                            window.dispatchEvent(new Event("authChanged"));
                            window.dispatchEvent(new Event("profileUpdated"));
                            navigate("/", { replace: true });
                        } catch (error) {
                            setMessage(
                                getProblemMessage(
                                    error,
                                    "온보딩 저장에 실패했습니다.",
                                ),
                            );
                            setIsDialogOpen(true);
                        } finally {
                            setIsSubmitting(false);
                        }
                    }}
                />
            </TransparentBox>

            <ConfirmDialog
                className={"w-full max-w-md"}
                isOpen={isDialogOpen}
                title="온보딩 실패"
                message={message}
                showCancel={false}
                isRedButton={true}
                onConfirm={() => setIsDialogOpen(false)}
                onCancel={() => setIsDialogOpen(false)}
            />
        </div>
    );
};

export default Onboarding;
