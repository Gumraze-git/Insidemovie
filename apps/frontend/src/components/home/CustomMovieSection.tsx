import * as React from "react";
import MovieItem from "../MovieItem";
import ArrowRight from "@assets/arrow_right.svg?react";
import { useState, useEffect } from "react";
import { recommendApi } from "../../api/recommendApi";
import EmotionSection from "./EmotionSection";

interface CustomMovieSectionProps {
    className?: string;
}

interface Movie {
    movieId: number;
    title: string;
    posterPath: string;
    voteAverage: number;
    dominantEmotion: string;
    dominantEmotionRatio: number;
    ratingAvg: number;
}

const CustomMovieSection: React.FC<CustomMovieSectionProps> = ({
    className = "",
}) => {
    const scrollRef = React.useRef<HTMLDivElement>(null);
    const [canScrollLeft, setCanScrollLeft] = React.useState(false);
    const [canScrollRight, setCanScrollRight] = React.useState(true);
    const [recommendList, setRecommendList] = useState<Movie[]>([]);
    const [isLoading, setIsLoading] = useState(false);

    React.useEffect(() => {
        const scrollElement = scrollRef.current;
        if (!scrollElement) return;

        const updateScroll = () => {
            setCanScrollLeft(scrollElement.scrollLeft > 0);
            setCanScrollRight(
                scrollElement.scrollLeft + scrollElement.clientWidth <
                    scrollElement.scrollWidth,
            );
        };

        scrollElement.addEventListener("scroll", updateScroll);
        requestAnimationFrame(updateScroll);

        return () => scrollElement.removeEventListener("scroll", updateScroll);
    }, [recommendList]);

    useEffect(() => {
        const timer = setTimeout(async () => {
            setIsLoading(true);
            try {
                const res = await recommendApi().getRecommendMovie({
                    joy: 50,
                    sadness: 50,
                    anger: 50,
                    fear: 50,
                    disgust: 50,
                });
                setRecommendList(res.data.data);
                setIsLoading(false);
            } catch (e) {
                console.error("맞춤 영화 추천 에러!! : ", e);
                setRecommendList([]);
                setIsLoading(false);
            }
        });
        return () => clearTimeout(timer);
    }, []);

    const handleEmotionsChange = async (
        joy: number,
        sad: number,
        angry: number,
        fear: number,
        disgust: number,
    ) => {
        try {
            // Normalize values to 0-1
            const joyNorm = joy;
            const sadNorm = sad;
            const angryNorm = angry;
            const fearNorm = fear;
            const disgustNorm = disgust;
            const res = await recommendApi().getRecommendMovie({
                joy: joyNorm,
                sadness: sadNorm,
                anger: angryNorm,
                fear: fearNorm,
                disgust: disgustNorm,
            });
            console.log(
                `requestData: joy - ${joyNorm}, sad - ${sadNorm}, angry - ${angryNorm}, fear - ${fearNorm}, disgust - ${disgustNorm}`,
            );
            setRecommendList(res.data.data);
            console.log(res.data.data);
        } catch (e) {
            console.error("맞춤 영화 추천 에러!! : ", e);
            setRecommendList([]);
        }
    };

    return (
        <section className={`w-full ${className}`}>
            <EmotionSection onEmotionsChange={handleEmotionsChange} />
            <h1 className="text-xl font-semibold mb-4 text-white mt-10">
                맞춤 영화
            </h1>
            <div className="relative w-full">
                <div
                    ref={scrollRef}
                    className="w-full overflow-x-auto scrollbar-hide"
                >
                    <div className="flex gap-3 w-max px-2">
                        {isLoading
                            ? Array.from({ length: 5 }).map((_, idx) => (
                                  <div
                                      key={idx}
                                      className="w-[200px] h-[280px] mx-1 my-3 bg-gray-700 animate-pulse rounded-lg"
                                  />
                              ))
                            : recommendList.map((movie, idx) => (
                                  <MovieItem
                                      key={idx}
                                      movieId={movie.movieId}
                                      posterImg={movie.posterPath}
                                      posterName={movie.title}
                                      emotionIcon={movie.dominantEmotion.toLowerCase()}
                                      emotionValue={
                                          movie.dominantEmotionRatio || 0
                                      }
                                      starValue={movie.voteAverage}
                                      ratingAvg={movie.ratingAvg}
                                  />
                              ))}
                    </div>
                </div>

                {/* Left Fade + Arrow */}
                {canScrollLeft && (
                    <>
                        <div className="pointer-events-none absolute top-0 left-0 h-full w-16 bg-gradient-to-r from-[#081232] to-transparent" />
                        <button
                            onClick={() =>
                                scrollRef.current?.scrollBy({
                                    left: -300,
                                    behavior: "smooth",
                                })
                            }
                            className="absolute left-4 top-1/2 -translate-y-1/2 z-10"
                        >
                            <ArrowRight className="w-8 h-8 text-white rotate-180" />
                        </button>
                    </>
                )}

                {/* Right Fade + Arrow */}
                {canScrollRight && (
                    <>
                        <div className="pointer-events-none absolute top-0 right-0 h-full w-16 bg-gradient-to-l from-[#081232] to-transparent" />
                        <button
                            onClick={() =>
                                scrollRef.current?.scrollBy({
                                    left: 300,
                                    behavior: "smooth",
                                })
                            }
                            className="absolute right-4 top-1/2 -translate-y-1/2 z-10"
                        >
                            <ArrowRight className="w-8 h-8 text-white" />
                        </button>
                    </>
                )}
            </div>
        </section>
    );
};

export default CustomMovieSection;
