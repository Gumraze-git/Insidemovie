import * as React from "react";
import Avatar from "@mui/material/Avatar";
import Button from "@mui/material/Button";
import Divider from "@mui/material/Divider";
import Drawer, { drawerClasses } from "@mui/material/Drawer";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import LogoutRoundedIcon from "@mui/icons-material/LogoutRounded";
import iconBingBong from "../../../assets/character/bingbong_icon.png";

import MenuContent from "./MenuContent";
import { memberApi } from "../../../api/memberApi";

interface SideMenuMobileProps {
    open: boolean | undefined;
    toggleDrawer: (newOpen: boolean) => () => void;
}

export default function SideMenuMobile({
    open,
    toggleDrawer,
}: SideMenuMobileProps) {
    const handleLogout = async () => {
        try {
            await memberApi().logout();
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            localStorage.removeItem("authority");

            window.location.replace("/");
        } catch (err) {
            console.error("Logout failed", err);
        }
    };
    return (
        <Drawer
            anchor="right"
            open={open}
            onClose={toggleDrawer(false)}
            sx={{
                zIndex: (theme) => theme.zIndex.drawer + 1,
                [`& .${drawerClasses.paper}`]: {
                    backgroundImage: "none",
                    backgroundColor: "background.paper",
                },
            }}
        >
            <Stack
                sx={{
                    maxWidth: "70dvw",
                    height: "100%",
                }}
            >
                <Stack direction="row" sx={{ p: 2, pb: 0, gap: 1 }}>
                    <Stack
                        direction="column"
                        sx={{ gap: 1, alignItems: "center", flexGrow: 1, p: 1 }}
                    >
                        <Avatar
                            sizes="small"
                            alt="name"
                            src={iconBingBong}
                            sx={{ width: 24, height: 24 }}
                        />
                        <Typography component="p" variant="h6">
                            관리자
                        </Typography>
                        <Typography
                            variant="caption"
                            sx={{ color: "text.secondary" }}
                        >
                            admin@test.com
                        </Typography>
                    </Stack>
                </Stack>
                <Divider />
                <Stack sx={{ flexGrow: 1 }}>
                    <MenuContent />
                    <Divider />
                </Stack>
                {/* <CardAlert /> */}
                <Stack sx={{ p: 2 }}>
                    <Button
                        variant="outlined"
                        fullWidth
                        startIcon={<LogoutRoundedIcon />}
                        onClick={handleLogout}
                    >
                        Logout
                    </Button>
                </Stack>
            </Stack>
        </Drawer>
    );
}
