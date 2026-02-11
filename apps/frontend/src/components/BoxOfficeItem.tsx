import React from "react";
import joyIcon from "@assets/character/joy_icon.png";
import sadIcon from "@assets/character/sad_icon.png";
import angryIcon from "@assets/character/angry_icon.png";
import fearIcon from "@assets/character/fear_icon.png";
import disgustIcon from "@assets/character/disgust_icon.png";
import bingbongIcon from "@assets/character/bingbong_icon.png";
import StarFull from "@assets/star_full.svg?react";
import TMDB from "@assets/TMDB.svg?react";
import DefaultImage from "@assets/defaultImage.svg?react";
import Down from "@assets/down.svg?react";
import Up from "@assets/up.svg?react";
import { useNavigate } from "react-router-dom";

interface BoxOfficeItemProps {
    movieId: number;
    rank: string;
    rankInten: string;
    rankOldAndNew: string;
    posterPath: string;
    title: string;
    audiAcc: string;
    mainEmotion: string;
    mainEmotionValue: number;
    voteAverage: number;
    ratingAvg: number;
}

const emotionMap = {
    joy: joyIcon,
    sad: sadIcon,
    angry: angryIcon,
    fear: fearIcon,
    disgust: disgustIcon,
    bingbong: bingbongIcon,
    none: bingbongIcon,
};

const BoxOfficeItem: React.FC<BoxOfficeItemProps> = ({
    movieId,
    rank,
    rankInten,
    rankOldAndNew,
    posterPath,
    title,
    audiAcc,
    mainEmotion,
    mainEmotionValue,
    voteAverage,
    ratingAvg,
}) => {
    const navigate = useNavigate();
    return (
        <div
            className="flex gap-3 mb-2"
            onClick={() =>
                movieId !== null ? navigate(`/movies/detail/${movieId}`) : null
            }
        >
            <div className="w-[80px] text-white font-bold text-end relative">
                <p className="text-4xl">{rank}</p>
                <div className="flex gap-1 mt-2 justify-end text-xs font-extralight">
                    {Number(rankInten) > 0 && (
                        <>
                            <Up />
                            <span>{rankInten}</span>
                        </>
                    )}
                    {Number(rankInten) < 0 && (
                        <>
                            <Down />
                            <span>{Math.abs(Number(rankInten))}</span>
                        </>
                    )}
                    {Number(rankInten) === 0 && <span>-</span>}
                </div>
                <div className="">
                    <p className="text-xs mt-1 font-extralight text-error_red">
                        {rankOldAndNew === "NEW" ? rankOldAndNew : ""}
                    </p>
                </div>
            </div>
            <div className="flex w-full rounded-3xl bg-box_bg_white items-center cursor-pointer transform transition-all duration-200 hover:bg-box_bg_white/30">
                {posterPath ? (
                    <img
                        src={posterPath}
                        alt={title}
                        className="w-auto h-32 rounded-l-3xl object-cover me-4"
                    />
                ) : (
                    <DefaultImage className="w-auto h-32 rounded-l-3xl object-cover me-4" />
                )}

                <div className="flex flex-col gap-2 flex-1 text-white">
                    <div className="text-lg font-semibold">{title}</div>
                    <div className="text-xs font-light text-gray-300">
                        누적 관객수 {Number(audiAcc).toLocaleString()}명
                    </div>

                    <div className="flex gap-2 px-1 pb-2">
                        <div className="flex items-center text-xs font-light text-white">
                            <img
                                src={emotionMap[mainEmotion]}
                                alt={mainEmotion}
                                className="w-6 h-6"
                            />
                            <p>{Math.round(mainEmotionValue)}%</p>
                        </div>
                        <div className="flex items-center text-xs font-light text-white">
                            <StarFull className="w-6 h-6" />
                            <p>{ratingAvg}</p>
                        </div>
                        <div className="flex items-center text-xs font-light text-white">
                            <TMDB className="w-6 h-6" />
                            <p>{voteAverage}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BoxOfficeItem;
