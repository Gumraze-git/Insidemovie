/** @type {import('tailwindcss').Config} */
export default {
    content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
    theme: {
        extend: {
            backgroundImage: {
                "navy-radial":
                    "radial-gradient(circle at top center, #0b0f3a, #050a1a)",
            },
            fontFamily: {
                paper: ["Paperlogy", "sans-serif"], // 유틸리티 이름: font-paper
            },
            colors: {
                movie_main: "#081232",
                movie_sub: "#8572FF",
                movie_bright: "#CBC3FF",
                movie_highlight: "#CBC3FF",
                kakao_yellow: "#FEE502",
                kakao_yellow_bright: "#FBF24A",
                like_red: "#EA3323",
                error_red: "#FF6C6C",
                grey_400: "#444444",
                grey_300: "#7C7C7C",
                grey_200: "#CCCCCC",
                grey_100: "#E9EAEC",
                white: "#FFFFFF",
                box_bg_white: "#FFFFFF40",
                joy_yellow: "#FFD602",
                sad_blue: "#1169F0",
                angry_red: "#DD2424",
                fear_purple: "#9360BD",
                disgust_green: "#A2C95A",
                anxiety_orange: "#F57F2B",
                ennui_purple: "#4637A9",
            },
        },
    },
    plugins: [require("@tailwindcss/line-clamp")],
};
