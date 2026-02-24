import * as React from "react";
import { useEffect, useRef, useState } from "react";
import SearchIcon from "@assets/search.svg?react";
import joyIcon from "@assets/character/joy_icon.png";
import sadIcon from "@assets/character/sad_icon.png";
import angryIcon from "@assets/character/angry_icon.png";
import fearIcon from "@assets/character/fear_icon.png";
import disgustIcon from "@assets/character/disgust_icon.png";
import Button from "../Button";
import { movieApi } from "../../api/movieApi";

export type EmotionKey = "joy" | "sad" | "angry" | "fear" | "disgust";

export interface EmotionAverages {
    joy: number;
    sad: number;
    angry: number;
    fear: number;
    disgust: number;
}

export interface EmotionSelectionResult {
    emotionAverages: EmotionAverages;
    dominantEmotion: EmotionKey;
}

interface MovieEmotionStepProps {
    completeButtonText?: string;
    onComplete: (result: EmotionSelectionResult) => void;
}

const emotionMap = {
    joy: joyIcon,
    sad: sadIcon,
    angry: angryIcon,
    fear: fearIcon,
    disgust: disgustIcon,
};

const emotionColorMap = {
    joy: "bg-joy_yellow",
    sad: "bg-sad_blue",
    angry: "bg-angry_red",
    fear: "bg-fear_purple",
    disgust: "bg-disgust_green",
};

const initialAverages: EmotionAverages = {
    joy: 0,
    sad: 0,
    angry: 0,
    fear: 0,
    disgust: 0,
};

const MovieEmotionStep: React.FC<MovieEmotionStepProps> = ({
    completeButtonText = "선택완료",
    onComplete,
}) => {
    const [searchTerm, setSearchTerm] = useState("");
    const [searchResults, setSearchResults] = useState<
        Array<{
            id: number;
            posterPath: string;
            title: string;
        }>
    >([]);
    const [selectedMovies, setSelectedMovies] = useState<
        Array<{
            id: number;
            posterPath: string;
            title: string;
        }>
    >([]);

    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [loadingMovies, setLoadingMovies] = useState(false);
    const resultsRef = useRef<HTMLDivElement | null>(null);
    const previewRef = useRef<HTMLDivElement | null>(null);

    const [emotionAverages, setEmotionAverages] =
        useState<EmotionAverages>(initialAverages);
    const [dominantEmotion, setDominantEmotion] = useState<EmotionKey>("joy");

    useEffect(() => {
        if (selectedMovies.length === 0) {
            setDominantEmotion("joy");
            return;
        }

        const entries = Object.entries(emotionAverages) as [EmotionKey, number][];
        const maxValue = Math.max(...entries.map(([, value]) => value));
        if (maxValue === 0) {
            setDominantEmotion("joy");
            return;
        }

        const topEmotion = entries.find(([, value]) => value === maxValue)?.[0];
        setDominantEmotion(topEmotion ?? "joy");
    }, [emotionAverages, selectedMovies]);

    useEffect(() => {
        if (previewRef.current) {
            previewRef.current.scrollTo({
                left: previewRef.current.scrollWidth,
                behavior: "smooth",
            });
        }
    }, [selectedMovies]);

    useEffect(() => {
        const fetchEmotions = async () => {
            if (selectedMovies.length === 0) {
                setEmotionAverages(initialAverages);
                return;
            }

            try {
                const responses = await Promise.all(
                    selectedMovies.map((movie) =>
                        movieApi().getMovieEmotions({ movieId: movie.id }),
                    ),
                );

                const totals = {
                    joy: 0,
                    anger: 0,
                    sadness: 0,
                    fear: 0,
                    disgust: 0,
                };

                responses.forEach((response) => {
                    const data = response.data;
                    totals.joy += Number(data.joy) || 0;
                    totals.anger += Number(data.anger) || 0;
                    totals.sadness += Number(data.sadness) || 0;
                    totals.fear += Number(data.fear) || 0;
                    totals.disgust += Number(data.disgust) || 0;
                });

                const count = selectedMovies.length || 1;
                setEmotionAverages({
                    joy: totals.joy / count,
                    angry: totals.anger / count,
                    sad: totals.sadness / count,
                    fear: totals.fear / count,
                    disgust: totals.disgust / count,
                });
            } catch (error) {
                console.error("Emotion fetch error", error);
            }
        };

        void fetchEmotions();
    }, [selectedMovies]);

    useEffect(() => {
        setPage(0);
        setHasMore(true);
    }, [searchTerm]);

    useEffect(() => {
        const fetchMovies = async () => {
            setLoadingMovies(true);
            try {
                if (!searchTerm) {
                    const response = await movieApi().getPopularMovies({
                        page: 0,
                        pageSize: 8,
                    });
                    const mapped = response.data.results.map((movie) => ({
                        id: movie.id,
                        posterPath: movie.poster_path,
                        title: movie.title,
                    }));
                    setSearchResults(mapped);
                    setHasMore(false);
                } else {
                    const response = await movieApi().searchTitle({
                        title: searchTerm,
                        page,
                        pageSize: 10,
                    });
                    const { content, last } = response.data;
                    setSearchResults((prev) =>
                        page === 0 ? content : [...prev, ...content],
                    );
                    setHasMore(!last);
                }
            } catch (error) {
                console.error(error);
                setHasMore(false);
            } finally {
                setLoadingMovies(false);
            }
        };

        void fetchMovies();
    }, [searchTerm, page]);

    const handleScroll = (event: React.UIEvent<HTMLDivElement>) => {
        const { scrollTop, scrollHeight, clientHeight } = event.currentTarget;
        if (scrollHeight - scrollTop <= clientHeight + 50 && hasMore && !loadingMovies) {
            setPage((prev) => prev + 1);
        }
    };

    const isDisabled = selectedMovies.length === 0;

    return (
        <div className="w-full transition-all duration-300">
            <div className="bg-box_bg_white px-4 py-2 rounded-3xl mb-6 ring-2 ring-purple-500 ring-opacity-50 shadow-[0_0_10px_rgba(124,106,255,1.0)] transition-shadow duration-300">
                <div className="flex gap-2 items-center justify-center">
                    {[
                        { icon: "joy", value: emotionAverages.joy },
                        { icon: "sad", value: emotionAverages.sad },
                        { icon: "angry", value: emotionAverages.angry },
                        { icon: "fear", value: emotionAverages.fear },
                        { icon: "disgust", value: emotionAverages.disgust },
                    ].map((emotion, index) => (
                        <div key={index} className="flex flex-1 items-center gap-1">
                            <img
                                src={emotionMap[emotion.icon as EmotionKey]}
                                alt={emotion.icon}
                                className="w-4 h-4"
                            />
                            <div
                                className="h-2 w-full rounded-full bg-box_bg_white overflow-hidden"
                                style={{
                                    width: `${Math.round(emotion.value * 100)}%`,
                                }}
                            >
                                <div
                                    className={`h-full rounded-full ${
                                        emotionColorMap[emotion.icon as EmotionKey]
                                    }`}
                                />
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="flex justify-between items-center mb-2 px-1">
                <div className="text-white font-semibold text-xl">
                    좋아하는 영화를 골라주세요!
                </div>
            </div>
            <div className="text-white font-light mb-5 text-sm px-1">
                당신의 감정 취향을 분석해 맞춤 영화를 찾아드릴게요.
            </div>

            <div className="w-full mb-6">
                <div className="relative">
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={(event) => setSearchTerm(event.target.value)}
                        placeholder="영화를 검색하세요."
                        className="w-full py-4 px-5 pr-12 rounded-full bg-box_bg_white text-white text-sm font-light placeholder-white placeholder-opacity-60 focus:outline-none transition-shadow duration-300"
                    />
                    <div className="absolute top-1/2 right-4 transform -translate-y-1/2 text-white">
                        <SearchIcon className="w-7 h-7 opacity-40" />
                    </div>
                </div>
            </div>

            <div
                className="flex space-x-2 mb-4 overflow-x-auto hide-scrollbar transition-all duration-300 ease-in-out"
                style={{
                    msOverflowStyle: "none",
                    scrollbarWidth: "none",
                }}
                ref={previewRef}
            >
                {selectedMovies.map((movie) => (
                    <div
                        key={movie.id}
                        className="relative flex-shrink-0 transition-all duration-300 ease-in-out transform"
                    >
                        <img
                            src={movie.posterPath}
                            alt={movie.title}
                            className="w-12 h-16 rounded"
                        />
                        <button
                            type="button"
                            onClick={() =>
                                setSelectedMovies((prev) =>
                                    prev.filter((selected) => selected.id !== movie.id),
                                )
                            }
                            className="absolute -top-1 -right-1 bg-black bg-opacity-50 text-white w-4 h-4 flex items-center justify-center rounded-full"
                        >
                            ×
                        </button>
                    </div>
                ))}
            </div>

            <div
                className="grid grid-cols-4 gap-4 mb-8 h-64 overflow-y-auto bg-box_bg_white p-2 rounded-3xl hide-scrollbar"
                style={{
                    msOverflowStyle: "none",
                    scrollbarWidth: "none",
                }}
                ref={resultsRef}
                onScroll={handleScroll}
            >
                {searchResults.map((movie) => {
                    const isSelected = selectedMovies.some(
                        (selected) => selected.id === movie.id,
                    );
                    return (
                        <div
                            key={movie.id}
                            onClick={() => {
                                setSelectedMovies((prev) =>
                                    isSelected
                                        ? prev.filter((selected) => selected.id !== movie.id)
                                        : [...prev, movie],
                                );
                            }}
                            className={`cursor-pointer rounded border-2 h-48 flex flex-col items-center justify-start ${
                                isSelected
                                    ? "border-movie_point"
                                    : "border-transparent"
                            }`}
                        >
                            <img
                                src={movie.posterPath}
                                alt={movie.title}
                                className="w-full object-contain"
                            />
                            <p className="text-white text-center mt-1 text-sm line-clamp-2 overflow-hidden">
                                {movie.title}
                            </p>
                        </div>
                    );
                })}
                {loadingMovies && (
                    <div className="col-span-4 text-center text-xs text-gray-400 py-2">
                        불러오는 중...
                    </div>
                )}
            </div>

            <Button
                text={completeButtonText}
                textColor="white"
                buttonColor={isDisabled ? "disabled" : "default"}
                disabled={isDisabled}
                className="w-full"
                onClick={() =>
                    onComplete({
                        emotionAverages,
                        dominantEmotion,
                    })
                }
            />
        </div>
    );
};

export default MovieEmotionStep;
