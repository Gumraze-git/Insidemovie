import * as React from "react";

interface TransparentBoxProps {
    children: React.ReactNode;
    padding?: string;
    className?: string;
}

const TransparentBox: React.FC<TransparentBoxProps> = ({
    children,
    padding,
    className = "",
}) => {
    return (
        <div
            className={`bg-box_bg_white/10 backdrop-blur rounded-3xl shadow-xl ${padding} ${className}`}
        >
            {children}
        </div>
    );
};

export default TransparentBox;
