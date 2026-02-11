import * as React from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import SearchIcon from "@assets/search.svg?react";
import CloseIcon from "@assets/close.svg?react";

interface SearchSectionProps {
    className?: string;
}

const SearchSection: React.FC<SearchSectionProps> = ({ className = "" }) => {
    const [searchTerm, setSearchTerm] = useState("");
    const navigate = useNavigate();

    const handleSearch = () => {
        const keyword = searchTerm.trim();
        if (keyword) {
            navigate(`/search?title=${encodeURIComponent(keyword)}`);
        }
    };

    return (
        <div className={`md:max-w-screen-md w-full ${className}`}>
            <div className="relative">
                <input
                    type="text"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") handleSearch();
                    }}
                    placeholder="영화를 검색하세요."
                    className="w-full py-4 px-5 pr-12 rounded-full bg-box_bg_white text-white text-sm font-light placeholder-white placeholder-opacity-60 focus:outline-none"
                />
                {searchTerm && (
                    <div
                        className="absolute top-1/2 right-12 transform -translate-y-1/2 text-white cursor-pointer"
                        onClick={() => {
                            setSearchTerm("");
                        }}
                    >
                        <CloseIcon className="w-5 h-5 opacity-60 hover:opacity-100 transition-opacity" />
                    </div>
                )}
                <div
                    className="absolute top-1/2 right-4 transform -translate-y-1/2 text-white"
                    onClick={handleSearch}
                    style={{ cursor: "pointer" }}
                >
                    <SearchIcon className="w-7 h-7 opacity-40" />
                </div>
            </div>
        </div>
    );
};

export default SearchSection;
