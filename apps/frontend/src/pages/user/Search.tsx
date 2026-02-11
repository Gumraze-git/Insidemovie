import React, { useEffect, useState, useRef, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { movieApi } from "../../api/movieApi";
import MovieItem from "../../components/MovieItem";
import { Pagination } from "@mui/material";
import SearchIcon from "@assets/search.svg?react";
import CloseIcon from "@assets/close.svg?react";
import { useMediaQuery } from "react-responsive";

interface Movie {
    id: number;
    posterPath: string;
    title: string;
    mainEmotion: string;
    mainEmotionValue: number;
    voteAverage: number;
    ratingAvg: number;
}

const Search: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const title = searchParams.get("title") || "";
    const [page, setPage] = useState(0);
    const pageSize = 40;
    const [results, setResults] = useState<Movie[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const isMobile = useMediaQuery({ query: "(max-width: 767px)" });
    const [isLoading, setIsLoading] = useState(false);
    const [isLastPage, setIsLastPage] = useState(false);
    const observer = useRef<IntersectionObserver | null>(null);

    const [searchTerm, setSearchTerm] = useState("");

    const handleSearch = () => {
        const keyword = searchTerm.trim();
        if (keyword) {
            setSearchParams({ title: keyword });
            setPage(0);
        }
    };

    useEffect(() => {
        setSearchTerm(title);
    }, [title]);

    useEffect(() => {
        if (!title) return;
        const fetchResults = async () => {
            setIsLoading(true);
            try {
                const res = await movieApi().searchTitle({
                    title,
                    page,
                    pageSize,
                });
                const {
                    content,
                    totalPages: tp,
                    totalElements: te,
                } = res.data.data;
                setTotalPages(tp);
                setTotalElements(te);
                setIsLastPage(page >= tp - 1);
                if (isMobile && page > 0) {
                    setResults((prev) => [...prev, ...content]);
                } else {
                    setResults(content);
                }
            } catch (e) {
                console.error("영화 검색 실패:", e);
                if (!isMobile) setResults([]);
            } finally {
                setIsLoading(false);
            }
        };
        fetchResults();
    }, [title, page, isMobile]);

    const handlePageChange = (
        _e: React.ChangeEvent<unknown>,
        value: number,
    ) => {
        setPage(value - 1);
    };

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
        <div className="flex justify-center">
            <div className="max-w-screen-lg w-full flex flex-col items-center pt-20">
                <div className="flex w-full max-w-screen-sm mb-24">
                    <div className="relative w-full">
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === "Enter") handleSearch();
                            }}
                            placeholder="영화를 검색하세요."
                            className="w-full py-4 px-5 pr-12 rounded-full bg-box_bg_white text-white text-sm font-light placeholder-white placeholder-opacity-60 focus:outline-none"
                        />
                        {searchTerm && (
                            <div
                                className="absolute top-1/2 right-12 transform -translate-y-1/2 text-white cursor-pointer"
                                onClick={() => {
                                    setSearchTerm("");
                                }}
                            >
                                <CloseIcon className="w-5 h-5 opacity-60 hover:opacity-100 transition-opacity" />
                            </div>
                        )}
                        <div
                            className="absolute top-1/2 right-4 transform -translate-y-1/2 text-white"
                            onClick={handleSearch}
                            style={{ cursor: "pointer" }}
                        >
                            <SearchIcon className="w-7 h-7 opacity-40" />
                        </div>
                    </div>
                </div>

                <h1 className="flex gap-3 items-center text-white text-2xl font-semibold text-left w-full mb-4">
                    '{title}' <p className="font-extralight">검색 결과</p>{" "}
                    <p className="font-extralight text-sm">{totalElements}건</p>
                </h1>
                {results.length === 0 ? (
                    <div className="flex items-center justify-center h-64">
                        <p className="text-white text-lg">
                            검색 결과가 없습니다.
                        </p>
                    </div>
                ) : (
                    <>
                        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 mt-4 mb-20">
                            {results.map((movie, idx) => {
                                const isLast =
                                    isMobile && idx === results.length - 1;
                                return (
                                    <div
                                        key={movie.id}
                                        ref={isLast ? lastItemRef : undefined}
                                    >
                                        <MovieItem
                                            movieId={movie.id}
                                            posterImg={movie.posterPath}
                                            posterName={movie.title}
                                            emotionIcon={movie.mainEmotion.toLowerCase()}
                                            emotionValue={
                                                movie.mainEmotionValue
                                            }
                                            starValue={movie.voteAverage}
                                            ratingAvg={movie.ratingAvg}
                                        />
                                    </div>
                                );
                            })}
                        </div>
                        {!isMobile && totalPages > 1 && (
                            <div className="flex justify-center text-white mt-6 mb-36">
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
                    </>
                )}
            </div>
        </div>
    );
};
export default Search;
