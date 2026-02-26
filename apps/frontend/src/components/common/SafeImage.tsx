import React, { useEffect, useMemo, useState } from "react";
import DefaultPoster from "@assets/sample_poster.png";

type SafeImageProps = Omit<React.ImgHTMLAttributes<HTMLImageElement>, "src"> & {
    src?: string | null;
    fallbackSrc?: string;
    fallbackKey?: string | number;
    fallbackLabel?: string;
};

type FallbackTheme = {
    start: string;
    end: string;
    accent: string;
};

const FALLBACK_THEMES: FallbackTheme[] = [
    { start: "#0f172a", end: "#1d4ed8", accent: "#93c5fd" },
    { start: "#111827", end: "#7c3aed", accent: "#c4b5fd" },
    { start: "#1f2937", end: "#0ea5e9", accent: "#67e8f9" },
    { start: "#3f1d2e", end: "#be185d", accent: "#f9a8d4" },
    { start: "#052e16", end: "#15803d", accent: "#86efac" },
    { start: "#422006", end: "#d97706", accent: "#fde68a" },
    { start: "#172554", end: "#2563eb", accent: "#bfdbfe" },
    { start: "#083344", end: "#0891b2", accent: "#a5f3fc" },
];

const hashValue = (value: string): number => {
    let hash = 0;
    for (let i = 0; i < value.length; i += 1) {
        hash = (hash * 31 + value.charCodeAt(i)) >>> 0;
    }
    return hash;
};

const toFallbackDataUri = (theme: FallbackTheme, label: string): string => {
    const svg = `
<svg xmlns="http://www.w3.org/2000/svg" width="600" height="900" viewBox="0 0 600 900">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="${theme.start}" />
      <stop offset="100%" stop-color="${theme.end}" />
    </linearGradient>
  </defs>
  <rect width="600" height="900" fill="url(#bg)" rx="26" />
  <rect x="165" y="250" width="270" height="340" rx="20" fill="rgba(255,255,255,0.12)" />
  <rect x="210" y="305" width="180" height="200" rx="14" fill="rgba(255,255,255,0.24)" />
  <circle cx="220" cy="330" r="14" fill="${theme.accent}" />
  <rect x="200" y="655" width="200" height="54" rx="27" fill="rgba(0,0,0,0.26)" />
  <text x="300" y="690" text-anchor="middle" font-size="26" fill="#ffffff" font-family="Apple SD Gothic Neo, Noto Sans KR, sans-serif">${label}</text>
</svg>`;
    return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
};

const SafeImage: React.FC<SafeImageProps> = ({
    src,
    fallbackSrc,
    fallbackKey,
    fallbackLabel = "포스터 준비중",
    loading = "lazy",
    onError,
    ...props
}) => {
    const deterministicFallbackSrc = useMemo(() => {
        if (fallbackSrc) return fallbackSrc;

        const key = String(fallbackKey ?? props.alt ?? src ?? "");
        if (key.trim().length === 0) return DefaultPoster;

        const theme = FALLBACK_THEMES[hashValue(key) % FALLBACK_THEMES.length];
        return toFallbackDataUri(theme, fallbackLabel);
    }, [fallbackKey, fallbackLabel, fallbackSrc, props.alt, src]);

    const normalizedSrc = useMemo(() => {
        if (!src) return deterministicFallbackSrc;
        const trimmed = src.trim();
        return trimmed.length > 0 ? trimmed : deterministicFallbackSrc;
    }, [deterministicFallbackSrc, src]);

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
                if (currentSrc !== deterministicFallbackSrc) {
                    setCurrentSrc(deterministicFallbackSrc);
                }
                onError?.(event);
            }}
        />
    );
};

export default SafeImage;
