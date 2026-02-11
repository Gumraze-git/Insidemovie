import React from "react";
import Rating from "@mui/material/Rating";

interface StarRatingProps {
    value: number;
    onChange?: (value: number) => void;
    readOnly?: boolean;
    showOneStar?: boolean;
    showValue?: boolean;
    size?: "small" | "medium" | "large";
}

const StarRating: React.FC<StarRatingProps> = ({
    value,
    onChange,
    readOnly = false,
    showOneStar = false,
    showValue = false,
    size = "medium",
}) => {
    return (
        <div className="flex items-center gap-2">
            <Rating
                name="rating"
                value={showOneStar ? 1 : value}
                precision={0.5}
                readOnly={readOnly}
                max={showOneStar ? 1 : 5}
                size={size}
                onChange={(_, newValue) => {
                    if (onChange && newValue !== null) {
                        onChange(newValue);
                    }
                }}
                sx={{
                    "& .MuiRating-iconFilled": {
                        color: "#FFD602", // 채워진 별
                    },
                    "& .MuiRating-iconEmpty": {
                        color: "#7C7C7C", // 빈 별 색상
                    },
                    "& .MuiRating-iconHover": {
                        color: "#FFD602", // 호버 시
                    },
                }}
            />
            {showValue && (
                <span className="text-sm text-white">
                    {(value ?? 0).toFixed(1)}
                </span>
            )}
        </div>
    );
};

export default StarRating;
