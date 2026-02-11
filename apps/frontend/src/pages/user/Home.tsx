import * as React from "react";
import SearchSection from "../../components/home/SearchSection";
import CustomMovieSection from "../../components/home/CustomMovieSection";
import BoxOfficeSection from "../../components/home/BoxOfficeSection";
import MatchSection from "../../components/home/MatchSection";
import RecommendMovieSection from "../../components/home/RecommendMovieSection";

const Home: React.FC = () => {
    window.dispatchEvent(new Event("profileUpdated"));
    return (
        <div className="min-h-screen flex justify-center flex-col items-center px-4">
            <div className="md:max-w-screen-lg w-full flex flex-col items-center mb-52">
                <SearchSection className="mt-32" />
                {/*<EmotionSection className="mt-32" />*/}
                <CustomMovieSection className="mt-32" />
                <RecommendMovieSection className="mt-32" />
                <BoxOfficeSection className="mt-32" />
                <MatchSection className="mt-32" />
                {/*<section className="snap-start h-screen"></section>*/}
            </div>
        </div>
    );
};

export default Home;
