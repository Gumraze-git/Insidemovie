import * as React from "react";
import { useState } from "react";
import InputField from "../../../components/InputField";
import Button from "../../../components/Button";
import TransparentBox from "../../../components/TransparentBox";
import BackgroundBubble from "@assets/background_bubble.svg?react";
import { useNavigate } from "react-router-dom";
import Logo from "@assets/insidemovie_white.png";
import { memberApi } from "../../../api/memberApi";
import { ConfirmDialog } from "../../../components/ConfirmDialog";
import { getProblemMessage } from "../../../utils/problemDetail";
import MovieEmotionStep, {
    type EmotionAverages,
    type EmotionKey,
} from "../../../components/onboarding/MovieEmotionStep";

import joyProfile from "@assets/profile/joy_profile.png";
import sadProfile from "@assets/profile/sad_profile.png";
import angryProfile from "@assets/profile/angry_profile.png";
import fearProfile from "@assets/profile/fear_profile.png";
import disgustProfile from "@assets/profile/disgust_profile.png";

const emotionColorMap: Record<EmotionKey, string> = {
    joy: "bg-joy_yellow",
    sad: "bg-sad_blue",
    angry: "bg-angry_red",
    fear: "bg-fear_purple",
    disgust: "bg-disgust_green",
};

const emotionLabelMap: Record<EmotionKey, string> = {
    joy: "기쁨",
    sad: "슬픔",
    angry: "버럭",
    fear: "소심",
    disgust: "까칠",
};

const profileImgMap: Record<EmotionKey, string> = {
    joy: joyProfile,
    sad: sadProfile,
    angry: angryProfile,
    fear: fearProfile,
    disgust: disgustProfile,
};

const Signup: React.FC = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState("");
    const [emailCheck, setEmailCheck] = useState("");
    const [password, setPassword] = useState("");
    const [passwordConfirm, setPasswordConfirm] = useState("");
    const [nickname, setNickname] = useState("");
    const [emailError, setEmailError] = useState("");
    const [emailCheckError, setEmailCheckError] = useState("");
    const [emailSent, setEmailSent] = useState(false);
    const [passwordError, setPasswordError] = useState("");
    const [passwordConfirmError, setPasswordConfirmError] = useState("");
    const [nicknameError, setNicknameError] = useState("");

    const [dialogTitle, setDialogTitle] = useState<string>("");
    const [dialogIsRedButton, setDialogIsRedButton] = useState<boolean>(false);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [message, setMessage] = useState("");

    const [emotionAverages, setEmotionAverages] = useState<EmotionAverages>({
        joy: 0,
        sad: 0,
        angry: 0,
        fear: 0,
        disgust: 0,
    });
    const [dominantEmotion, setDominantEmotion] = useState<EmotionKey>("joy");

    const validateEmail = (value: string) => {
        const trimmed = value.trim();
        const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        return regex.test(trimmed);
    };

    const validatePassword = (value: string) => {
        const trimmed = value.trim();
        const regex =
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+[\]{};':",.<>?]).{8,}$/;
        return regex.test(trimmed);
    };

    const handleEmailChange = (value: string) => {
        setEmail(value);
        setEmailError(
            validateEmail(value) ? "" : "올바른 이메일 형식이 아닙니다.",
        );
    };

    const handleSendAuthCode = () => {
        memberApi()
            .emailAuth({ mail: email })
            .then(() => {
                setEmailCheckError("");
                setEmailSent(true);
            })
            .catch((error) => {
                setEmailCheckError(
                    getProblemMessage(error, "이메일 인증 요청에 실패했습니다."),
                );
            });
    };

    const handleCodeChange = (value: string) => {
        setEmailCheck(value);
        if (emailCheckError) {
            setEmailCheckError("");
        }
    };

    const handlePasswordChange = (value: string) => {
        setPassword(value);
        setPasswordError(
            validatePassword(value)
                ? ""
                : "영문 대소문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.",
        );
        setPasswordConfirmError(
            value === passwordConfirm ? "" : "비밀번호가 일치하지 않습니다.",
        );
    };

    const handlePasswordConfirmChange = (value: string) => {
        setPasswordConfirm(value);
        setPasswordConfirmError(
            value === password ? "" : "비밀번호가 일치하지 않습니다.",
        );
    };

    const handleNicknameChange = (value: string) => {
        setNickname(value);
        if (value.length < 2 || value.length > 20) {
            setNicknameError("닉네임은 2~20자 이내여야 합니다.");
            return;
        }

        memberApi()
            .checkNickname({ nickname: value })
            .then((res) => {
                const { duplicated } = res.data;
                setNicknameError(duplicated ? "이미 사용중인 닉네임입니다." : "");
            })
            .catch((error) => {
                setNicknameError(
                    getProblemMessage(error, "닉네임 검증에 실패했습니다."),
                );
            });
    };

    const isStep3Disabled = !nickname || !!nicknameError;

    const handleSignup = async () => {
        try {
            const response = await memberApi().signup({
                email,
                password,
                checkedPassword: passwordConfirm,
                nickname,
            });
            const { userId } = response.data;

            await memberApi().registerEmotions({
                userId,
                joy: emotionAverages.joy,
                sadness: emotionAverages.sad,
                anger: emotionAverages.angry,
                fear: emotionAverages.fear,
                disgust: emotionAverages.disgust,
            });

            setDialogTitle("회원가입 성공");
            setMessage("회원가입이 정상적으로 완료되었습니다.");
            setDialogIsRedButton(false);
            setIsDialogOpen(true);
        } catch (error) {
            setDialogTitle("회원가입 실패");
            setMessage(getProblemMessage(error, "회원가입에 실패했습니다."));
            setDialogIsRedButton(true);
            setIsDialogOpen(true);
        }
    };

    return (
        <div className="min-h-screen flex justify-center items-center px-4">
            <div className="max-w-screen-xl w-full flex flex-col md:flex-row">
                <div className="w-full md:w-1/2 bg-cover bg-center relative flex items-center justify-center">
                    <BackgroundBubble className="absolute h-fit w-fit" />
                    <img src={Logo} alt="INSIDE MOVIE" className="w-56 z-10" />
                </div>

                <div className="w-full md:w-1/2 flex items-center justify-center">
                    <TransparentBox
                        className="w-[500px] h-fit flex flex-col justify-center items-center"
                        padding="px-8 py-10"
                    >
                        {step === 1 && (
                            <div className="w-full">
                                <p className="text-white font-light text-xl mb-20 w-full">
                                    <span className="text-movie_point font-bold">
                                        인사이드무비
                                    </span>
                                    에 오신 것을 환영합니다.
                                </p>
                                <div className="flex items-center">
                                    <InputField
                                        type="email"
                                        placeholder="이메일"
                                        icon="email"
                                        value={email}
                                        onChange={handleEmailChange}
                                        disabled={emailSent}
                                        isError={true}
                                        error={emailError}
                                    />
                                    <Button
                                        className="ml-2 w-24 mb-10"
                                        text="인증하기"
                                        onClick={handleSendAuthCode}
                                        disabled={
                                            !email || !!emailError || emailSent
                                        }
                                    />
                                </div>

                                <InputField
                                    type="text"
                                    placeholder="인증 번호"
                                    icon="email"
                                    value={emailCheck}
                                    onChange={handleCodeChange}
                                    isError={true}
                                    error={emailCheckError}
                                    disabled={!emailSent}
                                />
                                <InputField
                                    type="password"
                                    placeholder="비밀번호"
                                    icon="password"
                                    showToggle
                                    value={password}
                                    onChange={handlePasswordChange}
                                    isError={true}
                                    error={passwordError}
                                />
                                <InputField
                                    type="password"
                                    placeholder="비밀번호 확인"
                                    icon="password"
                                    showToggle
                                    value={passwordConfirm}
                                    onChange={handlePasswordConfirmChange}
                                    isError={true}
                                    error={passwordConfirmError}
                                />
                                <Button
                                    text="다음"
                                    textColor="white"
                                    buttonColor="default"
                                    className="w-full mt-10"
                                    disabled={
                                        !emailSent ||
                                        !emailCheck ||
                                        !!emailCheckError
                                    }
                                    onClick={async () => {
                                        try {
                                            await memberApi().checkAuthNumber({
                                                mail: email,
                                                code: emailCheck,
                                            });
                                            setEmailCheckError("");
                                            setStep(2);
                                        } catch (error) {
                                            setEmailCheckError(
                                                getProblemMessage(
                                                    error,
                                                    "인증번호 확인에 실패했습니다.",
                                                ),
                                            );
                                        }
                                    }}
                                />
                            </div>
                        )}

                        {step === 2 && (
                            <MovieEmotionStep
                                onComplete={({ emotionAverages, dominantEmotion }) => {
                                    setEmotionAverages(emotionAverages);
                                    setDominantEmotion(dominantEmotion);
                                    setStep(3);
                                }}
                            />
                        )}

                        {step === 3 && (
                            <div className="w-full">
                                <div className="text-white text-xl mb-6 w-full">
                                    <span className="text-white font-bold">
                                        당신의 감정은{" "}
                                    </span>
                                    <span
                                        className={`${emotionColorMap[dominantEmotion]} font-bold`}
                                    >
                                        {emotionLabelMap[dominantEmotion]}
                                    </span>
                                    <span className="text-white">이네요.</span>
                                    <p className="text-sm mt-1 font-light">
                                        서비스를 이용하기 위한 추가 정보를
                                        입력해주세요.
                                    </p>
                                </div>

                                <div className="bg-box_bg_white w-full rounded-3xl py-5 px-6 flex flex-col items-center gap-3 mb-8">
                                    <img
                                        src={profileImgMap[dominantEmotion]}
                                        alt={`Emotion ${emotionLabelMap[dominantEmotion]}`}
                                        className="w-36 h-36 rounded-full"
                                    />
                                    <p className="text-white text-xs font-light">
                                        이후 마이페이지에서 변경 가능합니다.
                                    </p>
                                </div>

                                <InputField
                                    type="text"
                                    placeholder="닉네임"
                                    icon="nickname"
                                    value={nickname}
                                    onChange={handleNicknameChange}
                                    isError={true}
                                    error={nicknameError}
                                />

                                <Button
                                    text="회원가입 완료"
                                    textColor="white"
                                    buttonColor={
                                        isStep3Disabled ? "disabled" : "default"
                                    }
                                    className="w-full mt-10"
                                    disabled={isStep3Disabled}
                                    onClick={handleSignup}
                                />
                            </div>
                        )}
                    </TransparentBox>
                </div>
            </div>
            <ConfirmDialog
                className={"w-full max-w-md"}
                isOpen={isDialogOpen}
                title={dialogTitle}
                message={message}
                showCancel={false}
                isRedButton={dialogIsRedButton}
                onConfirm={() => {
                    setIsDialogOpen(false);
                    if (dialogTitle === "회원가입 성공") {
                        navigate("/login", { replace: true });
                        window.location.replace("/login");
                    }
                }}
                onCancel={() => setIsDialogOpen(false)}
            />
        </div>
    );
};

export default Signup;
