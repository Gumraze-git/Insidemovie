import * as React from "react";
import { useEffect, useMemo, useState } from "react";
import InputField from "../../../components/InputField";
import Button from "../../../components/Button";
import TransparentBox from "../../../components/TransparentBox";
import { useNavigate } from "react-router-dom";
import Logo from "@assets/insidemovie_white.png";
import KakaoIcon from "@assets/kakao.png";
import { memberApi } from "../../../api/memberApi";
import { ConfirmDialog } from "../../../components/ConfirmDialog";
import { getProblemMessage } from "../../../utils/problemDetail";
import {
    type DemoAccountOption,
    type DemoAccountsApiResponse,
} from "../../../types/demoAccount";

const Login: React.FC = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [message, setMessage] = useState("");
    const [demoAccounts, setDemoAccounts] = useState<DemoAccountOption[]>([]);
    const [selectedDemoAccountKey, setSelectedDemoAccountKey] = useState("");
    const [demoLoginVisible, setDemoLoginVisible] = useState(false);
    const [isDemoLoginLoading, setIsDemoLoginLoading] = useState(false);

    const groupedDemoAccounts = useMemo(
        () => ({
            onboarding: demoAccounts.filter(
                (account) => account.category === "ONBOARDING",
            ),
            general: demoAccounts.filter(
                (account) => account.category === "GENERAL",
            ),
        }),
        [demoAccounts],
    );

    useEffect(() => {
        let isCancelled = false;

        const fetchDemoAccounts = async () => {
            try {
                const response =
                    await memberApi().getDemoAccounts();
                const payload = response.data as DemoAccountsApiResponse;
                const accounts = payload.accounts ?? [];
                if (isCancelled || accounts.length === 0) {
                    return;
                }
                setDemoAccounts(accounts);
                setSelectedDemoAccountKey(accounts[0].accountKey);
                setDemoLoginVisible(true);
            } catch {
                if (!isCancelled) {
                    setDemoLoginVisible(false);
                }
            }
        };

        void fetchDemoAccounts();

        return () => {
            isCancelled = true;
        };
    }, []);

    const handleConfirm = () => {
        setIsDialogOpen(false);
    };

    const handleCancel = () => {
        setIsDialogOpen(false);
    };

    const completeLogin = async (authorityFromResponse?: string) => {
        const role =
            authorityFromResponse ??
            (await memberApi().profile()).data.authority;

        window.dispatchEvent(new Event("authChanged"));

        const target = role === "ROLE_ADMIN" ? "/admin" : "/";
        navigate(target, { replace: true });
    };

    const handleLogin = async () => {
        try {
            const response = await memberApi().login({ email, password });
            await completeLogin(response.data.authority);
        } catch (error) {
            setMessage(getProblemMessage(error, "로그인에 실패했습니다."));
            setIsDialogOpen(true);
        }
    };

    const handleDemoLogin = async () => {
        if (!selectedDemoAccountKey) {
            return;
        }

        setIsDemoLoginLoading(true);
        try {
            const response = await memberApi().loginDemo({
                accountKey: selectedDemoAccountKey,
            });
            await completeLogin(response.data.authority);
        } catch (error) {
            setMessage(
                getProblemMessage(error, "임시 계정 로그인에 실패했습니다."),
            );
            setIsDialogOpen(true);
        } finally {
            setIsDemoLoginLoading(false);
        }
    };

    const handleKakaoLogin = async () => {
        try {
            const clientId = import.meta.env.VITE_KAKAO_REST_API_KEY;
            const redirectUri = import.meta.env.VITE_KAKAO_REDIRECT_URI;
            window.location.href = `https://kauth.kakao.com/oauth/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code`;
        } catch (error) {
            setMessage(getProblemMessage(error, "카카오 로그인 요청에 실패했습니다."));
            setIsDialogOpen(true);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center">
            <TransparentBox
                className="w-full max-w-[500px] m-5 md:m-0"
                padding="px-8 py-10"
            >
                <img
                    src={Logo}
                    alt="INSIDE MOVIE"
                    className="w-20 mx-auto mb-10"
                />

                <Button
                    text="카카오 로그인"
                    textColor="black"
                    buttonColor="kakao"
                    prefixIcon={KakaoIcon}
                    className="w-full mb-4"
                    onClick={handleKakaoLogin}
                />

                <div className="flex items-center gap-4 text-white text-xs mb-4 opacity-70">
                    <hr className="flex-1 border-grey_200" />
                    <span>OR</span>
                    <hr className="flex-1 border-grey_200" />
                </div>

                <InputField
                    type="email"
                    placeholder="이메일"
                    icon="email"
                    value={email}
                    onChange={setEmail}
                />

                <InputField
                    type="password"
                    placeholder="비밀번호"
                    icon="password"
                    showToggle
                    value={password}
                    onChange={setPassword}
                />

                <div className="flex justify-end items-center text-sm text-white mt-2 mb-8">
                    <button className="text-xs text-movie_sub hover:underline">
                        비밀번호를 잊으셨나요?
                    </button>
                </div>

                <Button
                    text="로그인"
                    textColor="white"
                    buttonColor="default"
                    className="w-full"
                    onClick={handleLogin}
                />

                {demoLoginVisible && (
                    <div className="mt-4">
                        <p className="text-white text-xs opacity-80 mb-2">
                            임시 계정으로 로그인
                        </p>
                        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
                            <select
                                className="flex-1 rounded-full px-4 py-3 text-xs bg-box_bg_white text-white focus:outline-none"
                                value={selectedDemoAccountKey}
                                onChange={(event) =>
                                    setSelectedDemoAccountKey(
                                        event.target.value,
                                    )
                                }
                            >
                                {groupedDemoAccounts.onboarding.length > 0 && (
                                    <optgroup label="온보딩 계정">
                                        {groupedDemoAccounts.onboarding.map(
                                            (account) => (
                                                <option
                                                    key={account.accountKey}
                                                    value={account.accountKey}
                                                >
                                                    {account.label}
                                                </option>
                                            ),
                                        )}
                                    </optgroup>
                                )}
                                {groupedDemoAccounts.general.length > 0 && (
                                    <optgroup label="일반 계정">
                                        {groupedDemoAccounts.general.map(
                                            (account) => (
                                                <option
                                                    key={account.accountKey}
                                                    value={account.accountKey}
                                                >
                                                    {account.label}
                                                </option>
                                            ),
                                        )}
                                    </optgroup>
                                )}
                            </select>
                            <Button
                                text={
                                    isDemoLoginLoading
                                        ? "로그인 중..."
                                        : "로그인"
                                }
                                textColor="white"
                                buttonColor="transparent"
                                className="min-w-24 py-3"
                                disabled={isDemoLoginLoading}
                                onClick={handleDemoLogin}
                            />
                        </div>
                    </div>
                )}

                <p className="text-white text-center text-xs mt-5 opacity-80">
                    계정이 없으신가요?{" "}
                    <span
                        className="text-movie_sub hover:underline cursor-pointer"
                        onClick={() => navigate("/signup")}
                    >
                        회원가입
                    </span>
                </p>
            </TransparentBox>
            <ConfirmDialog
                className={"w-full max-w-md"}
                isOpen={isDialogOpen}
                title="로그인 실패"
                message={message}
                onConfirm={handleConfirm}
                showCancel={false}
                onCancel={handleCancel}
                isRedButton={true}
            />
        </div>
    );
};

export default Login;
