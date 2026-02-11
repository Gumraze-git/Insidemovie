import * as React from "react";
import TransparentBox from "./TransparentBox";
import { useNavigate } from "react-router-dom";
import joyIcon from "@assets/character/joy_icon.png";
import sadIcon from "@assets/character/sad_icon.png";
import angryIcon from "@assets/character/angry_icon.png";
import fearIcon from "@assets/character/fear_icon.png";
import disgustIcon from "@assets/character/disgust_icon.png";
import bingbongIcon from "@assets/character/bingbong_icon.png";
import StarFull from "@assets/star_full.svg?react";
import TMDB from "@assets/TMDB.svg?react";

interface MovieItemProps {
    movieId: number;
    posterImg: string;
    posterName: string;
    emotionIcon: string;
    emotionValue: number;
    ratingAvg: number;
    starValue: number;
    className?: string;
}

const emotionMap = {
    joy: joyIcon,
    sadness: sadIcon,
    anger: angryIcon,
    fear: fearIcon,
    disgust: disgustIcon,
    bingbong: bingbongIcon,
    none: bingbongIcon,
};

const MovieItem: React.FC<MovieItemProps> = ({
    movieId,
    posterImg,
    posterName,
    emotionIcon = "bingbong",
    emotionValue = 0,
    ratingAvg = 0,
    starValue = 0,
    className = "",
}) => {
    const navigate = useNavigate();

    return (
        <div
            className="px-1 py-3"
            onClick={() => {
                navigate(`/movies/detail/${movieId}`);
            }}
        >
            <TransparentBox
                className={`w-[180px] bg-box_bg_white rounded-xl shadow-md cursor-pointer flex flex-col transform transition-transform duration-200 hover:scale-105 ${className}`}
            >
                <img
                    src={posterImg}
                    alt="posterImage"
                    className="w-full h-64 object-cover rounded-t-xl"
                />
                <div className="text-left text-white text-lg font-extralight px-2 py-1 truncate">
                    {posterName}
                </div>
                <div className="flex items-center gap-2 px-1 pb-2">
                    <div className="flex items-center text-xs font-light text-white">
                        <img
                            src={emotionMap[emotionIcon]}
                            alt={emotionIcon}
                            className="w-4 h-4"
                        />
                        <p>{Math.round(emotionValue)}%</p>
                    </div>
                    <div className="flex items-center text-xs font-light text-white">
                        <StarFull className="w-4 h-4" />
                        <p>{Math.round(ratingAvg)}</p>
                    </div>
                    <div className="flex items-center text-xs font-light text-white">
                        <TMDB className="w-4 h-4" />
                        <p>{Math.round(starValue)}</p>
                    </div>
                </div>
            </TransparentBox>
        </div>
    );
};

export default MovieItem;
