import * as React from "react";
import ArrowRight from "@assets/arrow_right.svg?react";
import { useEffect, useState } from "react";
import BoxOfficeItem from "../BoxOfficeItem";
import type { boxOffice } from "../../interfaces/boxOffice";
import { useNavigate } from "react-router-dom";
import { boxofficeApi } from "../../api/boxofficeApi";

interface CustomBoxOfficeSectionProps {
    className?: string;
}

const BoxOfficeSection: React.FC<CustomBoxOfficeSectionProps> = ({
    className = "",
}) => {
    const navigate = useNavigate();
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
                console.error("박스오피스 영화 조회 에러!! : ", e);
            }
        })();
    }, []);

    return (
        <section className={`w-full ${className}`}>
            <h1
                onClick={() => navigate("/boxoffice")}
                className="inline-flex items-center gap-2 cursor-pointer text-xl font-semibold mb-4 text-white transform transition-transform duration-200 hover:scale-105"
            >
                박스오피스 순위
                <ArrowRight />
            </h1>
            <div className="flex flex-col md:flex-row">
                {/* Daily Box Office */}
                <div className="flex flex-col flex-1 gap-3 overflow-x-hidden scrollbar-hide px-2">
                    <h2 className="text-white font-semibold mb-2">
                        일별 순위 Top 3
                    </h2>
                    {movieDailyList.length === 0
                        ? Array.from({ length: 3 }).map((_, idx) => (
                              <div
                                  key={idx}
                                  className="w-full h-[150px] mx-1 my-1 bg-gray-700 animate-pulse rounded-lg"
                              />
                          ))
                        : movieDailyList
                              .slice(0, 3)
                              .map((movie, idx) => (
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
                        주간 순위 Top 3
                    </h2>
                    {movieWeeklyList.length === 0
                        ? Array.from({ length: 3 }).map((_, idx) => (
                              <div
                                  key={idx}
                                  className="w-full h-[150px] mx-1 my-1 bg-gray-700 animate-pulse rounded-lg"
                              />
                          ))
                        : movieWeeklyList
                              .slice(0, 3)
                              .map((movie, idx) => (
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
        </section>
    );
};

export default BoxOfficeSection;
