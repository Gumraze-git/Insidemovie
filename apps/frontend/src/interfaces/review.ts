export interface Review {
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
    memberEmotion: string;
    emotion: Emotion;
    isReported: boolean;
    isConcealed: boolean;
}

export interface Emotion {
    repEmotion: string;
    joy: number;
    sadness: number;
    anger: number;
    fear: number;
    disgust: number;
}
