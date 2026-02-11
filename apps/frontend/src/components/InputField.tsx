import * as React from "react";
import { useState } from "react";
import MailIcon from "@assets/mail.svg?react";
import PasswordIcon from "@assets/password.svg?react";
import NicknameIcon from "@assets/nickname.svg?react";
import Visible from "@assets/visibility_on.svg?react";
import Invisible from "@assets/visibility_off.svg?react";

interface InputFieldProps {
    type: "text" | "email" | "password";
    placeholder: string;
    icon?: "email" | "password" | "nickname";
    showToggle?: boolean;
    value?: string;
    onChange?: (value: string) => void;
    isError?: boolean;
    error?: string;
    disabled?: boolean;
}

const InputField: React.FC<InputFieldProps> = ({
    type,
    placeholder,
    icon,
    showToggle = false,
    value,
    onChange,
    isError = false,
    error,
    disabled = false,
}) => {
    const [isVisible, setIsVisible] = useState(false);
    const [internalValue, setInternalValue] = useState("");
    const toggleVisibility = () => setIsVisible((prev) => !prev);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value;
        onChange?.(newValue);
        setInternalValue(newValue);
    };

    const renderIcon = () => {
        if (icon === "email") return <MailIcon className="text-grey_400" />;
        if (icon === "password")
            return <PasswordIcon className="text-grey_400" />;
        if (icon === "nickname")
            return <NicknameIcon className="text-grey_400" />;
        return null;
    };

    return (
        <div className="mb-4 w-full">
            <div className="flex items-center bg-white/80 rounded-full shadow-md text-xs px-4 py-1 mb-2 transition duration-200 focus-within:bg-white">
                {renderIcon()}
                <input
                    type={showToggle && isVisible ? "text" : type}
                    placeholder={placeholder}
                    value={value !== undefined ? value : internalValue}
                    onChange={handleChange}
                    disabled={disabled}
                    className="ml-3 py-3 flex-1 bg-transparent outline-none text-black"
                />
                {showToggle && (
                    <button
                        type="button"
                        onClick={toggleVisibility}
                        className="text-gray-500"
                    >
                        {isVisible ? <Visible /> : <Invisible />}
                    </button>
                )}
            </div>
            {isError && (
                <p
                    className={`text-xs text-error_red pr-2 flex w-full justify-end items-center transition-opacity duration-200 ${error ? "opacity-100" : "opacity-0"}`}
                >
                    {error || "‎"}
                </p>
            )}
        </div>
    );
};

export default InputField;
