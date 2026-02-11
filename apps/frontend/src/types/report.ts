export type Report = {
    reportId: number;
    reviewId: number;
    reviewContent: string;
    reporterId: number;
    reporterNickname: string;
    reportedMemberId: number;
    reportedNickname: string;
    reason: string;
    status: string;
    createdAt: string;
};
