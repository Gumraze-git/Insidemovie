import * as React from "react";
import InputField from "../../../components/InputField";
import Button from "../../../components/Button";
import TransparentBox from "../../../components/TransparentBox";
import BackgroundBubble from "@assets/background_bubble.svg?react";
import SearchIcon from "@assets/search.svg?react";
import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import Logo from "@assets/insidemovie_white.png";
import { memberApi } from "../../../api/memberApi";
import { ConfirmDialog } from "../../../components/ConfirmDialog";
import { movieApi } from "../../../api/movieApi";

import joyIcon from "@assets/character/joy_icon.png";
import sadIcon from "@assets/character/sad_icon.png";
import angryIcon from "@assets/character/angry_icon.png";
import fearIcon from "@assets/character/fear_icon.png";
import disgustIcon from "@assets/character/disgust_icon.png";

import joyProfile from "@assets/profile/joy_profile.png";
import sadProfile from "@assets/profile/sad_profile.png";
import angryProfile from "@assets/profile/angry_profile.png";
import fearProfile from "@assets/profile/fear_profile.png";
import disgustProfile from "@assets/profile/disgust_profile.png";

const emotionMap = {
    joy: joyIcon,
    sad: sadIcon,
    angry: angryIcon,
    fear: fearIcon,
    disgust: disgustIcon,
};

const emotionColorMap = {
    joy: "bg-joy_yellow",
    sad: "bg-sad_blue",
    angry: "bg-angry_red",
    fear: "bg-fear_purple",
    disgust: "bg-disgust_green",
};

const emotionLabelMap: Record<keyof typeof emotionMap, string> = {
    joy: "기쁨",
    sad: "슬픔",
    angry: "버럭",
    fear: "소심",
    disgust: "까칠",
};

const profileImgMap: Record<keyof typeof emotionMap, string> = {
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

    // Dialog
    const [dialogTitle, setDialogTitle] = useState<string>("");
    const [dialogIsRedButton, setDialogIsRedButton] = useState<boolean>(false);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [message, setMessage] = useState("");

    // Search Movie
    const [searchTerm, setSearchTerm] = useState("");
    const [searchResults, setSearchResults] = useState<
        Array<{
            id: number;
            posterPath: string;
            title: string;
            voteAverage: number;
        }>
    >([]);
    const [selectedMovies, setSelectedMovies] = useState<
        Array<{
            id: number;
            posterPath: string;
            title: string;
            voteAverage: number;
        }>
    >([]);

    // 무한 스크롤
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [loadingMovies, setLoadingMovies] = useState(false);
    const resultsRef = useRef<HTMLDivElement | null>(null);
    const previewRef = useRef<HTMLDivElement | null>(null);

    // 선탹한 영화의 감정 평균 값
    const [emotionAverages, setEmotionAverages] = useState<{
        joy: number;
        sad: number;
        angry: number;
        fear: number;
        disgust: number;
    }>({ joy: 0, sad: 0, angry: 0, fear: 0, disgust: 0 });

    // 대표 감정 값
    const [dominantEmotion, setDominantEmotion] =
        useState<keyof typeof emotionLabelMap>("joy");

    // 대표 감정 찾기
    useEffect(() => {
        // 영화 선택 안됨
        if (selectedMovies.length === 0) {
            setDominantEmotion("joy");
            return;
        }
        const entries = Object.entries(emotionAverages) as [
            keyof typeof emotionAverages,
            number,
        ][];
        const values = entries.map(([, v]) => v);
        const maxValue = Math.max(...values);

        // 모두 0인 경우
        if (maxValue === 0) {
            setDominantEmotion("joy");
            return;
        }

        // 대표 감정 값 찾기
        const topEmotions = entries
            .filter(([, v]) => v === maxValue)
            .map(([k]) => k);
        setDominantEmotion(topEmotions[0]);
    }, [emotionAverages, selectedMovies]);

    // 선택된 영화 미리보기 스크롤 자동 이동
    useEffect(() => {
        if (previewRef.current) {
            previewRef.current.scrollTo({
                left: previewRef.current.scrollWidth,
                behavior: "smooth",
            });
        }
    }, [selectedMovies]);

    // 감정 선택 시 마다 평균 감정 계산
    useEffect(() => {
        const fetchEmotions = async () => {
            if (selectedMovies.length === 0) {
                setEmotionAverages({
                    joy: 0,
                    sad: 0,
                    angry: 0,
                    fear: 0,
                    disgust: 0,
                });
                return;
            }
            try {
                // Fetch emotions for all selected movies in parallel
                const promises = selectedMovies.map((movie) =>
                    movieApi().getMovieEmotions({ movieId: movie.id }),
                );
                const responses = await Promise.all(promises);
                // Extract emotion data arrays
                const totals = {
                    joy: 0,
                    anger: 0,
                    sadness: 0,
                    fear: 0,
                    disgust: 0,
                };

                responses.forEach((res) => {
                    const d = res.data.data;
                    totals.joy += Number(d.joy) || 0;
                    totals.anger += Number(d.anger) || 0;
                    totals.sadness += Number(d.sadness) || 0;
                    totals.fear += Number(d.fear) || 0;
                    totals.disgust += Number(d.disgust) || 0;
                });
                const count = selectedMovies.length || 1; // 0 나누기 방지
                setEmotionAverages({
                    joy: totals.joy / count,
                    angry: totals.anger / count, // 또는 field 이름에 맞춰 상태에 저장
                    sad: totals.sadness / count,
                    fear: totals.fear / count,
                    disgust: totals.disgust / count,
                });
            } catch (err) {
                console.error("Emotion fetch error", err);
            }
        };
        fetchEmotions();
    }, [selectedMovies]);

    // 이메일 확인
    const validateEmail = (email: string) => {
        const trimmed = email.trim();
        const regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        return regex.test(trimmed);
    };

    // 비밀번호 확인
    const validatePassword = (password: string) => {
        const trimmed = password.trim();
        const regex =
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+[\]{};':",.<>?]).{8,}$/;
        return regex.test(trimmed);
    };

    // 이메일 필드
    const handleEmailChange = (value: string) => {
        setEmail(value);
        setEmailError(
            validateEmail(value) ? "" : "올바른 이메일 형식이 아닙니다.",
        );
    };

    // 이메일 인증 요청
    const handleSendAuthCode = () => {
        memberApi()
            .emailAuth({ mail: email })
            .then(() => {
                setEmailCheckError("");
                setEmailSent(true);
            })
            .catch((error) => {
                setEmailCheckError(error.response?.data?.message);
            });
    };

    // 인증번호 입력 처리
    const handleCodeChange = (value: string) => {
        setEmailCheck(value);
        if (emailCheckError) setEmailCheckError("");
    };

    // 비밀번호 필드
    const handlePasswordChange = (value: string) => {
        setPassword(value);
        setPasswordError(
            validatePassword(value)
                ? ""
                : "영문 대소문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.",
        );
        // Also validate password confirmation again
        setPasswordConfirmError(
            value === passwordConfirm ? "" : "비밀번호가 일치하지 않습니다.",
        );
    };

    // 비밀번호 확인 필드
    const handlePasswordConfirmChange = (value: string) => {
        setPasswordConfirm(value);
        setPasswordConfirmError(
            value === password ? "" : "비밀번호가 일치하지 않습니다.",
        );
    };

    // 닉네임 필드
    const handleNicknameChange = (value: string) => {
        setNickname(value);

        // 길이 검증
        if (value.length < 2 || value.length > 20) {
            setNicknameError("닉네임은 2~20자 이내여야 합니다.");
            return;
        }

        // 중복 확인 API 호출
        memberApi()
            .checkNickname({ nickname: value })
            .then((res) => {
                const { duplicated } = res.data.data;
                if (duplicated) setNicknameError("이미 사용중인 닉네임입니다.");
                else setNicknameError("");
            })
            .catch((error) => {
                setNicknameError(error.response?.data?.message);
            });
    };

    // Step 1 Check
    const isStep1Disabled =
        !email ||
        !password ||
        !passwordConfirm ||
        !!emailError ||
        !!passwordError ||
        !!passwordConfirmError;

    // Step 2 Check
    const isStep2Disabled = selectedMovies.length === 0;

    // Step 3 Check
    const isStep3Disabled = !nickname || !!nicknameError;

    // 페이지 초기화
    useEffect(() => {
        setPage(0);
        setHasMore(true);
    }, [searchTerm]);

    // 영화 출력
    useEffect(() => {
        const fetchMovies = async () => {
            setLoadingMovies(true);
            try {
                if (!searchTerm) {
                    // Popular movies: 8 items only
                    const res = await movieApi().getPopularMovies({
                        page: 0,
                        pageSize: 8,
                    });
                    const mapped = res.data.data.results.map((m) => ({
                        id: m.id,
                        posterPath: m.poster_path,
                        title: m.title,
                        voteAverage: m.vote_average,
                    }));
                    setSearchResults(mapped);
                    setHasMore(false);
                } else {
                    // Search with infinite scroll
                    const res = await movieApi().searchTitle({
                        title: searchTerm,
                        page,
                        pageSize: 10,
                    });
                    const { content, last } = res.data.data;
                    setSearchResults((prev) =>
                        page === 0 ? content : [...prev, ...content],
                    );
                    setHasMore(!last);
                }
            } catch (err) {
                console.error(err);
                setHasMore(false);
            } finally {
                setLoadingMovies(false);
            }
        };
        fetchMovies();
    }, [searchTerm, page]);

    // Infinite scroll handler
    const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
        const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
        if (
            scrollHeight - scrollTop <= clientHeight + 50 &&
            hasMore &&
            !loadingMovies
        ) {
            setPage((prev) => prev + 1);
        }
    };

    // 회원가입
    const handleSignup = async () => {
        try {
            // 1) 회원가입
            const response = await memberApi().signup({
                email,
                password,
                checkedPassword: passwordConfirm,
                nickname,
            });
            const { memberId } = response.data.data;

            // 2) 초기 감정 상태 등록
            await memberApi().registerEmotions({
                memberId,
                joy: emotionAverages.joy,
                sadness: emotionAverages.sad,
                anger: emotionAverages.angry,
                fear: emotionAverages.fear,
                disgust: emotionAverages.disgust,
            });

            // 3) 모두 성공 시 로그인 화면으로 이동
            setDialogTitle("회원가입 성공");
            setMessage("회원가입이 정상적으로 완료되었습니다.");
            setDialogIsRedButton(false);
            setIsDialogOpen(true);
        } catch (error) {
            setDialogTitle("회원가입 실패");
            setMessage(
                error.response?.data?.message || "회원가입에 실패했습니다.",
            );
            setDialogIsRedButton(true);
            setIsDialogOpen(true);
        }
    };

    return (
        <div className="min-h-screen flex justify-center items-center px-4">
            <div className="max-w-screen-xl w-full flex flex-col md:flex-row">
                {/* Left side with logo and background */}
                <div className="w-full md:w-1/2 bg-cover bg-center relative flex items-center justify-center">
                    <BackgroundBubble className="absolute h-fit w-fit" />
                    <img src={Logo} alt="INSIDE MOVIE" className="w-56 z-10" />
                </div>

                {/* Right side with signup form */}
                <div className="w-full md:w-1/2 flex items-center justify-center">
                    <TransparentBox
                        className="w-[500px] h-fit flex flex-col justify-center items-center"
                        padding="px-8 py-10"
                    >
                        {/* === Signup Step 1: 이메일, 비밀번호 입력 === */}
                        {/* Step 1 UI */}
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
                                                error.response?.data?.message,
                                            );
                                        }
                                    }}
                                />
                            </div>
                        )}

                        {/* Step 2 UI */}
                        {/* TODO : 영화 박스오피스 Top 8 조회 후 리스트업 / 검색 시 검색한 영화 목록 리스트업 / 영화 눌러 최소 1개 선택 / 최소 1개 선택 후 버튼 활성화 */}
                        {step === 2 && (
                            <div className="w-full transition-all duration-300">
                                {/* Emotion averages display */}
                                <div className="bg-box_bg_white px-4 py-2 rounded-3xl mb-6 ring-2 ring-purple-500 ring-opacity-50 shadow-[0_0_10px_rgba(124,106,255,1.0)] transition-shadow duration-300">
                                    <div className="flex gap-2 items-center justify-center">
                                        {[
                                            {
                                                icon: "joy",
                                                value: emotionAverages.joy,
                                            },
                                            {
                                                icon: "sad",
                                                value: emotionAverages.sad,
                                            },
                                            {
                                                icon: "angry",
                                                value: emotionAverages.angry,
                                            },
                                            {
                                                icon: "fear",
                                                value: emotionAverages.fear,
                                            },
                                            {
                                                icon: "disgust",
                                                value: emotionAverages.disgust,
                                            },
                                        ].map((e, i) => (
                                            <div
                                                key={i}
                                                className="flex flex-1 items-center gap-1"
                                            >
                                                <img
                                                    src={emotionMap[e.icon]}
                                                    alt={e.icon}
                                                    className="w-4 h-4"
                                                />
                                                <div
                                                    className="h-2 w-full rounded-full bg-box_bg_white overflow-hidden"
                                                    style={{
                                                        width: `${Math.round(e.value * 100)}%`,
                                                    }}
                                                >
                                                    <div
                                                        className={`h-full rounded-full ${emotionColorMap[e.icon]}`}
                                                    />
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                                <div className="flex justify-between items-center mb-2 px-1">
                                    <div className="text-white font-semibold text-xl">
                                        좋아하는 영화를 골라주세요!
                                    </div>
                                </div>
                                <div className="text-white font-light mb-5 text-sm px-1">
                                    당신의 감정 취향을 분석해 맞춤 영화를
                                    찾아드릴게요.
                                </div>

                                <div className="w-full mb-6">
                                    <div className="relative">
                                        <input
                                            type="text"
                                            value={searchTerm}
                                            onChange={(e) =>
                                                setSearchTerm(e.target.value)
                                            }
                                            placeholder="영화를 검색하세요."
                                            className="w-full py-4 px-5 pr-12 rounded-full bg-box_bg_white text-white text-sm font-light placeholder-white placeholder-opacity-60 focus:outline-none transition-shadow duration-300"
                                        />
                                        <div className="absolute top-1/2 right-4 transform -translate-y-1/2 text-white">
                                            <SearchIcon className="w-7 h-7 opacity-40" />
                                        </div>
                                    </div>
                                </div>
                                {/* 선택된 영화 미리보기 */}
                                <div
                                    className="flex space-x-2 mb-4 overflow-x-auto hide-scrollbar"
                                    style={{
                                        msOverflowStyle: "none",
                                        scrollbarWidth: "none",
                                    }}
                                    ref={previewRef}
                                >
                                    {selectedMovies.map((movie) => (
                                        <div
                                            key={movie.id}
                                            className="relative flex-shrink-0"
                                        >
                                            <img
                                                src={movie.posterPath}
                                                alt={movie.title}
                                                className="w-12 h-16 rounded"
                                            />
                                            <button
                                                type="button"
                                                onClick={() =>
                                                    setSelectedMovies((prev) =>
                                                        prev.filter(
                                                            (m) =>
                                                                m.id !==
                                                                movie.id,
                                                        ),
                                                    )
                                                }
                                                className="absolute -top-1 -right-1 bg-black bg-opacity-50 text-white w-4 h-4 flex items-center justify-center rounded-full"
                                            >
                                                ×
                                            </button>
                                        </div>
                                    ))}
                                </div>
                                <div
                                    className="grid grid-cols-4 gap-4 mb-8 h-64 overflow-y-auto bg-box_bg_white p-2 rounded-3xl hide-scrollbar"
                                    style={{
                                        msOverflowStyle: "none",
                                        scrollbarWidth: "none",
                                    }}
                                    ref={resultsRef}
                                    onScroll={handleScroll}
                                >
                                    {searchResults.map((movie) => {
                                        const isSelected = selectedMovies.some(
                                            (m) => m.id === movie.id,
                                        );
                                        return (
                                            <div
                                                key={movie.id}
                                                onClick={() => {
                                                    setSelectedMovies((prev) =>
                                                        isSelected
                                                            ? prev.filter(
                                                                  (m) =>
                                                                      m.id !==
                                                                      movie.id,
                                                              )
                                                            : [...prev, movie],
                                                    );
                                                }}
                                                className={`cursor-pointer rounded border-2 h-48 flex flex-col items-center justify-start ${
                                                    isSelected
                                                        ? "border-movie_point"
                                                        : "border-transparent"
                                                }`}
                                            >
                                                <img
                                                    src={movie.posterPath}
                                                    alt={movie.title}
                                                    className="w-full object-contain"
                                                />
                                                <p className="text-white text-center mt-1 text-sm line-clamp-2 overflow-hidden">
                                                    {movie.title}
                                                </p>
                                            </div>
                                        );
                                    })}
                                    {/* Optionally, loading indicator */}
                                    {loadingMovies && (
                                        <div className="col-span-4 text-center text-xs text-gray-400 py-2">
                                            불러오는 중...
                                        </div>
                                    )}
                                </div>
                                <Button
                                    text="선택완료"
                                    textColor="white"
                                    buttonColor={
                                        isStep2Disabled ? "disabled" : "default"
                                    }
                                    disabled={isStep2Disabled}
                                    className="w-full"
                                    onClick={() => setStep(3)}
                                />
                            </div>
                        )}

                        {/* Step 3 UI */}
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
                    // 성공 시 로그인으로, 실패 시 다이얼로그만 닫기
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
