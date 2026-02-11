import dateFormat from "./dateFormating";
import { authorityFormat } from "./authorityFormating";

import type { GridRowsProp } from "@mui/x-data-grid";
import type { Member } from "../types/member";

export function mapMembersToRows(members: Member[]): GridRowsProp {
    if (!members) {
        return []; // null이나 undefined일 땐 빈 배열 반환
    }
    return members.map((member) => {
        console.log(authorityFormat(member.authority));
        return {
            id: member.id, // DataGrid 고유키 필수
            email: member.email,
            nickname: member.nickname,
            reportCount: member.reportCount,
            authority: authorityFormat(member.authority),
            reviewCount: member.reviewCount,
            banned: member.banned,
            createdAt: dateFormat(member.createdAt),
        };
    });
}
