import * as React from "react";
import ArrowRight from "@assets/arrow_right.svg?react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { Review } from "../../interfaces/review";
import MyReviewItem from "../MyReviewItem";
import { memberApi } from "../../api/memberApi";

interface ReviewSectionProps {
    className?: string;
}

const MyReviewSection: React.FC<ReviewSectionProps> = ({ className = "" }) => {
    const navigate = useNavigate();
    const [reviewList, setReviewList] = useState<Review[]>([]);

    useEffect(() => {
        (async () => {
            try {
                const res = await memberApi().getMyReviews({
                    page: 0,
                    pageSize: 3,
                });
                const { content } = res.data.data;
                setReviewList(content);
            } catch (e) {
                console.error("맞춤 영화 조회 에러!! : ", e);
            }
        })();
    }, []);

    return (
        <section className={`w-full ${className}`}>
            <h1
                onClick={() => navigate("/mypage/my-review")}
                className="inline-flex items-center gap-2 cursor-pointer text-xl font-semibold mb-4 text-white transform transition-transform duration-200 hover:scale-105"
            >
                내가 쓴 리뷰
                <ArrowRight />
            </h1>
            <div className="flex flex-col gap-3 overflow-x-hidden scrollbar-hide px-2">
                {reviewList.map((review) => (
                    <MyReviewItem
                        reviewId={review.reviewId}
                        content={review.content}
                        rating={review.rating}
                        spoiler={false}
                        createdAt={review.createdAt}
                        likeCount={review.likeCount}
                        myReview={review.myReview}
                        modify={review.modify}
                        myLike={review.myLike}
                        nickname={review.nickname}
                        memberId={review.memberId}
                        movieId={review.movieId}
                        profile={review.memberEmotion}
                        emotion={review.emotion}
                        isReported={review.isReported}
                        isConcealed={review.isConcealed}
                        isMypage={true}
                    />
                ))}
            </div>
        </section>
    );
};

export default MyReviewSection;
