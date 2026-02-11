import * as React from "react";
import EmotionSlider from "../EmotionSlider";
import { useEffect, useState } from "react";
import joyImg from "@assets/character/joy.png";
import sadImg from "@assets/character/sad.png";
import angryImg from "@assets/character/angry.png";
import fearImg from "@assets/character/fear.png";
import disgustImg from "@assets/character/disgust.png";
import bingdbongImg from "@assets/character/bingbong.png";
import Button from "../Button";
import { memberApi } from "../../api/memberApi";

interface CharacterCarouselSectionProps {
    className?: string;
    onEmotionsChange?: (
        joy: number,
        sad: number,
        angry: number,
        fear: number,
        disgust: number,
    ) => void;
}

const characters = [
    joyImg,
    sadImg,
    angryImg,
    fearImg,
    disgustImg,
    bingdbongImg,
];

const EmotionSection: React.FC<CharacterCarouselSectionProps> = ({
    className = "",
    onEmotionsChange,
}) => {
    const [joyValue, setJoyValue] = useState(50);
    const [sadValue, setSadValue] = useState(50);
    const [angryValue, setAngryValue] = useState(50);
    const [fearValue, setFearValue] = useState(50);
    const [disgustValue, setDisgustValue] = useState(50);

    // Store the baseline values for reset
    const [initialEmotions, setInitialEmotions] = useState({
        joy: 50,
        sad: 50,
        angry: 50,
        fear: 50,
        disgust: 50,
    });

    useEffect(() => {
        const loadEmotions = async () => {
            try {
                const res = await memberApi().getMyAverageEmotions();
                const data = res.data.data;
                // API returns decimals between 0 and 1
                const joy100 = Math.round(data.joy);
                const sad100 = Math.round(data.sadness);
                const angry100 = Math.round(data.anger);
                const fear100 = Math.round(data.fear);
                const disgust100 = Math.round(data.disgust);
                setJoyValue(joy100);
                setSadValue(sad100);
                setAngryValue(angry100);
                setFearValue(fear100);
                setDisgustValue(disgust100);
                setInitialEmotions({
                    joy: joy100,
                    sad: sad100,
                    angry: angry100,
                    fear: fear100,
                    disgust: disgust100,
                });
            } catch {
                // not logged in or error: keep defaults
            }
        };
        loadEmotions();
    }, []);

    return (
        <div
            className={`flex flex-col justify-center items-center ${className}`}
        >
            <div className="flex flex-wrap items-center justify-center space-x-4 overflow-x-auto scrollbar-hide px-2 py-4 m">
                <EmotionSlider
                    name="JOY"
                    color="#FFD602"
                    value={joyValue}
                    onChange={setJoyValue}
                    onChangeEnd={() =>
                        onEmotionsChange?.(
                            joyValue,
                            sadValue,
                            angryValue,
                            fearValue,
                            disgustValue,
                        )
                    }
                    image={characters[0]}
                />
                <EmotionSlider
                    name="SAD"
                    color="#1169F0"
                    value={sadValue}
                    onChange={setSadValue}
                    onChangeEnd={() =>
                        onEmotionsChange?.(
                            joyValue,
                            sadValue,
                            angryValue,
                            fearValue,
                            disgustValue,
                        )
                    }
                    image={characters[1]}
                />
                <EmotionSlider
                    name="ANGRY"
                    color="#DD2424"
                    value={angryValue}
                    onChange={setAngryValue}
                    onChangeEnd={() =>
                        onEmotionsChange?.(
                            joyValue,
                            sadValue,
                            angryValue,
                            fearValue,
                            disgustValue,
                        )
                    }
                    image={characters[2]}
                />
                <EmotionSlider
                    name="FEAR"
                    color="#9360BD"
                    value={fearValue}
                    onChange={setFearValue}
                    onChangeEnd={() =>
                        onEmotionsChange?.(
                            joyValue,
                            sadValue,
                            angryValue,
                            fearValue,
                            disgustValue,
                        )
                    }
                    image={characters[3]}
                />
                <EmotionSlider
                    name="DISGUST"
                    color="#A2C95A"
                    value={disgustValue}
                    onChange={setDisgustValue}
                    onChangeEnd={() =>
                        onEmotionsChange?.(
                            joyValue,
                            sadValue,
                            angryValue,
                            fearValue,
                            disgustValue,
                        )
                    }
                    image={characters[4]}
                />
            </div>
            <Button
                text="감정 초기화"
                textColor="white"
                buttonColor="transparent"
                className="w-full max-w-screen-sm"
                onClick={() => {
                    setJoyValue(initialEmotions.joy);
                    setSadValue(initialEmotions.sad);
                    setAngryValue(initialEmotions.angry);
                    setFearValue(initialEmotions.fear);
                    setDisgustValue(initialEmotions.disgust);
                }}
            />
        </div>
    );
};

export default EmotionSection;
