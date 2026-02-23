export interface ValidationErrorItem {
    field?: string;
    reason?: string;
    rejectedValue?: unknown;
}

export interface ProblemDetail {
    type?: string;
    title?: string;
    status?: number;
    detail?: string;
    instance?: string;
    code?: string;
    timestamp?: string;
    traceId?: string;
    errors?: ValidationErrorItem[];
    message?: string;
}
