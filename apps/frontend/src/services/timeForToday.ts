/**
 * 주어진 날짜/시간 값으로부터 현재까지의 경과를 사람이 읽기 좋은 형태로 반환합니다.
 * @param value Date 객체, ISO 문자열, 혹은 타임스탬프 숫자
 * @returns “방금 전”, “n초 전”, “n분 전”, “n시간 전” 또는 “YYYY.MM.DD HH:mm” 형식의 문자열
 */
export const timeForToday = (value: string | number | Date): string => {
    const now = new Date();
    const created = new Date(value);
    const diffSec = Math.floor((now.getTime() - created.getTime()) / 1000);

    if (isNaN(created.getTime()) || diffSec < 0) {
        return "";
    }
    if (diffSec < 5) {
        return "방금 전";
    }
    if (diffSec < 60) {
        return `${diffSec}초 전`;
    }

    const diffMin = Math.floor(diffSec / 60);
    if (diffMin < 60) {
        return `${diffMin}분 전`;
    }

    const diffHour = Math.floor(diffMin / 60);
    if (diffHour < 24) {
        return `${diffHour}시간 전`;
    }

    const Y = created.getFullYear();
    const M = String(created.getMonth() + 1).padStart(2, "0");
    const D = String(created.getDate()).padStart(2, "0");
    const h = String(created.getHours()).padStart(2, "0");
    const m = String(created.getMinutes()).padStart(2, "0");
    return `${Y}.${M}.${D} ${h}:${m}`;
};
