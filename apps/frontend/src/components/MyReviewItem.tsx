import React, { useState } from "react";
import StarRating from "./StarRating";
import Unlike from "@assets/unlike.svg?react";
import joyIcon from "@assets/character/joy_icon.png";
import sadIcon from "@assets/character/sad_icon.png";
import angryIcon from "@assets/character/angry_icon.png";
import fearIcon from "@assets/character/fear_icon.png";
import disgustIcon from "@assets/character/disgust_icon.png";
import bingbongIcon from "@assets/character/bingbong_icon.png";
import { useNavigate } from "react-router-dom";
import Edit from "@assets/edit.svg?react";
import Delete from "@assets/delete.svg?react";
import ArrowRight from "@assets/arrow_right.svg?react";
import bingbongProfile from "@assets/profile/bingbong_profile.png";
import joyProfile from "@assets/profile/joy_profile.png";
import angryProfile from "@assets/profile/angry_profile.png";
import sadnessProfile from "@assets/profile/sad_profile.png";
import fearProfile from "@assets/profile/fear_profile.png";
import disgustProfile from "@assets/profile/disgust_profile.png";
import { ConfirmDialog } from "./ConfirmDialog";
import { reviewApi } from "../api/reviewApi";
import { timeForToday } from "../services/timeForToday";
import type { Emotion } from "../interfaces/review";

interface MyReviewItemProps {
    reviewId: number;
    content: string;
    rating: number;
    spoiler?: boolean;
    createdAt: string;
    likeCount: number;
    myReview?: boolean;
    modify?: boolean;
    myLike?: boolean;
    nickname: string;
    memberId: string;
    movieId: string;
    profile: string;
    emotion: Emotion;
    isReported: boolean;
    isConcealed: boolean;
    isMypage?: boolean;
}

const emotionMap = {
    joy: joyIcon,
    sadness: sadIcon,
    anger: angryIcon,
    fear: fearIcon,
    disgust: disgustIcon,
    bingbong: bingbongIcon,
    none: bingbongIcon,
};

const MyReviewItem: React.FC<MyReviewItemProps> = ({
    reviewId,
    content,
    rating,
    spoiler = false,
    createdAt,
    likeCount,
    myReview = false,
    modify = false,
    myLike = false,
    nickname,
    memberId,
    movieId,
    profile,
    emotion,
    isReported,
    isConcealed,
    isMypage = false,
}) => {
    const repEmotion = emotion?.repEmotion ?? "none";
    const navigate = useNavigate();

    const emotionProfileMap: Record<string, string> = {
        joy: joyProfile,
        anger: angryProfile,
        sadness: sadnessProfile,
        fear: fearProfile,
        disgust: disgustProfile,
        none: bingbongProfile,
    };

    const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);

    return (
        <div
            className={`bg-box_bg_white p-4 rounded-3xl text-white mb-3 ${!isMypage && "mt-10"}`}
        >
            <div className="flex items-center justify-between">
                <div className="flex gap-2 items-center">
                    <img
                        src={
                            profile
                                ? emotionProfileMap[profile?.toLowerCase()]
                                : bingbongProfile
                        }
                        alt="Profile"
                        className="h-8 w-8 rounded-full "
                    />
                    <div>
                        <div className="font-normal text-sm">
                            {nickname ? nickname : "알 수 없는 사용자"}
                        </div>
                        <div className="text-xs font-light text-gray-400">
                            {timeForToday(createdAt)}
                        </div>
                    </div>
                </div>
                <div className="flex items-center gap-1 bg-box_bg_white/10 px-2 py-1 rounded-full text-sm">
                    <img
                        src={emotionMap[repEmotion]}
                        alt={repEmotion}
                        className="w-6 h-6"
                    />
                    |
                    <StarRating
                        value={rating}
                        readOnly={true}
                        showOneStar={true}
                        showValue={true}
                        size={"small"}
                    />
                </div>
            </div>

            <div className="mt-3 text-[15px] leading-relaxed">
                <p className="px-2">{content}</p>
            </div>

            <div className="w-full h-[1px] bg-white/10 mt-4" />

            <div className="mt-4 flex justify-start items-center text-sm text-gray-300">
                <div className="flex items-center gap-1 hover:bg-box_bg_white rounded-full px-2 py-1 transition-all duration-200 cursor-pointer">
                    <Unlike className="w-5 h-5" />
                    {likeCount}
                </div>
                {!isMypage && (
                    <div className="flex">
                        <div
                            className="flex items-center gap-1 hover:bg-box_bg_white rounded-full px-2 py-1 transition-all duration-200 cursor-pointer"
                            onClick={() => {
                                navigate(`/review-write/${movieId}`);
                            }}
                        >
                            <Edit className="w-5 h-5" />
                            수정하기
                        </div>
                        <div
                            className="flex items-center gap-1 hover:bg-box_bg_white rounded-full px-2 py-1 transition-all duration-200 cursor-pointer"
                            onClick={() => setIsDeleteConfirmOpen(true)}
                        >
                            <Delete className="w-5 h-5" />
                            삭제하기
                        </div>
                    </div>
                )}
                {isMypage && (
                    <div
                        className="flex items-center gap-1 hover:bg-box_bg_white rounded-full px-2 py-1 transition-all duration-200 cursor-pointer"
                        onClick={() => {
                            navigate(`/movies/detail/${movieId}`);
                        }}
                    >
                        영화 보기
                        <ArrowRight className="w-5 h-5" />
                    </div>
                )}
            </div>
            <ConfirmDialog
                className={"w-full max-w-md"}
                isOpen={isDeleteConfirmOpen}
                title="리뷰 삭제"
                message="리뷰를 삭제하시겠습니까?"
                showCancel={true}
                isRedButton={true}
                onConfirm={async () => {
                    try {
                        await reviewApi().deleteReview({ reviewId });
                        window.location.reload();
                    } catch (e) {
                        console.error("리뷰 삭제 실패", e);
                    } finally {
                        setIsDeleteConfirmOpen(false);
                    }
                }}
                onCancel={() => setIsDeleteConfirmOpen(false)}
            />
        </div>
    );
};

export default MyReviewItem;
