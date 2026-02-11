import React, { useEffect, useState, useRef, useCallback } from "react";
import { useMediaQuery } from "react-responsive";
import StarRating from "../../../components/StarRating";
import joyIcon from "@assets/character/joy_icon.png";
import sadIcon from "@assets/character/sad_icon.png";
import angryIcon from "@assets/character/angry_icon.png";
import fearIcon from "@assets/character/fear_icon.png";
import disgustIcon from "@assets/character/disgust_icon.png";
import bingbongIcon from "@assets/character/bingbong_icon.png";
import { movieApi } from "../../../api/movieApi";
import type { MovieOne } from "../../../interfaces/movieOne";
import type { Review } from "../../../interfaces/review";
import Button from "../../../components/Button";
import ReviewItem from "../../../components/ReviewItem";
import { useNavigate, useParams } from "react-router-dom";
import { Select, MenuItem, Pagination } from "@mui/material";
import MyReviewItem from "../../../components/MyReviewItem";
import Like from "@assets/like.svg?react";
import Unlike from "@assets/unlike.svg?react";
import { reviewApi } from "../../../api/reviewApi";
import type { SelectChangeEvent } from "@mui/material/Select";
import TMDB from "@assets/TMDB.svg?react";
import Netflix from "@assets/netflix.png";
import Watcha from "@assets/watcha.png";
import Wavve from "@assets/wavve.png";
import AppleTVPlus from "@assets/appleTV+.png";
import DisneyPlus from "@assets/disneyPlus.png";

const emotionMap = {
    joy: joyIcon,
    sad: sadIcon,
    angry: angryIcon,
    fear: fearIcon,
    disgust: disgustIcon,
    bingbong: bingbongIcon,
};

const emotionColorMap = {
    joy: "bg-joy_yellow",
    sad: "bg-sad_blue",
    angry: "bg-angry_red",
    fear: "bg-fear_purple",
    disgust: "bg-disgust_green",
    bingbong: "bg-bingbong_pink",
};

const MovieDetail: React.FC = () => {
    const { movieId } = useParams<{ movieId: string }>();
    const movieIdNumber = Number(movieId);
    const navigate = useNavigate();
    const [movieInfo, setMovieInfo] = useState<MovieOne | null>(null);
    const [myReview, setMyReview] = useState<Review | null>(null);
    const [reviewList, setReviewList] = useState<Review[]>([]);
    const [myReviewLoaded, setMyReviewLoaded] = useState<boolean>(false);
    // 감정평가 API 결과 저장
    const [emotionStats, setEmotionStats] = useState<{
        joy: number;
        anger: number;
        sadness: number;
        fear: number;
        disgust: number;
        dominantEmotion: string;
    } | null>(null);

    // Review list pagination & sort
    const [reviewPage, setReviewPage] = useState(0);
    const [reviewTotalPages, setReviewTotalPages] = useState(0);
    const [reviewSort, setReviewSort] = useState<
        "POPULAR" | "LATEST" | "OLDEST"
    >("POPULAR");

    // Determine login status based on stored access token
    const isLogin = Boolean(localStorage.getItem("accessToken"));

    // Mobile infinite scroll / desktop pagination
    const isMobile = useMediaQuery({ query: "(max-width: 767px)" });
    const [isLoading, setIsLoading] = useState(false);
    const [isLastPage, setIsLastPage] = useState(false);
    const observer = useRef<IntersectionObserver | null>(null);

    useEffect(() => {
        (async () => {
            try {
                // 영화 상세 조회
                const detailRes = await movieApi().getMovieDetail({
                    movieId: movieIdNumber,
                });
                setMovieInfo(detailRes.data.data);
                // 영화 감정 조회
                const emotionRes = await movieApi().getMovieEmotions({
                    movieId: movieIdNumber,
                });
                setEmotionStats(emotionRes.data.data);
                // 내 리뷰 단건 조회
                try {
                    const myRes = await reviewApi().getMyReview({
                        movieId: movieIdNumber,
                    });
                    setMyReview(myRes.data.data);
                } catch {
                    setMyReview(null);
                } finally {
                    setMyReviewLoaded(true);
                }
                // 전체 리뷰 목록 조회 (mobile infinite scroll, desktop pagination)
                setIsLoading(true);
                const listRes = await reviewApi().getReviewList({
                    movieId: movieIdNumber,
                    sort: reviewSort,
                    page: reviewPage,
                    size: 10,
                });
                const { content, totalPages: tp, last } = listRes.data.data;
                if (isMobile && reviewPage > 0) {
                    setReviewList((prev) => [...prev, ...content]);
                } else {
                    setReviewList(content);
                }
                setReviewTotalPages(tp);
                setIsLastPage(last);
                setIsLoading(false);
            } catch (e) {
                console.error("영화 상세 정보 조회 에러: ", e);
                setIsLoading(false);
            }
        })();
    }, [movieIdNumber, reviewPage, reviewSort, isMobile]);
    const handleReviewPageChange = (
        _: React.ChangeEvent<unknown>,
        value: number,
    ) => {
        setReviewPage(value - 1);
    };
    const handleReviewSortChange = (
        e: SelectChangeEvent<"POPULAR" | "LATEST" | "OLDEST">,
    ) => {
        setReviewSort(e.target.value as "POPULAR" | "LATEST" | "OLDEST");
        setReviewPage(0);
        setReviewList([]);
        setIsLastPage(false);
    };

    // Attach to last ReviewItem on mobile
    const lastReviewRef = useCallback(
        (node: HTMLDivElement) => {
            if (!isMobile || isLoading) return;
            if (observer.current) observer.current.disconnect();
            observer.current = new IntersectionObserver((entries) => {
                if (entries[0].isIntersecting && !isLastPage) {
                    setReviewPage((prev) => prev + 1);
                }
            });
            if (node) observer.current.observe(node);
        },
        [isMobile, isLoading, isLastPage],
    );

    // 좋아요/취소 토글 핸들러
    const handleLikeClick = async () => {
        if (!movieInfo) return;
        try {
            if (movieInfo.isLike) {
                await movieApi().likeMovie({ movieId: movieIdNumber });
            } else {
                await movieApi().likeMovie({ movieId: movieIdNumber });
            }
            // 로컬 state 토글
            setMovieInfo((prev) =>
                prev ? { ...prev, isLike: !prev.isLike } : prev,
            );
        } catch (e) {
            console.error("좋아요 토글 실패:", e);
        }
    };

    // 데이터 로딩 전에는 아무것도 렌더링하지 않음
    if (!movieInfo) {
        return (
            <div className="w-full h-full flex justify-center items-center text-white">
                로딩 중...
            </div>
        );
    }

    return (
        <div className="relative">
            <div className="absolute -top-[100px] left-0 w-full h-[840px] z-10">
                <img
                    src={movieInfo.backdropPath}
                    alt="backdrop"
                    className="w-full h-full object-cover opacity-50 blur-xs"
                    style={{
                        WebkitMaskImage:
                            "linear-gradient(to top, transparent 0%, black 100%)",
                        maskImage:
                            "linear-gradient(to top, transparent 0%, black 100%)",
                    }}
                />
            </div>
            <div className="flex justify-center relative z-10 pt-96 mx-5">
                <div className="max-w-screen-lg w-full">
                    {/* 상단: 포스터 + 정보 */}
                    <div className="flex flex-col items-center md:items-start md:flex-row gap-10 text-white">
                        <img
                            src={movieInfo.posterPath}
                            alt={movieInfo.title}
                            className="w-80 h-fit rounded-md"
                        />
                        <div>
                            <div className="flex items-center gap-2">
                                <h1 className="text-4xl font-normal">
                                    {movieInfo.title}
                                </h1>
                                <button
                                    className="ml-1 p-2 hover:bg-box_bg_white rounded-full transition-all duration-300"
                                    onClick={handleLikeClick}
                                >
                                    {movieInfo.isLike ? <Like /> : <Unlike />}
                                </button>
                            </div>
                            <div className="mt-2 font-light text-sm text-grey_200 mb-2">
                                {movieInfo.titleEn}
                            </div>
                            <div className="flex items-center">
                                <StarRating
                                    value={movieInfo.ratingAvg}
                                    readOnly={true}
                                    showValue={true}
                                    showOneStar={true}
                                />
                                <TMDB className="x-4 y-4 ml-4 mr-1" />
                                {movieInfo.voteAverage}
                            </div>
                            <div className="flex gap-2 mt-4">
                                {movieInfo.ottProviders.map((provider, idx) => {
                                    if (provider === "Netflix")
                                        return (
                                            <a
                                                key={`netflix-${idx}`}
                                                href="https://www.netflix.com"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                            >
                                                <img
                                                    src={Netflix}
                                                    alt="Netflix"
                                                    className="w-6 h-6"
                                                />
                                            </a>
                                        );
                                    if (provider.includes("Watcha"))
                                        return (
                                            <a
                                                key={`watcha-${idx}`}
                                                href="https://watcha.com"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                            >
                                                <img
                                                    src={Watcha}
                                                    alt="Watcha"
                                                    className="w-6 h-6"
                                                />
                                            </a>
                                        );
                                    if (
                                        provider.toLowerCase().includes("wavve")
                                    )
                                        return (
                                            <a
                                                key={`wavve-${idx}`}
                                                href="https://www.wavve.com"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                            >
                                                <img
                                                    src={Wavve}
                                                    alt="Wavve"
                                                    className="w-6 h-6"
                                                />
                                            </a>
                                        );
                                    if (provider.includes("Apple TV+"))
                                        return (
                                            <a
                                                key={`apple-${idx}`}
                                                href="https://tv.apple.com"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                            >
                                                <img
                                                    src={AppleTVPlus}
                                                    alt="Apple TV+"
                                                    className="w-6 h-6"
                                                />
                                            </a>
                                        );
                                    if (provider.includes("Disney Plus"))
                                        return (
                                            <a
                                                key={`disney-${idx}`}
                                                href="https://www.disneyplus.com"
                                                target="_blank"
                                                rel="noopener noreferrer"
                                            >
                                                <img
                                                    src={DisneyPlus}
                                                    alt="Disney Plus"
                                                    className="w-6 h-6"
                                                />
                                            </a>
                                        );
                                    return null;
                                })}
                            </div>
                            <div className="mt-3 space-y-1 text-sm text-gray-300">
                                <p>개봉일 : {movieInfo.releaseDate}</p>
                                <p>상영 시간 : {movieInfo.runtime}분</p>
                                <p>
                                    장르 :{" "}
                                    {movieInfo.genre.length > 0
                                        ? movieInfo.genre.join(", ")
                                        : "장르 정보 없음"}
                                </p>
                                <p>
                                    언어 :{" "}
                                    {movieInfo.originalLanguage.toUpperCase()}
                                </p>
                                <p>
                                    감독 :{" "}
                                    {movieInfo.director.length > 0
                                        ? movieInfo.director.join(", ")
                                        : "감독 정보 없음"}
                                </p>
                                <p
                                    className="mt-1 text-sm text-gray-300 overflow-hidden"
                                    style={{
                                        display: "-webkit-box",
                                        WebkitLineClamp: 5,
                                        WebkitBoxOrient: "vertical",
                                    }}
                                >
                                    배우 :{" "}
                                    {movieInfo.actors.length > 0
                                        ? movieInfo.actors.join(", ")
                                        : "배우 정보 없음"}
                                </p>
                            </div>
                        </div>
                        {/*<img src={Background} className={"w-[80px]"} />*/}
                    </div>

                    {/* 감정 평가 & 시놉시스 */}
                    <div className="flex flex-col md:flex-row gap-10 mt-10">
                        <div className="flex-1 bg-box_bg_white p-6 rounded-3xl">
                            <div className="flex items-center mb-4 text-white">
                                <h2 className="text-3xl  font-bold">
                                    감정 평가
                                </h2>
                                <p className="font-light text-xs ml-2">
                                    | 사용자의 리뷰를 바탕으로 제작되었습니다.
                                </p>
                            </div>

                            {/* 감정 바: API 결과 사용 */}
                            {emotionStats &&
                                [
                                    { icon: "joy", value: emotionStats.joy },
                                    {
                                        icon: "angry",
                                        value: emotionStats.anger,
                                    },
                                    {
                                        icon: "sad",
                                        value: emotionStats.sadness,
                                    },
                                    { icon: "fear", value: emotionStats.fear },
                                    {
                                        icon: "disgust",
                                        value: emotionStats.disgust,
                                    },
                                    // optional: use dominantEmotion if you have it
                                ].map((e, i) => (
                                    <div
                                        key={i}
                                        className="flex items-center gap-1 mb-2"
                                    >
                                        <img
                                            src={emotionMap[e.icon]}
                                            alt={e.icon}
                                            className="w-10 h-10"
                                        />
                                        <div className="w-full h-2 rounded-full bg-box_bg_white overflow-hidden">
                                            <div
                                                className={`${emotionColorMap[e.icon]} h-full rounded-full`}
                                                style={{
                                                    width: `${Math.round(e.value)}%`,
                                                }}
                                            />
                                        </div>
                                        <span className="ml-2 w-12 text-sm text-white">
                                            {Math.round(e.value)}%
                                        </span>
                                    </div>
                                ))}
                        </div>
                        <div className="flex-1 bg-box_bg_white p-6 rounded-3xl text-white">
                            <h2 className="text-3xl  font-bold">시놉시스</h2>
                            <p className="text-sm text-gray-300 leading-relaxed mt-5">
                                {movieInfo.overview}
                            </p>
                        </div>
                    </div>

                    {/* 내가 쓴 리뷰 */}
                    {/* 내가 쓴 리뷰 및 리뷰 목록 */}
                    {myReviewLoaded &&
                        (myReview ? (
                            <MyReviewItem
                                reviewId={myReview.reviewId}
                                content={myReview.content}
                                rating={myReview.rating}
                                spoiler={false}
                                createdAt={myReview.createdAt}
                                likeCount={myReview.likeCount}
                                myReview={true}
                                modify={myReview.modify}
                                myLike={myReview.myLike}
                                nickname={myReview.nickname}
                                memberId={myReview.memberId}
                                movieId={myReview.movieId}
                                profile={myReview.memberEmotion}
                                emotion={myReview.emotion}
                                isReported={myReview.isReported}
                                isConcealed={myReview.isConcealed}
                                isMypage={false}
                                {...myReview}
                            />
                        ) : (
                            <div className="flex justify-center mt-10 p-10 rounded-3xl border border-white/20">
                                <Button
                                    text="리뷰 작성 하기"
                                    onClick={() => {
                                        if (!isLogin) {
                                            navigate("/login");
                                            return;
                                        }
                                        navigate(`/review-write/${movieId}`);
                                    }}
                                />
                            </div>
                        ))}

                    {/* 리뷰 목록 */}
                    <div className="mt-12 mb-36">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-3xl font-semibold text-white">
                                리뷰
                            </h2>
                            <Select
                                value={reviewSort}
                                onChange={handleReviewSortChange}
                                size="small"
                                sx={{
                                    color: "#fff",
                                    ".MuiOutlinedInput-notchedOutline": {
                                        borderColor: "transparent",
                                    },
                                    ".MuiSvgIcon-root": { color: "#fff" },
                                }}
                            >
                                <MenuItem value="POPULAR">인기순</MenuItem>
                                <MenuItem value="LATEST">최신순</MenuItem>
                                <MenuItem value="OLDEST">오래된순</MenuItem>
                            </Select>
                        </div>
                        {reviewList.length === 0 ? (
                            <div className="text-center text-white py-10 mb-36">
                                리뷰가 없습니다
                            </div>
                        ) : (
                            reviewList.map((review, idx) => {
                                const isLast =
                                    isMobile && idx === reviewList.length - 1;
                                return (
                                    <div
                                        key={review.reviewId}
                                        ref={isLast ? lastReviewRef : undefined}
                                    >
                                        <ReviewItem
                                            reviewId={review.reviewId}
                                            content={review.content}
                                            rating={review.rating}
                                            spoiler={review.spoiler}
                                            createdAt={review.createdAt}
                                            likeCount={review.likeCount}
                                            myReview={review.myReview}
                                            myLike={review.myLike}
                                            nickname={review.nickname}
                                            memberId={review.memberId}
                                            movieId={review.movieId}
                                            profile={review.memberEmotion}
                                            emotion={review.emotion}
                                            isReported={review.isReported}
                                            isConcealed={review.isConcealed}
                                            {...review}
                                        />
                                    </div>
                                );
                            })
                        )}
                        {!isMobile && reviewTotalPages > 0 && (
                            <div className="flex justify-center mt-20 mb-36">
                                <Pagination
                                    count={reviewTotalPages}
                                    page={reviewPage + 1}
                                    onChange={handleReviewPageChange}
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
export default MovieDetail;
