import React, { useEffect, useState } from "react";
import { Snackbar, Alert } from "@mui/material";
import MovieItem from "../../components/MovieItem";
import Button from "../../components/Button";
import WinnerItem from "../../components/WinnerItem";
import { useNavigate } from "react-router-dom";
import { matchApi } from "../../api/matchApi";

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

const WeekMatch: React.FC = () => {
    const navigate = useNavigate();
    const [movieList, setMovieList] = useState<Movie[]>([]);
    const [pastWinners, setPastWinners] = useState<Winner[]>([]);
    // Selection & notification state
    const [selectedMovieId, setSelectedMovieId] = useState<number | null>(null);
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    // Login status
    const isLogin = Boolean(localStorage.getItem("accessToken"));
    // Error snackbar for duplicate vote
    const [errorSnackbarOpen, setErrorSnackbarOpen] = useState(false);
    const { getWeeklyMatchMovie, getPastMatchMovie, voteMatch } = matchApi();

    useEffect(() => {
        (async () => {
            try {
                const res = await getWeeklyMatchMovie();
                setMovieList(res.data.data);
            } catch (e) {
                console.error("금주의 영화 대결 조회 에러: ", e);
            }
        })();
    }, []);

    useEffect(() => {
        (async () => {
            try {
                const res = await getPastMatchMovie();
                setPastWinners(res.data.data);
            } catch (e) {
                console.error("역대 우승 영화 조회 에러:", e);
            }
        })();
    }, []);

    return (
        <div>
            <div className="flex justify-center">
                <div className="max-w-screen-lg w-full">
                    <div className="flex flex-col pt-20 mx-5">
                        <h1 className="text-center text-white text-3xl font-semibold pb-3 border-b-[1px] border-box_bg_white">
                            가장 마음에 드는 영화를 골라주세요
                        </h1>

                        <div className="flex flex-col items-center justify-center">
                            {movieList.length === 0 ? (
                                <div className="flex justify-center items-center gap-10 mt-10 text-center text-gray-400 py-12">
                                    아직 투표된 내용이 없습니다.
                                </div>
                            ) : (
                                <div className="flex flex-col md:flex-row justify-center items-center overflow-x-hidden gap-10 mt-10">
                                    {movieList.map((poster, idx) => (
                                        <React.Fragment key={idx}>
                                            <div className="flex flex-col">
                                                <MovieItem
                                                    key={poster.id}
                                                    className={`cursor-pointer rounded transition-all duration-300 ${selectedMovieId === poster.id ? "border-2 rounded-3xl border-movie_sub" : ""}`}
                                                    movieId={poster.id}
                                                    posterImg={
                                                        poster.posterPath
                                                    }
                                                    posterName={poster.title}
                                                    emotionIcon={
                                                        poster.mainEmotion
                                                    }
                                                    emotionValue={
                                                        poster.emotionValue
                                                    }
                                                    starValue={
                                                        poster.voteAverage
                                                    }
                                                    ratingAvg={poster.ratingAvg}
                                                />

                                                {/* 선택하기 링크 */}
                                                <div className="text-center mt-2">
                                                    <span
                                                        className="text-grey_200 underline cursor-pointer font-light"
                                                        onClick={() =>
                                                            setSelectedMovieId(
                                                                poster.id,
                                                            )
                                                        }
                                                    >
                                                        선택하기
                                                    </span>
                                                </div>
                                            </div>
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
                                className="w-1/2 mt-10"
                                text="투표하기"
                                textColor="white"
                                buttonColor={
                                    selectedMovieId ? "default" : "disabled"
                                }
                                disabled={!selectedMovieId}
                                onClick={async () => {
                                    if (!isLogin) {
                                        navigate("/login");
                                        return;
                                    }
                                    if (selectedMovieId) {
                                        try {
                                            await voteMatch({
                                                movieId: selectedMovieId,
                                            });
                                            setSnackbarOpen(true);
                                        } catch (e) {
                                            console.error("투표 에러:", e);
                                            setErrorSnackbarOpen(true);
                                        }
                                    }
                                }}
                            />
                        </div>

                        <div className="flex flex-col flex-1 mt-20">
                            <h1 className="flex items-center gap-2 text-2xl font-semibold mb-4 text-white">
                                역대 우승 영화
                            </h1>
                            <div className="flex flex-col items-center justify-center px-2 py-1 mb-36">
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
                                            emotionIcon={
                                                movie.movie.mainEmotion
                                            }
                                            emotionValue={
                                                movie.movie.emotionValue
                                            }
                                            starValue={movie.movie.voteAverage}
                                            winnerWeek={movie.matchDate}
                                            ratingAvg={movie.movie.ratingAvg}
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
                </div>
            </div>
            <Snackbar
                open={snackbarOpen}
                autoHideDuration={3000}
                onClose={() => setSnackbarOpen(false)}
                anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
            >
                <Alert
                    onClose={() => setSnackbarOpen(false)}
                    severity="success"
                    variant="filled"
                >
                    투표가 완료되었습니다!
                </Alert>
            </Snackbar>
            <Snackbar
                open={errorSnackbarOpen}
                autoHideDuration={3000}
                onClose={() => setErrorSnackbarOpen(false)}
                anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
            >
                <Alert
                    onClose={() => setErrorSnackbarOpen(false)}
                    severity="error"
                    variant="filled"
                >
                    이미 투표하셨습니다!
                </Alert>
            </Snackbar>
        </div>
    );
};
export default WeekMatch;
