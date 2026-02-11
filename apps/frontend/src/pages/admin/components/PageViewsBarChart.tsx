import * as React from "react";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Typography from "@mui/material/Typography";
import Stack from "@mui/material/Stack";
import { BarChart } from "@mui/x-charts/BarChart";
import { alpha, useTheme } from "@mui/material/styles";

interface PageViewBarChartProps {
    MonthlytotalMembers: number[];
    MonthlytotalReviews: number[];
    MonthlyconcealedReviews: number[];
}

function getLastWeekFromYesterday() {
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1); // 어제 날짜

    const days = [];
    for (let i = 6; i >= 0; i--) {
        const d = new Date(yesterday);
        d.setDate(yesterday.getDate() - i);
        const label = d.toLocaleDateString("ko-KR", {
            month: "long",
            day: "numeric",
        });
        days.push(label);
    }

    return days;
}

export default function PageViewsBarChart({
    MonthlytotalMembers,
    MonthlytotalReviews,
    MonthlyconcealedReviews,
}: PageViewBarChartProps) {
    const theme = useTheme();
    const latestOneWeek = getLastWeekFromYesterday();
    const colorPalette = [
        (theme.vars || theme).palette.primary.dark,
        (theme.vars || theme).palette.primary.main,
        (theme.vars || theme).palette.primary.light,
    ];
    const safeSlice = (arr: number[]) => {
        if (arr.length < 29) {
            return arr.slice(-7);
        }
        return arr.slice(22, 29);
    };
    return (
        <Card variant="outlined" sx={{ width: "100%" }}>
            <CardContent>
                <Typography component="h2" variant="subtitle2" gutterBottom>
                    일간 추이
                </Typography>
                <Stack sx={{ justifyContent: "space-between" }}>
                    <Stack
                        direction="row"
                        sx={{
                            alignContent: { xs: "center", sm: "flex-start" },
                            alignItems: "center",
                            gap: 1,
                        }}
                    ></Stack>
                    <Typography
                        variant="caption"
                        sx={{ color: "text.secondary" }}
                    >
                        전체 통계 (지난 7일 - 어제)
                    </Typography>
                </Stack>
                <BarChart
                    borderRadius={8}
                    colors={colorPalette}
                    xAxis={[
                        {
                            scaleType: "band",
                            categoryGapRatio: 0.5,
                            data: latestOneWeek,
                            height: 24,
                        },
                    ]}
                    yAxis={[{ width: 50 }]}
                    series={[
                        {
                            id: "users",
                            label: "유저 수",
                            data: safeSlice(MonthlytotalMembers),
                            stack: "A",
                            color: alpha(theme.palette.success.main, 0.8),
                        },
                        {
                            id: "reviews",
                            label: "리뷰 수",
                            data: safeSlice(MonthlytotalReviews),
                            stack: "B",
                            color: alpha(theme.palette.grey[400], 0.8),
                        },
                        {
                            id: "reports",
                            label: "신고 수",
                            data: safeSlice(MonthlyconcealedReviews),
                            stack: "C",
                            color: alpha(theme.palette.error.main, 0.8),
                        },
                    ]}
                    height={250}
                    margin={{ left: 0, right: 0, top: 20, bottom: 0 }}
                    grid={{ horizontal: true }}
                    hideLegend
                />
            </CardContent>
        </Card>
    );
}
