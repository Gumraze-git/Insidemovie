import * as React from "react";
import ArrowRight from "@assets/arrow_right.svg?react";
import MovieItem from "../MovieItem";
import Tag from "../Tag";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { movieApi } from "../../api/movieApi";

interface RecommendMovieSectionProps {
    className?: string;
}

interface Movie {
    id: number;
    posterPath: string;
    title: string;
    voteAverage: number;
    mainEmotion: string;
    mainEmotionValue: number;
    releaseDate: string;
    ratingAvg: number;
}

const RecommendMovieSection: React.FC<RecommendMovieSectionProps> = ({
    className = "",
}) => {
    const navigate = useNavigate();
    const tagList = [
        "액션",
        "모험",
        "애니메이션",
        "코미디",
        "범죄",
        "다큐멘터리",
        "드라마",
        "가족",
        "판타지",
        "역사",
        "공포",
        "음악",
        "미스터리",
        "로맨스",
        "SF",
        "TV영화",
        "스릴러",
        "전쟁",
        "서부",
    ];
    const [selectedTags, setSelectedTags] = React.useState<string[]>([
        tagList[0],
    ]);
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
        const tag = selectedTags[0];
        if (!tag) {
            setMovieList([]);
            return;
        }
        (async () => {
            try {
                const res = await movieApi().getPopularMoviesByGenre({
                    genre: tag,
                    page: 0,
                    pageSize: 10,
                });
                setMovieList(res.data.data.content);
            } catch (e) {
                console.error("장르별 영화 조회 에러!! : ", e);
                setMovieList([]);
            }
        })();
    }, [selectedTags]);

    const handleTagClick = (label: string) => {
        setSelectedTags((prev) =>
            // 다중 선택
            // prev.includes(label)
            //     ? prev.filter((t) => t !== label)
            //     : [...prev, label],
            prev[0] === label ? [] : [label],
        );
    };

    return (
        <section className={`w-full ${className}`}>
            <h1
                onClick={() => navigate("/recommend")}
                className="inline-flex items-center gap-2 cursor-pointer text-xl font-semibold mb-4 text-white transform transition-transform duration-200 hover:scale-105"
            >
                추천 영화
                <ArrowRight />
            </h1>
            <div className="flex items-center mb-4 px-2 w-full overflow-x-auto scrollbar-hide whitespace-nowrap">
                {tagList.map((tag) => (
                    <Tag
                        key={tag}
                        label={tag}
                        selected={selectedTags.includes(tag)}
                        onClick={handleTagClick}
                    />
                ))}
            </div>
            <div className="relative w-full">
                <div
                    ref={scrollRef}
                    className="w-full overflow-x-auto scrollbar-hide"
                >
                    <div className="flex gap-3 w-max px-2">
                        {movieList.length === 0
                            ? Array.from({ length: 5 }).map((_, idx) => (
                                  <div
                                      key={idx}
                                      className="w-[200px] h-[280px] mx-1 my-3 bg-gray-700 animate-pulse rounded-lg"
                                  />
                              ))
                            : movieList.map((poster, idx) => (
                                  <MovieItem
                                      key={idx}
                                      movieId={poster.id}
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

export default RecommendMovieSection;
