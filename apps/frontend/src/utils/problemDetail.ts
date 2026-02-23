import { isAxiosError } from "axios";
import type { ProblemDetail } from "../types/problem";

const isRecord = (value: unknown): value is Record<string, unknown> => {
    return typeof value === "object" && value !== null;
};

export const extractProblemDetail = (error: unknown): ProblemDetail | null => {
    if (!isAxiosError(error)) {
        return null;
    }

    const data = error.response?.data;

    if (isRecord(data)) {
        return data as ProblemDetail;
    }

    return null;
};

export const getProblemMessage = (
    error: unknown,
    fallback = "요청 처리 중 오류가 발생했습니다.",
): string => {
    const problem = extractProblemDetail(error);

    if (problem?.detail && problem.detail.trim().length > 0) {
        return problem.detail;
    }

    if (problem?.errors && problem.errors.length > 0) {
        const first = problem.errors[0];
        if (first?.reason) {
            return first.reason;
        }
    }

    if (problem?.message && problem.message.trim().length > 0) {
        return problem.message;
    }

    if (error instanceof Error && error.message.trim().length > 0) {
        return error.message;
    }

    return fallback;
};
