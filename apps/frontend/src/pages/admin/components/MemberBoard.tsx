import * as React from "react";
import { DataGrid, type GridRowsProp } from "@mui/x-data-grid";
import { getMemberColumns } from "../internals/data/gridData";
import Box from "@mui/material/Box";
import { useEffect, useState } from "react";
import axios from "axios";
import type { Member } from "../../../types/member";
import { useNavigate } from "react-router-dom";
import { mapMembersToRows } from "../../../services/mapMembersToRows";
import { useColorScheme, useTheme } from "@mui/material/styles";
import { darken } from "@mui/material/styles";

export default function MemberBoard() {
    const [memberList, setMemberList] = useState<Member[] | null>(null);
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
                    "http://52.79.175.149:8080/api/v1/admin/members?page=0&size=20",
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
                setMemberList(allData);
                console.log("필터링 : ", allData);
            } catch (err) {
                console.error("멤버 페이지 데이터 불러오기 실패:", err);
                navigate("/login");
            }
        };
        fetchData();
    }, []);
    const rows: GridRowsProp = mapMembersToRows(memberList);
    if (!memberList) {
        return [];
    }

    const handleBannedChange = (memberId: number, newStatus: boolean) => {
        const updated = memberList.map((r) =>
            r.id === memberId ? { ...r, banned: newStatus } : r,
        );
        setMemberList(updated ?? []);
    };
    const columns = getMemberColumns(handleBannedChange);

    if (!memberList) {
        return <div className="text-white text-center">Loading...</div>;
    }

    return (
        <Box
            sx={{
                width: "100%",
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
