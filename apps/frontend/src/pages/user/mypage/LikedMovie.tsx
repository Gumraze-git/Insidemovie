import { useNavigate } from "react-router-dom";
import React, { useEffect, useState, useRef, useCallback } from "react";
import { useMediaQuery } from "react-responsive";
import MovieItem from "../../../components/MovieItem";
import ArrowRight from "@assets/arrow_right.svg?react";
import { memberApi } from "../../../api/memberApi";
import { Pagination } from "@mui/material";

interface Movie {
    movieId: number;
    posterPath: string;
    title: string;
    mainEmotion: string;
    mainEmotionValue: number;
    voteAverage: number;
    ratingAvg: number;
}

const LikedMovie: React.FC = () => {
    const navigate = useNavigate();
    const [movieList, setMovieList] = useState<Movie[]>([]);
    const [page, setPage] = useState(0);
    const [pageSize] = useState(40);
    const [totalPages, setTotalPages] = useState(0);
    // mobile infinite scroll / desktop pagination
    const isMobile = useMediaQuery({ query: "(max-width: 767px)" });
    const [isLoading, setIsLoading] = useState(false);
    const [isLastPage, setIsLastPage] = useState(false);
    const observer = useRef<IntersectionObserver | null>(null);

    useEffect(() => {
        const fetchLiked = async () => {
            setIsLoading(true);
            try {
                const res = await memberApi().getMyLikedMovies({
                    page,
                    pageSize,
                });
                const { content, totalPages: tp } = res.data.data;
                setTotalPages(tp);
                const lastFlag = page >= tp - 1;
                setIsLastPage(lastFlag);
                if (isMobile && page > 0) {
                    setMovieList((prev) => [...prev, ...content]);
                } else {
                    setMovieList(content);
                }
            } catch (e) {
                console.error("내가 좋아요한 영화 조회 에러: ", e);
            } finally {
                setIsLoading(false);
            }
        };
        fetchLiked();
    }, [page, pageSize, isMobile]);

    // detect last item on mobile to load more
    const lastItemRef = useCallback(
        (node: HTMLDivElement) => {
            if (!isMobile || isLoading) return;
            if (observer.current) observer.current.disconnect();
            observer.current = new IntersectionObserver((entries) => {
                if (entries[0].isIntersecting && !isLastPage) {
                    setPage((prev) => prev + 1);
                }
            });
            if (node) observer.current.observe(node);
        },
        [isMobile, isLoading, isLastPage],
    );

    return (
        <div>
            <div className="flex justify-center">
                <div className="max-w-screen-lg w-full flex flex-col pt-20 px-5">
                    <h1 className="flex gap-4 items-center text-white text-3xl font-semibold text-left pb-3 border-b-[1px] border-box_bg_white">
                        <p
                            className="font-extralight cursor-pointer hover:font-normal"
                            onClick={() => {
                                navigate(-1);
                            }}
                        >
                            마이페이지
                        </p>
                        <ArrowRight />
                        <p>좋아요 한 영화</p>
                    </h1>
                    {movieList.length === 0 ? (
                        <div className="flex items-center justify-center h-64">
                            <p className="text-white font-extralight text-lg">
                                좋아요 한 영화가 없습니다.
                            </p>
                        </div>
                    ) : (
                        <>
                            <div className="min-h-48 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 mt-4 mb-20">
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
                            {/* Sentinel for mobile infinite scroll */}
                            {isMobile && !isLastPage && (
                                <div
                                    ref={lastItemRef}
                                    style={{ height: 1, marginBottom: 20 }}
                                />
                            )}
                            {!isMobile && totalPages > 1 && (
                                <div className="flex justify-center text-white mt-6 mb-36">
                                    <Pagination
                                        count={totalPages}
                                        page={page + 1}
                                        onChange={(e, value) =>
                                            setPage(value - 1)
                                        }
                                        siblingCount={1}
                                        boundaryCount={1}
                                        showFirstButton
                                        showLastButton
                                        color="primary"
                                        sx={{
                                            // Make all pagination text white
                                            "& .MuiPaginationItem-root": {
                                                color: "#fff",
                                            },
                                        }}
                                    />
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LikedMovie;
