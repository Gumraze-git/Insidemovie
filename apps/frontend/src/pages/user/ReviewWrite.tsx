import * as React from "react";
import { useState, useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
// 에셋
import Button from "../../components/Button";
import TransparentBox from "../../components/TransparentBox";
// 날짜 라이브러리
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import CalendarIcon from "@assets/calendar.svg?react";
import ReactDatePicker from "react-datepicker";

// 평점 라이브러리
import StarRating from "../../components/StarRating";
import { reviewApi } from "../../api/reviewApi";
import { ConfirmDialog } from "../../components/ConfirmDialog";
import type { MovieOne } from "../../interfaces/movieOne";
import { movieApi } from "../../api/movieApi";

const ReviewWrite: React.FC = () => {
    const { movieId } = useParams<{ movieId: string }>();
    const [content, setContent] = useState<string>("");
    const [rating, setRating] = useState<number>(0);
    const [spoilerType, setSpoilerType] = useState<boolean>(false);
    const [watchedAt, setWatchedAt] = useState<Date | null>();

    const [movieInfo, setMovieInfo] = useState<MovieOne | null>(null);

    const [isEditMode, setIsEditMode] = useState(false);
    const [reviewId, setReviewId] = useState<number | null>(null);

    const navigate = useNavigate();
    const movieIdNumber = Number(movieId);

    const [isSuccessOpen, setIsSuccessOpen] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

    const [isErrorOpen, setIsErrorOpen] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");
    const [buttonEnabled, setButtonEnabled] = useState(true);

    // 달력 아이콘으로 달력 열기
    const datePickerRef = useRef<ReactDatePicker | null>(null);
    const openCalendar = () => {
        if (datePickerRef.current) {
            datePickerRef.current.setOpen(true);
        }
    };
    // 최초 마운트
    useEffect(() => {
        if (!movieId) return;
        (async () => {
            try {
                // 영화 상세 조회
                const detailRes = await movieApi().getMovieDetail({
                    movieId: movieIdNumber,
                });
                setMovieInfo(detailRes.data.data);
                const myRes = await reviewApi().getMyReview({
                    movieId: movieIdNumber,
                });
                const myData = myRes.data.data;
                setIsEditMode(true);
                setReviewId(myData.reviewId);
                setContent(myData.content);
                setRating(myData.rating);
                setWatchedAt(
                    myData.watchedAt ? new Date(myData.watchedAt) : null,
                );
                setSpoilerType(myData.spoiler);
            } catch {
                // no existing review
            }
        })();
    }, [movieId]);

    // 제출
    const handleSubmit = async () => {
        if (!content.trim()) {
            setErrorMessage("리뷰 내용을 입력해주세요.");
            setIsErrorOpen(true);
            return;
        }
        if (!watchedAt) {
            setErrorMessage("관람 일자를 선택해주세요.");
            setIsErrorOpen(true);
            return;
        }
        if (rating < 0.5 || rating > 5) {
            setErrorMessage("별점을 선택해주세요.");
            setIsErrorOpen(true);
            return;
        }

        try {
            setButtonEnabled(false);
            if (isEditMode && reviewId) {
                await reviewApi().modifyReview({
                    reviewId,
                    content,
                    rating,
                    spoiler: spoilerType,
                    watchedAt: watchedAt.toISOString(),
                });
                setSuccessMessage("수정");
                setIsSuccessOpen(true);
            } else {
                await reviewApi().createReview({
                    movieId: movieIdNumber,
                    content,
                    rating,
                    spoiler: spoilerType,
                    watchedAt: watchedAt.toISOString(),
                });
                setSuccessMessage("등록");
                setIsSuccessOpen(true);
                setButtonEnabled(true);
            }
        } catch (err) {
            console.error("리뷰 등록 실패:", err);
            setErrorMessage("리뷰 등록 중 오류가 발생했습니다.");
            setIsErrorOpen(true);
        }
    };

    if (!movieInfo) {
        return (
            <div className="w-full h-full flex justify-center items-center text-white">
                로딩 중...
            </div>
        );
    }

    return (
        <div className="flex justify-center">
            <div className="max-w-screen-lg w-full mx-5 mb-36">
                <div className="flex flex-col">
                    <div className="flex justify-between items-center pt-20 py-36 pb-6">
                        <h1 className="text-white text-3xl font-semibold text-left">
                            {isEditMode ? "리뷰 수정" : "리뷰 작성"}
                        </h1>
                    </div>
                    <div className="flex gap-10 text-white">
                        <img
                            src={movieInfo.posterPath}
                            alt={movieInfo.title}
                            className="w-20 object-contain"
                        />
                        <div className="flex flex-col justify-center">
                            <h1 className="text-4xl font-normal">
                                {movieInfo.title}
                            </h1>
                            <div className="mt-2 font-light text-sm text-grey_200">
                                {movieInfo.titleEn}
                            </div>
                        </div>
                    </div>

                    <div className="flex items-center gap-3 my-6 flex-wrap">
                        {/* 관람일 */}
                        <TransparentBox
                            className="flex justify-center text-white font-light text-xs gap-2 items-center"
                            padding="px-4 py-3"
                        >
                            <span>관람 일자</span>
                            <span>|</span>
                            <div
                                className="flex justify-center items-center cursor-pointer"
                                onClick={openCalendar}
                            >
                                <CalendarIcon className="w-4 h-4 me-2" />
                                <DatePicker
                                    popperClassName="z-[9999]"
                                    portalId="root-portal"
                                    ref={datePickerRef}
                                    selected={watchedAt}
                                    onChange={(date: Date | null) =>
                                        setWatchedAt(date)
                                    }
                                    showYearDropdown
                                    showMonthDropdown
                                    dropdownMode="select"
                                    maxDate={new Date()}
                                    placeholderText="YYYY-MM-DD"
                                    dateFormat="yyyy-MM-dd"
                                    className="cursor-pointer bg-transparent text-white placeholder-white outline-none rounded"
                                />
                            </div>
                        </TransparentBox>
                        <TransparentBox className="text-white text-xs py-2 px-4 flex justify-center items-center">
                            <StarRating
                                value={rating}
                                onChange={(value) => setRating(value)}
                                readOnly={false}
                                showOneStar={false}
                                showValue={false}
                                size={"medium"}
                            />
                        </TransparentBox>
                        <TransparentBox className="text-white text-xs py-2 px-4 gap-2 flex justify-between items-center">
                            <span>스포일러 포함</span>
                            <button
                                onClick={() => setSpoilerType(!spoilerType)}
                                className={`w-10 h-6 flex items-center rounded-full p-1 duration-300 ${
                                    spoilerType
                                        ? "bg-movie_sub"
                                        : "bg-box_bg_white"
                                }`}
                            >
                                <div
                                    className={`w-4 h-4 bg-white rounded-full shadow-md transform duration-300 ${
                                        spoilerType
                                            ? "translate-x-4"
                                            : "translate-x-0"
                                    }`}
                                />
                            </button>
                        </TransparentBox>
                    </div>
                    <textarea
                        placeholder="리뷰 내용을 입력하세요"
                        className="bg-box_bg_white h-[400px] text-white text-sm w-full p-4 outline-none focus:ring-1 focus:ring-movie_sub rounded-3xl"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                    />
                    <div className="flex items-center justify-end gap-3 my-6 flex-wrap">
                        <Button
                            text={isEditMode ? "수정하기" : "등록하기"}
                            textColor="white"
                            buttonColor="default"
                            disabled={!buttonEnabled}
                            onClick={handleSubmit}
                            className="px-16"
                        />
                    </div>
                </div>
            </div>
            <ConfirmDialog
                className={"max-w-md"}
                isOpen={isErrorOpen}
                title="오류"
                message={errorMessage}
                showCancel={false}
                isRedButton={true}
                onConfirm={() => setIsErrorOpen(false)}
                onCancel={function (): void {
                    throw new Error("Function not implemented.");
                }}
            />
            <ConfirmDialog
                className={"w-full max-w-md"}
                isOpen={isSuccessOpen}
                title={`리뷰 ${successMessage} 완료`}
                message={`리뷰가 ${successMessage}되었습니다.`}
                showCancel={false}
                isRedButton={false}
                onConfirm={() => {
                    setIsSuccessOpen(false);
                    navigate(-1);
                }}
                onCancel={() => setIsSuccessOpen(false)}
            />
        </div>
    );
};

export default ReviewWrite;
