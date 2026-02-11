import * as React from "react";
import {
    BrowserRouter,
    Routes,
    Route,
    useLocation,
    Navigate,
} from "react-router-dom";
import Login from "./pages/user/social/Login";
import Home from "./pages/user/Home";
import Signup from "./pages/user/social/Signup";
import BoxOfficeMovie from "./pages/user/BoxOfficeMovie";
import Header from "./components/Header";
import RecommendMovie from "./pages/user/RecommendMovie";
import WeekMatch from "./pages/user/WeekMatch";
import MovieDetail from "./pages/user/movie/MovieDetail";
import Dashboard from "./pages/admin/pages/Dashboard";
import ReportPage from "./pages/admin/pages/ReportPage";
import MemberPage from "./pages/admin/pages/MemberPage";
import ReviewWrite from "./pages/user/ReviewWrite";
import KakaoRedirect from "./pages/user/social/KakaoRedirect";
import SignupKakao from "./pages/user/social/SignupKakao";
import MyPage from "./pages/user/mypage/MyPage";
import LikedMovie from "./pages/user/mypage/LikedMovie";
import WatchedMovie from "./pages/user/mypage/WatchedMovie";
import MyReview from "./pages/user/mypage/MyReview";
import Search from "./pages/user/Search";

const AppContent: React.FC = () => {
    const location = useLocation();
    const cleanedPath = location.pathname.replace(/\/$/, "");

    const hideHeader = [
        "/login",
        "/signup",
        "/signup-kakao",
        "/admin",
        "/admin/report",
        "/admin/member",
    ].includes(cleanedPath);    // URL 끝에 "/" 붙어도 적용되게 변경

    // authority 값 가져오기
    const authority = localStorage.getItem("authority");

    return (
        <>
            {!hideHeader && <Header />}
            <Routes>
                {/* Redirect Logic */}
                {authority === "ROLE_ADMIN" ? (
                    <>
                        <Route path="/admin" element={<Dashboard />} />
                        <Route path="/admin/report" element={<ReportPage />} />
                        <Route path="/admin/member" element={<MemberPage />} />
                        {/* Admin이 유저 페이지 접근 시 리디렉션 */}
                        <Route
                            path="/*"
                            element={<Navigate to="/admin" replace />}
                        />
                    </>
                ) : (
                    <>
                        {/*User or default fallback*/}
                        <Route path="/" element={<Home />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/signup" element={<Signup />} />
                        <Route path="/signup-kakao" element={<SignupKakao />} />
                        <Route path="/mypage" element={<MyPage />} />
                        <Route
                            path="/mypage/liked-movie"
                            element={<LikedMovie />}
                        />
                        <Route
                            path="/mypage/watched-movie"
                            element={<WatchedMovie />}
                        />
                        <Route
                            path="/mypage/my-review"
                            element={<MyReview />}
                        />
                        <Route path="/search" element={<Search />} />
                        <Route path="/recommend" element={<RecommendMovie />} />
                        <Route path="/boxoffice" element={<BoxOfficeMovie />} />
                        <Route path="/weekmatch" element={<WeekMatch />} />
                        <Route
                            path="/movies/detail/:movieId"
                            element={<MovieDetail />}
                        />
                        <Route
                            path="/review-write/:movieId"
                            element={<ReviewWrite />}
                        />
                        <Route
                            path="/login/oauth2/code/kakao"
                            element={<KakaoRedirect />}
                        />
                        {/* User 또는 권한 없는 사용자가 어드민 페이지 접근 시 리디렉션 */}
                        <Route
                            path="/admin/*"
                            element={<Navigate to="/" replace />}
                        />
                    </>
                )}
            </Routes>
        </>
    );
};

const App: React.FC = () => {
    return (
        <BrowserRouter>
            <AppContent />
        </BrowserRouter>
    );
};

export default App;
