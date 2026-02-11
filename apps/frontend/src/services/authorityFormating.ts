export const authorityFormat = (authority: string) => {
    if (authority === "ROLE_ADMIN") {
        return "관리자";
    } else if (authority === "ROLE_USER") {
        return "일반 사용자";
    }
};
