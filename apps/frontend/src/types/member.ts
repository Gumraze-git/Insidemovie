export type Member = {
    id: number;
    email: string;
    nickname: string | null;
    reportCount: number;
    authority: string;
    reviewCount: number;
    banned: boolean;
    createdAt: string;
};
