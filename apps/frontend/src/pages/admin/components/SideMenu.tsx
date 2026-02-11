import { styled } from "@mui/material/styles";
import Avatar from "@mui/material/Avatar";
import MuiDrawer, { drawerClasses } from "@mui/material/Drawer";
import Box from "@mui/material/Box";
import Divider from "@mui/material/Divider";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import MenuContent from "./MenuContent";
import OptionsMenu from "./OptionsMenu";
import logoLight from "../../../assets/insidemovie_white_long.svg";
import logoDark from "../../../assets/insidemovie_dark_long.svg";
import iconBingBong from "../../../assets/character/bingbong_icon.png";
import { useColorScheme } from "@mui/material/styles";

const drawerWidth = 240;

const Drawer = styled(MuiDrawer)({
    width: drawerWidth,
    flexShrink: 0,
    boxSizing: "border-box",
    mt: 10,
    [`& .${drawerClasses.paper}`]: {
        width: drawerWidth,
        boxSizing: "border-box",
    },
});

export default function SideMenu() {
    const { mode, systemMode } = useColorScheme();
    const appliedMode = mode === "system" ? systemMode : mode;
    const logo = appliedMode === "light" ? logoDark : logoLight;
    console.log(logo);
    return (
        <Drawer
            variant="permanent"
            sx={{
                display: { xs: "none", md: "block" },
                [`& .${drawerClasses.paper}`]: {
                    backgroundColor: "background.paper",
                },
            }}
        >
            <Box
                sx={{
                    display: "flex",
                    mt: "calc(var(--template-frame-height, 0px) + 4px)",
                    p: 1.5,
                    justifyContent: "center",
                }}
            >
                <img
                    src={logo}
                    alt="로고"
                    style={{ height: "40px", objectFit: "contain" }}
                />
            </Box>
            <Divider />
            <Box
                sx={{
                    overflow: "auto",
                    height: "100%",
                    display: "flex",
                    flexDirection: "column",
                }}
            >
                <MenuContent />
            </Box>
            <Stack
                direction="row"
                sx={{
                    p: 2,
                    gap: 1,
                    alignItems: "center",
                    borderTop: "1px solid",
                    borderColor: "divider",
                }}
            >
                <Avatar
                    sizes="small"
                    alt="hks"
                    src={iconBingBong}
                    sx={{ width: 36, height: 36 }}
                />
                <Box sx={{ mr: "auto" }}>
                    <Typography
                        variant="body2"
                        sx={{ fontWeight: 500, lineHeight: "16px" }}
                    >
                        관리자
                    </Typography>
                    <Typography
                        variant="caption"
                        sx={{ color: "text.secondary" }}
                    >
                        admin@test.com
                    </Typography>
                </Box>
                <OptionsMenu />
            </Stack>
        </Drawer>
    );
}
