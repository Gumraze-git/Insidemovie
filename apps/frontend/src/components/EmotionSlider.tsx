import * as React from "react";
import { useEffect, useRef, useState } from "react";

interface EmotionSliderProps {
    name: string; // 감정 이름 (예: "기쁨")
    color: string; // 감정 색상 (예: "#FFD93D")
    image: string; // 캐릭터 이미지 경로
    value: number; // 현재 슬라이더 값
    onChange: (value: number) => void; // 값 변경 핸들러
    onChangeEnd?: (value: number) => void; // called once when drag ends
}

const EmotionSlider: React.FC<EmotionSliderProps> = ({
    name,
    color,
    image,
    value,
    onChange,
    onChangeEnd,
}) => {
    const boxRef = useRef<HTMLDivElement>(null);
    const [isDragging, setIsDragging] = useState(false);
    const currentValueRef = useRef<number>(value);

    const handleMouseMove = (e: MouseEvent) => {
        if (!boxRef.current) return;

        const rect = boxRef.current.getBoundingClientRect();
        const offsetY = e.clientY - rect.top;
        const newValue = Math.max(
            0,
            Math.min(100, ((rect.height - offsetY) / rect.height) * 100),
        );
        // Round to nearest multiple of 5
        const stepValue = newValue;
        // Only emit when changed
        if (stepValue !== value) {
            onChange(stepValue);
        }
        currentValueRef.current = stepValue;
    };

    useEffect(() => {
        const handleMouseUp = () => {
            setIsDragging(false);
            if (onChangeEnd) onChangeEnd(currentValueRef.current);
        };
        const handleMouseMoveGlobal = (e: MouseEvent) => {
            if (isDragging) handleMouseMove(e);
        };

        window.addEventListener("mouseup", handleMouseUp);
        window.addEventListener("mousemove", handleMouseMoveGlobal);
        return () => {
            window.removeEventListener("mouseup", handleMouseUp);
            window.removeEventListener("mousemove", handleMouseMoveGlobal);
        };
    }, [isDragging, onChangeEnd]);

    const handleMouseDown = (e: React.MouseEvent) => {
        setIsDragging(true);
        handleMouseMove(e.nativeEvent);
    };

    return (
        <div className="flex flex-col items-center space-y-2 px-2 py-3 select-none transform transition-transform duration-200 hover:scale-105">
            <div
                ref={boxRef}
                onMouseDown={handleMouseDown}
                className="w-20 md:w-28 h-36 md:h-44 rounded-3xl overflow-hidden relative cursor-pointer transform transition-transform duration-200 border-1"
                style={{
                    background: `linear-gradient(to top, ${color} ${value}%, rgba(255,255,255,0.05) ${value}%)`,
                    transform: isDragging ? "scale(1.05)" : "scale(1)",
                    borderColor: color,
                    boxShadow: `0 0 12px ${color}`,
                }}
            >
                <div className="absolute inset-0">
                    <img
                        src={image}
                        alt={name}
                        className="w-full h-full object-contain pointer-events-none"
                        style={{
                            maskImage: `linear-gradient(to top, ${color} ${value}%, rgba(255,255,255,0.3) ${value}%)`,
                            WebkitMaskImage: `linear-gradient(to top, ${color} ${value}%, rgba(255,255,255,0.3) ${value}%)`,
                        }}
                    />
                </div>
            </div>
        </div>
    );
};

export default EmotionSlider;
