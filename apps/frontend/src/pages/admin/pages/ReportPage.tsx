import * as React from "react";
import type {} from "@mui/x-date-pickers/themeAugmentation";
import type {} from "@mui/x-charts/themeAugmentation";
import type {} from "@mui/x-data-grid/themeAugmentation";
import type {} from "@mui/x-tree-view/themeAugmentation";
import { alpha } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";
import Box from "@mui/material/Box";
import Stack from "@mui/material/Stack";
import AppNavbar from "../components/AppNavbar";
import Header from "../components/Header";
import SideMenu from "../components/SideMenu";
import AppTheme from "../shared-theme/AppTheme";
import {
    chartsCustomizations,
    dataGridCustomizations,
    datePickersCustomizations,
    treeViewCustomizations,
} from "../theme/customizations";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import Copyright from "../internals/components/Copyright";
import ReportBoardLarge from "../components/ReportBoardLarge";

const xThemeComponents = {
    ...chartsCustomizations,
    ...dataGridCustomizations,
    ...datePickersCustomizations,
    ...treeViewCustomizations,
};

export default function ReportPage(props: { disableCustomTheme?: boolean }) {
    return (
        <AppTheme {...props} themeComponents={xThemeComponents}>
            <CssBaseline enableColorScheme />
            <Box sx={{ display: "flex" }}>
                <SideMenu />
                <AppNavbar />
                <Box
                    component="main"
                    sx={(theme) => ({
                        flexGrow: 1,
                        backgroundColor: theme.vars
                            ? `rgba(${theme.vars.palette.background.defaultChannel} / 1)`
                            : alpha(theme.palette.background.default, 1),
                        overflow: "auto",
                    })}
                >
                    <Stack
                        spacing={2}
                        sx={{
                            alignItems: "center",
                            mx: 3,
                            pb: 5,
                            mt: { xs: 12, md: 0 },
                            minHeight: "100vh",
                        }}
                    >
                        <Header />
                        <Box
                            sx={{
                                width: "100%",
                                minHeight: "80vh",
                                maxWidth: { sm: "100%", md: "1700px" },
                            }}
                        >
                            <Typography
                                component="h2"
                                variant="h6"
                                sx={{ mb: 2, mt: 4 }}
                            >
                                신고 관리
                            </Typography>

                            <Grid container spacing={2} columns={12}>
                                <Grid size={{ xs: 12, lg: 12 }}>
                                    <ReportBoardLarge />
                                </Grid>
                            </Grid>

                            <Copyright sx={{ my: 4 }} />
                        </Box>
                    </Stack>
                </Box>
            </Box>
        </AppTheme>
    );
}
