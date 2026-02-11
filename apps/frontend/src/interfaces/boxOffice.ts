export interface boxOffice {
    movieId: number | null;
    title: string;
    posterPath: string;
    voteAverage: number;
    ratingAvg: number;
    mainEmotion: string;
    mainEmotionValue: number;
    base: boxOfficeBase;
}

export interface boxOfficeBase {
    id: number;
    rnum: string;
    rank: string;
    rankInten: string;
    rankOldAndNew: string;
    movieCd: string;
    movieNm: string;
    openDt: string;
    salesAmt: string;
    salesShare: string;
    salesInten: string;
    salesChange: string;
    salesAcc: string;
    audiCnt: string;
    audiInten: string;
    audiChange: string;
    audiAcc: string;
    scrnCnt: string;
    showCnt: string;
}
