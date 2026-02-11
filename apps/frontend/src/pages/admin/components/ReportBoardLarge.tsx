import * as React from "react";
import { DataGrid, type GridRowsProp } from "@mui/x-data-grid";
import { getColumns } from "../internals/data/gridData";
import Box from "@mui/material/Box";
import { useEffect, useState } from "react";
import { mapReportsToRows } from "../../../services/mapReportsToRows";
import axios from "axios";
import type { Report } from "../../../types/report";
import { useNavigate } from "react-router-dom";
import type { ReportStatus } from "../../../types/reportStatus";
import { useColorScheme, useTheme } from "@mui/material/styles";
import { darken } from "@mui/material/styles";

export default function ReportBoard() {
    const [reportList, setReportList] = useState<Report[] | null>(null);
    const navigate = useNavigate();

    const theme = useTheme();
    const { mode, systemMode } = useColorScheme();
    const appliedMode = mode === "system" ? systemMode : mode;

    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        // console.log("토큰 : ", token);
        const fetchData = async () => {
            try {
                const res = await axios.get(
                    "http://52.79.175.149:8080/api/v1/admin/reports?page=0&size=20",
                    {
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    },
                );
                const allData = res.data.data.content;
                if (!allData) {
                    console.error("allData 없음");
                    return;
                }
                setReportList(allData);
                console.log("필터링 : ", allData);
            } catch (err) {
                console.error("신고 페이지 데이터 불러오기 실패:", err);
                navigate("/login");
            }
        };
        fetchData();
    }, []);
    const rows: GridRowsProp = mapReportsToRows(reportList);

    const handleStatusChange = (reportId: number, newStatus: ReportStatus) => {
        const updated = reportList.map((r) =>
            r.reportId === reportId ? { ...r, status: newStatus } : r,
        );
        setReportList(updated ?? []);
    };
    const columns = getColumns(handleStatusChange);

    if (!reportList) {
        return <div className="text-white text-center">Loading...</div>;
    }
    return (
        <Box
            sx={{
                width: "100%",
                // 인덱스 0부터, odd -> 짝수 칸, even -> 홀수 칸
                "& .MuiDataGrid-row.odd": {
                    backgroundColor:
                        appliedMode === "light"
                            ? darken(theme.palette.background.paper, 0.04)
                            : darken(theme.palette.background.paper, 0.9),
                    "&:hover": {
                        backgroundColor:
                            appliedMode === "light"
                                ? darken(theme.palette.background.paper, 0)
                                : darken(theme.palette.background.paper, 0.8),
                    },
                },
                "& .MuiDataGrid-row.even": {
                    backgroundColor:
                        appliedMode === "light"
                            ? darken(theme.palette.background.paper, 0.02)
                            : darken(theme.palette.background.paper, 0.98),
                    "&:hover": {
                        backgroundColor:
                            appliedMode === "light"
                                ? darken(theme.palette.background.paper, 0)
                                : darken(theme.palette.background.paper, 0.8),
                    },
                },
            }}
        >
            <DataGrid
                rows={rows}
                columns={columns}
                getRowClassName={(params) =>
                    params.indexRelativeToCurrentPage % 2 === 0 ? "even" : "odd"
                }
                initialState={{
                    columns: {
                        columnVisibilityModel: {
                            status: false,
                        },
                    },
                    sorting: {
                        sortModel: [
                            {
                                field: "submissionTime",
                                sort: "desc",
                            },
                        ],
                    },
                    pagination: { paginationModel: { pageSize: 20 } },
                }}
                pageSizeOptions={[10, 20, 50]}
                disableColumnResize
                density="compact"
                slotProps={{
                    filterPanel: {
                        filterFormProps: {
                            logicOperatorInputProps: {
                                variant: "outlined",
                                size: "small",
                            },
                            columnInputProps: {
                                variant: "outlined",
                                size: "small",
                                sx: { mt: "auto" },
                            },
                            operatorInputProps: {
                                variant: "outlined",
                                size: "small",
                                sx: { mt: "auto" },
                            },
                            valueInputProps: {
                                InputComponentProps: {
                                    variant: "outlined",
                                    size: "small",
                                },
                            },
                        },
                    },
                }}
            ></DataGrid>
        </Box>
    );
}
