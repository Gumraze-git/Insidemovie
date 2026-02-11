import * as React from "react";
import { useState, useEffect } from "react";
import type { boxOffice } from "../../interfaces/boxOffice";
import BoxOfficeItem from "../../components/BoxOfficeItem";
import { boxofficeApi } from "../../api/boxofficeApi";

const BoxOfficeMovie: React.FC = () => {
    const [movieDailyList, setDailyMovieList] = useState<boxOffice[]>([]);
    const [movieWeeklyList, setWeeklyMovieList] = useState<boxOffice[]>([]);

    useEffect(() => {
        (async () => {
            try {
                const resDaily = await boxofficeApi().getDailyBoxOffice();
                setDailyMovieList(resDaily.data.data.items);

                const resWeekly = await boxofficeApi().getWeeklyBoxOffice();
                setWeeklyMovieList(resWeekly.data.data.items);
            } catch (e) {
                console.error("박스오피스 조회 에러!! : ", e);
            }
        })();
    }, []);

    return (
        <div className="flex justify-center">
            <div className="max-w-screen-lg w-full flex flex-col pt-20 py-36 px-5">
                <h1 className="text-white text-3xl font-semibold pb-6 text-left">
                    박스오피스 순위 top10
                </h1>
                <div className="flex flex-col md:flex-row">
                    {/* Daily Box Office */}
                    <div className="flex flex-col flex-1 gap-3 overflow-x-hidden scrollbar-hide px-2">
                        <h2 className="text-white font-semibold mb-2">
                            일별 순위 Top 10
                        </h2>
                        {movieDailyList.map((movie, idx) => (
                            <BoxOfficeItem
                                key={`daily-${idx}`}
                                movieId={movie.movieId}
                                rank={movie.base.rank}
                                rankInten={movie.base.rankInten}
                                rankOldAndNew={movie.base.rankOldAndNew}
                                posterPath={movie.posterPath}
                                title={movie.title}
                                audiAcc={movie.base.audiAcc}
                                mainEmotion={movie.mainEmotion.toLowerCase()}
                                mainEmotionValue={movie.mainEmotionValue}
                                voteAverage={movie.voteAverage}
                                ratingAvg={movie.ratingAvg}
                            />
                        ))}
                    </div>
                    {/* Weekly Box Office */}
                    <div className="flex flex-col flex-1 gap-3 overflow-x-hidden scrollbar-hide px-2 mt-10 md:mt-0">
                        <h2 className="text-white font-semibold mb-2">
                            주간 순위 Top 10
                        </h2>
                        {movieWeeklyList.map((movie, idx) => (
                            <BoxOfficeItem
                                key={`weekly-${idx}`}
                                movieId={movie.movieId}
                                rank={movie.base.rank}
                                rankInten={movie.base.rankInten}
                                rankOldAndNew={movie.base.rankOldAndNew}
                                posterPath={movie.posterPath}
                                title={movie.title}
                                audiAcc={movie.base.audiAcc}
                                mainEmotion={movie.mainEmotion.toLowerCase()}
                                mainEmotionValue={movie.mainEmotionValue}
                                voteAverage={movie.voteAverage}
                                ratingAvg={movie.ratingAvg}
                            />
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BoxOfficeMovie;
