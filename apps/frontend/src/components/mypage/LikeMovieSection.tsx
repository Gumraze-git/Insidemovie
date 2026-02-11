import * as React from "react";
import ArrowRight from "@assets/arrow_right.svg?react";
import MovieItem from "../MovieItem";
import { useEffect, useState } from "react";
import { memberApi } from "../../api/memberApi";
import { useNavigate } from "react-router-dom";

interface LikeMovieSectionProps {
    className?: string;
}

interface Movie {
    movieId: number;
    posterPath: string;
    title: string;
    mainEmotion: string;
    mainEmotionValue: number;
    voteAverage: number;
    ratingAvg: number;
}

const LikeMovieSection: React.FC<LikeMovieSectionProps> = ({
    className = "",
}) => {
    const navigate = useNavigate();
    const scrollRef = React.useRef<HTMLDivElement>(null);
    const [canScrollLeft, setCanScrollLeft] = React.useState(false);
    const [canScrollRight, setCanScrollRight] = React.useState(true);

    const [movieList, setMovieList] = useState<Movie[]>([]);

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
    }, [movieList]);

    useEffect(() => {
        (async () => {
            try {
                const res = await memberApi().getMyLikedMovies({
                    page: 0,
                    pageSize: 10,
                });
                setMovieList(res.data.data.content);
            } catch (e) {
                console.error("내 영화 좋아요 목록 조회 에러: ", e);
            }
        })();
    }, []);

    return (
        <section className={`w-full ${className}`}>
            <h1
                onClick={() => navigate("/mypage/liked-movie")}
                className="inline-flex items-center gap-2 cursor-pointer text-xl font-semibold mb-4 text-white transform transition-transform duration-200 hover:scale-105"
            >
                좋아요 한 영화
                <ArrowRight />
            </h1>
            <div className="relative w-full">
                <div
                    ref={scrollRef}
                    className="w-full overflow-x-auto scrollbar-hide"
                >
                    <div className="flex gap-3 w-max px-2">
                        {movieList.map((poster) => (
                            <MovieItem
                                key={poster.movieId}
                                movieId={poster.movieId}
                                posterImg={poster.posterPath}
                                posterName={poster.title}
                                emotionIcon={poster.mainEmotion.toLowerCase()}
                                emotionValue={poster.mainEmotionValue}
                                starValue={poster.voteAverage}
                                ratingAvg={poster.ratingAvg}
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

export default LikeMovieSection;
