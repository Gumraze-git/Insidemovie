import React, { useEffect, useState, useCallback, useRef } from "react";
import { movieApi } from "../../api/movieApi";
import MovieItem from "../../components/MovieItem";
import Tag from "../../components/Tag";
import { Pagination } from "@mui/material";
import { Select, MenuItem } from "@mui/material";

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

const RecommendMovie: React.FC = () => {
    const genreList = [
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
    const [movieList, setMovieList] = useState<Movie[]>([]);
    const [selectedTag, setSelectedTag] = useState<string>(genreList[0]);
    const [page, setPage] = useState(0);
    const pageSize = 40;
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
    // Sort type: 'rating' for 평점순, 'latest' for 최신순
    const [sortType, setSortType] = useState<"rating" | "latest">("rating");

    const [isLoading, setIsLoading] = useState(false);
    const [isLastPage, setIsLastPage] = useState(false);
    const observer = useRef<IntersectionObserver | null>(null);

    useEffect(() => {
        const fetchByGenre = async () => {
            try {
                setIsLoading(true);
                const res =
                    sortType === "rating"
                        ? await movieApi().getPopularMoviesByGenre({
                              genre: selectedTag,
                              page,
                              pageSize,
                          })
                        : await movieApi().getLatestMoviesByGenre({
                              genre: selectedTag,
                              page,
                              pageSize,
                          });
                const {
                    content,
                    totalPages: tp,
                    totalElements: te,
                } = res.data.data;
                if (isMobile) {
                    setMovieList((prev) =>
                        page === 0 ? content : [...prev, ...content],
                    );
                } else {
                    setMovieList(content);
                }
                setTotalPages(tp);
                setTotalElements(te);
                setIsLastPage(page >= tp - 1);
            } catch (e) {
                console.error("장르별 조회 에러: ", e);
                setMovieList([]);
            } finally {
                setIsLoading(false);
            }
        };
        fetchByGenre();
    }, [selectedTag, page, sortType, isMobile]);

    // Infinite scroll via IntersectionObserver on mobile
    const lastItemRef = useCallback(
        (node: HTMLDivElement) => {
            if (!isMobile) return;
            if (isLoading) return;
            if (observer.current) observer.current.disconnect();
            observer.current = new IntersectionObserver((entries) => {
                if (entries[0].isIntersecting && page < totalPages - 1) {
                    setPage((prev) => prev + 1);
                }
            });
            if (node) observer.current.observe(node);
        },
        [isMobile, isLoading, page, totalPages],
    );

    // Reset list and page when switching between mobile and desktop
    useEffect(() => {
        const handleResize = () => {
            const mobileNow = window.innerWidth < 768;
            if (mobileNow !== isMobile) {
                setIsMobile(mobileNow);
                setPage(0);
                setMovieList([]);
            }
        };
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, [isMobile]);

    const handleTagClick = (label: string) => {
        setSelectedTag(label);
        setPage(0);
    };

    const handlePageChange = (
        _e: React.ChangeEvent<unknown>,
        value: number,
    ) => {
        setPage(value - 1);
    };

    return (
        <div>
            <div className="flex justify-center">
                <div className="max-w-screen-lg w-full">
                    <div className="flex flex-col pt-20">
                        <h1 className="text-white text-3xl font-semibold text-left pb-3 border-b-[1px] border-box_bg_white mx-5 md:mx-0">
                            추천 영화
                        </h1>

                        <div className="flex items-center pt-3 w-full overflow-x-auto scrollbar-hide whitespace-nowrap px-5 md:mx-0">
                            {genreList.map((tag) => (
                                <Tag
                                    key={tag}
                                    label={tag}
                                    selected={selectedTag === tag}
                                    onClick={handleTagClick}
                                />
                            ))}
                        </div>
                        <div className="flex items-center justify-between w-full mt-10 px-5 md:px-0">
                            <p className="text-white text-2xl font-semibold">
                                <span className="font-extralight text-sm">
                                    {totalElements}건
                                </span>
                            </p>
                            <Select
                                value={sortType}
                                onChange={(e) =>
                                    setSortType(
                                        e.target.value as "rating" | "latest",
                                    )
                                }
                                size="small"
                                sx={{
                                    color: "#fff",
                                    ".MuiOutlinedInput-notchedOutline": {
                                        borderColor: "#00000000",
                                    },
                                    ".MuiSvgIcon-root": { color: "#fff" },
                                }}
                            >
                                <MenuItem value="rating">평점순</MenuItem>
                                <MenuItem value="latest">최신순</MenuItem>
                            </Select>
                        </div>
                        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 mt-4 mb-20 mx-5 md:mx-0">
                            {movieList.length === 0
                                ? Array.from({ length: 20 }).map((_, idx) => (
                                      <div
                                          key={idx}
                                          className="w-[200px] h-[280px] mx-1 my-3 bg-gray-700 animate-pulse rounded-lg"
                                      />
                                  ))
                                : movieList.map((poster, idx) => (
                                      <div
                                          key={poster.id}
                                          ref={
                                              idx === movieList.length - 1
                                                  ? lastItemRef
                                                  : undefined
                                          }
                                      >
                                          <MovieItem
                                              movieId={poster.id}
                                              posterImg={poster.posterPath}
                                              posterName={poster.title}
                                              emotionIcon={poster.mainEmotion.toLowerCase()}
                                              emotionValue={
                                                  poster.mainEmotionValue
                                              }
                                              starValue={poster.voteAverage}
                                              ratingAvg={poster.ratingAvg}
                                          />
                                      </div>
                                  ))}
                        </div>
                        {totalPages > 1 && (
                            <div className="hidden sm:flex justify-center text-white mt-6 mb-36">
                                <Pagination
                                    count={totalPages}
                                    page={page + 1}
                                    onChange={handlePageChange}
                                    siblingCount={1}
                                    boundaryCount={1}
                                    showFirstButton
                                    showLastButton
                                    color="primary"
                                    sx={{
                                        "& .MuiPaginationItem-root": {
                                            color: "#fff",
                                        },
                                    }}
                                />
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RecommendMovie;
