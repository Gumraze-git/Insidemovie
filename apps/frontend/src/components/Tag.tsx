import * as React from "react";
import Check from "@assets/check.svg?react";

interface TagProps {
    label: string;
    selected?: boolean;
    onClick?: (label: string) => void;
    className?: string;
}

const Tag: React.FC<TagProps> = ({
    label,
    selected = false,
    onClick,
    className = "",
}) => {
    return (
        <div
            className={`inline-flex items-center px-4 py-1 mr-2 rounded-full text-sm font-light cursor-pointer transform transition-transform duration-200 hover:scale-105 ${
                selected ? "bg-white text-black" : "bg-box_bg_white text-white"
            } ${className}`}
            onClick={() => onClick?.(label)}
        >
            <span className="mr-1 transition-all duration-200">
                {selected && <Check className="w-4 h-4" />}
            </span>
            {label}
        </div>
    );
};

export default Tag;
