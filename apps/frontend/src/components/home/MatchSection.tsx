import * as React from "react";
import MovieItem from "../MovieItem";
import ArrowRight from "@assets/arrow_right.svg?react";
import { useEffect, useState } from "react";
import WinnerItem from "../WinnerItem";
import Button from "../Button";
import { useNavigate } from "react-router-dom";
import { matchApi } from "../../api/matchApi";

interface MatchSectionProps {
    className?: string;
}

interface Movie {
    id: number;
    posterPath: string;
    title: string;
    voteAverage: number;
    mainEmotion: string;
    emotionValue: number;
    releaseDate: string;
    ratingAvg: number;
}

interface Winner {
    matchNumber: number;
    matchDate: string;
    movie: {
        id: number;
        posterPath: string;
        title: string;
        voteAverage: number;
        mainEmotion: string;
        emotionValue: number;
        releaseDate: string;
        matchDate: string;
        ratingAvg: number;
    };
}

const MatchSection: React.FC<MatchSectionProps> = ({ className = "" }) => {
    const navigate = useNavigate();
    const [movieList, setMovieList] = useState<Movie[]>([]);
    const [pastWinners, setPastWinners] = useState<Winner[]>([]);

    useEffect(() => {
        (async () => {
            try {
                const res = await matchApi().getWeeklyMatchMovie();
                setMovieList(res.data.data);
            } catch (e) {
                console.error("금주의 영화 대결 조회 에러: ", e);
            }
        })();
    }, []);

    useEffect(() => {
        (async () => {
            try {
                const res = await matchApi().getPastMatchMovie();
                setPastWinners(res.data.data);
            } catch (e) {
                console.error("역대 우승 영화 조회 에러:", e);
            }
        })();
    }, []);

    return (
        <section className={`w-full ${className}`}>
            <h1
                onClick={() => navigate("/weekmatch")}
                className="inline-flex items-center gap-2 cursor-pointer text-xl font-semibold mb-4 text-white transform transition-transform duration-200 hover:scale-105"
            >
                금주의 영화 대결
                <ArrowRight />
            </h1>
            <div className="flex flex-col w-full justify-center gap-10">
                <div className="flex flex-col items-center">
                    {movieList.length === 0 ? (
                        <div className="flex justify-center items-center gap-10 mt-10 text-center text-gray-400 py-12">
                            아직 투표된 내용이 없습니다.
                        </div>
                    ) : (
                        <div className="flex flex-col md:flex-row justify-center items-center">
                            {movieList.slice(0, 5).map((poster, idx) => (
                                <React.Fragment key={idx}>
                                    <MovieItem
                                        movieId={poster.id}
                                        posterImg={poster.posterPath}
                                        posterName={poster.title}
                                        emotionIcon={poster.mainEmotion}
                                        emotionValue={poster.emotionValue}
                                        starValue={poster.voteAverage}
                                        ratingAvg={poster.ratingAvg}
                                    />
                                    {idx < 2 && (
                                        <span className="text-white text-xl mx-2">
                                            VS
                                        </span>
                                    )}
                                </React.Fragment>
                            ))}
                        </div>
                    )}
                    <Button
                        className="w-1/2 mt-12"
                        text="투표하러 가기"
                        textColor="black"
                        buttonColor="white"
                        onClick={() => navigate("/weekmatch")}
                    />
                </div>
                <div className="flex flex-col flex-1">
                    <h1 className="flex items-center gap-2 text-lg font-semibold mb-4 text-white">
                        역대 우승 영화
                    </h1>
                    <div className="flex flex-col items-center justify-center px-2 py-1">
                        {pastWinners.length === 0 ? (
                            <p className="text-white text-center w-full">
                                역대 우승 영화가 없습니다.
                            </p>
                        ) : (
                            pastWinners.map((movie, i) => (
                                <WinnerItem
                                    key={i}
                                    posterImg={movie.movie.posterPath}
                                    posterName={movie.movie.title}
                                    emotionIcon={movie.movie.mainEmotion}
                                    emotionValue={movie.movie.emotionValue}
                                    starValue={movie.movie.voteAverage}
                                    ratingAvg={movie.movie.ratingAvg}
                                    winnerWeek={movie.matchDate}
                                    onClick={() =>
                                        navigate(
                                            `/movies/detail/${movie.movie.id}`,
                                        )
                                    }
                                />
                            ))
                        )}
                    </div>
                </div>
            </div>
        </section>
    );
};

export default MatchSection;
