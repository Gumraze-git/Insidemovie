import type { Report } from "../types/report"; // Report 타입 정의
import dateFormat from "./dateFormating";
import { statusDisplayMap } from "../pages/admin/internals/data/gridData";

import type { GridRowsProp } from "@mui/x-data-grid";
import type { ReportStatus } from "types/reportStatus";

export function mapReportsToRows(reports: Report[]): GridRowsProp {
    if (!reports) {
        return []; // null이나 undefined일 땐 빈 배열 반환
    }
    return reports.map((report) => {
        const statusKey = report.status as ReportStatus;
        const config = statusDisplayMap[statusKey];
        if (!config) {
            console.warn(`Unknown status key: ${statusKey}`);
            return {
                id: report.reportId,
                status: report.status,
                content: report.reviewContent,
                type: report.reason,
                reviewer: report.reportedNickname,
                reporter: report.reporterNickname,
                submissionTime: dateFormat(report.createdAt),

                reportStatus: "알 수 없음",
                reportResult: "알 수 없음",
            };
        }

        return {
            id: report.reportId, // DataGrid 고유키 필수
            status: report.status,
            content: report.reviewContent,
            type: report.reason,
            reviewer: report.reportedNickname,
            reporter: report.reporterNickname,
            submissionTime: dateFormat(report.createdAt),

            // 추가 필드
            reportStatus: config.statusLabel,
            reportResult: config.resultLabel,
        };
    });
}
