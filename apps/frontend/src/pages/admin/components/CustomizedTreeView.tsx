import * as React from "react";
import clsx from "clsx";
import { animated, useSpring } from "@react-spring/web";
import type { TransitionProps } from "@mui/material/transitions";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Collapse from "@mui/material/Collapse";
import Typography from "@mui/material/Typography";
import { RichTreeView } from "@mui/x-tree-view/RichTreeView";
import {
    useTreeItem,
    type UseTreeItemParameters,
} from "@mui/x-tree-view/useTreeItem";
import {
    TreeItemContent,
    TreeItemIconContainer,
    TreeItemLabel,
    TreeItemRoot,
} from "@mui/x-tree-view/TreeItem";
import { TreeItemIcon } from "@mui/x-tree-view/TreeItemIcon";
import { TreeItemProvider } from "@mui/x-tree-view/TreeItemProvider";
import type { TreeViewBaseItem } from "@mui/x-tree-view/models";
import { useTheme } from "@mui/material/styles";
import { useNavigate } from "react-router-dom";

type Color = "blue" | "green";

type ExtendedTreeItemProps = {
    color?: Color;
    id: string;
    label: string;
};

const ITEMS: TreeViewBaseItem<ExtendedTreeItemProps>[] = [
    {
        id: "1",
        label: "InsideMovie",
        children: [
            { id: "1.1", label: "홈", color: "green" },
            { id: "1.2", label: "추천 영화", color: "green" },
            { id: "1.3", label: "박스오피스", color: "green" },
            { id: "1.4", label: "주간 대결 영화", color: "green" },
        ],
    },
    {
        id: "2",
        label: "외부 사이트",
        children: [
            {
                id: "2.1",
                label: "API",
                children: [
                    { id: "2.1.1", label: "TMDb", color: "blue" },
                    { id: "2.1.2", label: "KOFIC", color: "blue" },
                ],
            },
        ],
    },
];

function DotIcon({ color }: { color: string }) {
    return (
        <Box sx={{ marginRight: 1, display: "flex", alignItems: "center" }}>
            <svg width={6} height={6}>
                <circle cx={3} cy={3} r={3} fill={color} />
            </svg>
        </Box>
    );
}

const AnimatedCollapse = animated(Collapse);

function TransitionComponent(props: TransitionProps) {
    const style = useSpring({
        to: {
            opacity: props.in ? 1 : 0,
            transform: `translate3d(0,${props.in ? 0 : 20}px,0)`,
        },
    });

    return <AnimatedCollapse style={style} {...props} />;
}

interface CustomLabelProps {
    children: React.ReactNode;
    color?: Color;
    expandable?: boolean;
}

function CustomLabel({
    color,
    // expandable,
    children,
    ...other
}: CustomLabelProps) {
    const theme = useTheme();
    const colors = {
        blue: (theme.vars || theme).palette.primary.main,
        green: (theme.vars || theme).palette.success.main,
    };

    const iconColor = color ? colors[color] : null;
    return (
        <TreeItemLabel
            {...other}
            sx={{ display: "flex", alignItems: "center" }}
        >
            {iconColor && <DotIcon color={iconColor} />}
            <Typography
                className="labelText"
                variant="body2"
                sx={{ color: "text.primary" }}
            >
                {children}
            </Typography>
        </TreeItemLabel>
    );
}

interface CustomTreeItemProps
    extends Omit<UseTreeItemParameters, "rootRef">,
        Omit<React.HTMLAttributes<HTMLLIElement>, "onFocus"> {}

const CustomTreeItem = React.forwardRef(function CustomTreeItem(
    props: CustomTreeItemProps,
    ref: React.Ref<HTMLLIElement>,
) {
    const { id, itemId, label, disabled, children, ...other } = props;

    const {
        getRootProps,
        getContentProps,
        getIconContainerProps,
        getLabelProps,
        getGroupTransitionProps,
        status,
        publicAPI,
    } = useTreeItem({
        id,
        itemId,
        children,
        label,
        disabled,
        rootRef: ref,
    });

    const item = publicAPI.getItem(itemId);
    const color = item?.color;
    return (
        <TreeItemProvider id={id} itemId={itemId}>
            <TreeItemRoot {...getRootProps(other)}>
                <TreeItemContent
                    {...getContentProps({
                        className: clsx("content", {
                            expanded: status.expanded,
                            selected: status.selected,
                            focused: status.focused,
                            disabled: status.disabled,
                        }),
                    })}
                >
                    {status.expandable && (
                        <TreeItemIconContainer {...getIconContainerProps()}>
                            <TreeItemIcon status={status} />
                        </TreeItemIconContainer>
                    )}

                    <CustomLabel {...getLabelProps({ color })} />
                </TreeItemContent>
                {children && (
                    <TransitionComponent
                        {...getGroupTransitionProps({
                            className: "groupTransition",
                        })}
                    />
                )}
            </TreeItemRoot>
        </TreeItemProvider>
    );
});

export default function CustomizedTreeView() {
    const navigate = useNavigate();
    const handleItemClick = (itemId: string) => {
        switch (itemId) {
            case "1.1":
                navigate("/");
                break;
            case "1.2":
                navigate("/recommend");
                break;
            case "1.3":
                navigate("/boxoffice");
                break;
            case "1.4":
                navigate("/weekmatch");
                break;
            case "2.1.1":
                window.open(
                    "https://developer.themoviedb.org/reference/intro/getting-started",
                    "_blank",
                );
                break;
            case "2.1.2":
                window.open(
                    "https://www.kobis.or.kr/kobisopenapi/homepg/main/main.do",
                    "_blank",
                );
                break;
            default:
                break;
        }
    };
    return (
        <Card
            variant="outlined"
            sx={{
                display: "flex",
                flexDirection: "column",
                gap: "8px",
                flexGrow: 1,
            }}
        >
            <CardContent>
                <Typography component="h2" variant="subtitle2">
                    바로가기
                </Typography>
                <RichTreeView
                    items={ITEMS}
                    aria-label="pages"
                    multiSelect
                    defaultExpandedItems={["1", "2", "2.1"]}
                    sx={{
                        m: "0 -8px",
                        pb: "8px",
                        height: "fit-content",
                        flexGrow: 1,
                        overflowY: "auto",
                    }}
                    slots={{
                        item: (props) => (
                            <CustomTreeItem
                                {...props}
                                onClick={() => handleItemClick(props.itemId)}
                            />
                        ),
                    }}
                />
            </CardContent>
        </Card>
    );
}
