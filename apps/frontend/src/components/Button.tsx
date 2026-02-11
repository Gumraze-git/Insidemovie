import * as React from "react";

interface ButtonProps {
    text: string;
    textColor?: "white" | "black";
    buttonColor?:
        | "kakao"
        | "default"
        | "transparent"
        | "disabled"
        | "white"
        | "red";
    disabled?: boolean;
    prefixIcon?: string;
    className?: string;
    onClick?: () => void;
}

const Button: React.FC<ButtonProps> = ({
    text = "",
    textColor = "white",
    buttonColor = "default",
    disabled = false,
    prefixIcon = "",
    className = "",
    onClick,
}) => {
    const textColors = {
        white: "text-white",
        black: "text-black",
    };

    const baseColors = {
        kakao: "bg-kakao_yellow hover:bg-kakao_yellow_bright",
        default: "bg-movie_sub hover:bg-movie_bright",
        transparent: "bg-box_bg_white hover:bg-white/40",
        disabled: "bg-grey_300",
        white: "bg-white hover:bg-white/80",
        red: "bg-error_red hover:bg-error_red/70",
    };

    const textClass =
        textColor && textColors[textColor] ? textColors[textColor] : "";
    const bgClass =
        buttonColor && baseColors[buttonColor] ? baseColors[buttonColor] : "";

    return (
        <button
            onClick={onClick}
            disabled={disabled}
            className={`rounded-full px-4 py-4 text-xs font-semibold transition duration-200 shadow-md flex items-center justify-center gap-2 ${textClass} ${bgClass} ${disabled ? "opacity-50" : ""} ${className}`}
        >
            {prefixIcon && (
                <img src={prefixIcon} alt="icon" className="w-4 h-4" />
            )}
            {text}
        </button>
    );
};

export default Button;
