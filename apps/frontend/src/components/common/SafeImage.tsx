import React, { useEffect, useMemo, useState } from "react";
import DefaultPoster from "@assets/sample_poster.png";

type SafeImageProps = Omit<React.ImgHTMLAttributes<HTMLImageElement>, "src"> & {
    src?: string | null;
    fallbackSrc?: string;
};

const SafeImage: React.FC<SafeImageProps> = ({
    src,
    fallbackSrc = DefaultPoster,
    loading = "lazy",
    onError,
    ...props
}) => {
    const normalizedSrc = useMemo(() => {
        if (!src) return fallbackSrc;
        const trimmed = src.trim();
        return trimmed.length > 0 ? trimmed : fallbackSrc;
    }, [src, fallbackSrc]);

    const [currentSrc, setCurrentSrc] = useState<string>(normalizedSrc);

    useEffect(() => {
        setCurrentSrc(normalizedSrc);
    }, [normalizedSrc]);

    return (
        <img
            {...props}
            src={currentSrc}
            loading={loading}
            onError={(event) => {
                if (currentSrc !== fallbackSrc) {
                    setCurrentSrc(fallbackSrc);
                }
                onError?.(event);
            }}
        />
    );
};

export default SafeImage;
