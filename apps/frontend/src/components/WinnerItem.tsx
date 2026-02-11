import React from "react";
import StarFull from "@assets/star_full.svg?react";
import TMDB from "@assets/TMDB.svg?react";

interface WinnerItemProps {
    posterImg: string;
    posterName: string;
    emotionIcon: string;
    emotionValue: number;
    starValue: number;
    ratingAvg: number;
    winnerWeek: string;
    onClick?: () => void;
}

const WinnerItem: React.FC<WinnerItemProps> = ({
    posterImg,
    posterName,
    starValue,
    ratingAvg,
    winnerWeek,
    onClick,
}) => {
    return (
        <div
            className="flex w-full rounded-3xl mb-3 bg-box_bg_white items-center cursor-pointer transform transition-all duration-200 hover:bg-box_bg_white/30"
            onClick={onClick}
        >
            <img
                src={posterImg}
                alt={posterName}
                className="w-auto h-32 rounded-l-3xl object-cover me-4"
            />

            <div className="flex-1 text-white">
                <div className="text-sm font-light">{winnerWeek}</div>
                <div className="text-xl font-semibold">{posterName}</div>

                <div className="flex items-center my-2">
                    <StarFull className="w-4 h-4" />
                    <span className="ml-1 text-sm">
                        {Number.isInteger(Number(ratingAvg))
                            ? String(Number(ratingAvg))
                            : Number(ratingAvg).toFixed(1)}
                    </span>
                    <TMDB className="w-4 h-4 ml-3" />
                    <span className="ml-1 text-sm">
                        {Number.isInteger(Number(starValue))
                            ? String(Number(starValue))
                            : Number(starValue).toFixed(1)}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default WinnerItem;
