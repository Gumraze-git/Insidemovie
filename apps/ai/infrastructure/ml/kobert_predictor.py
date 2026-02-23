from __future__ import annotations

import torch
from kobert_tokenizer import KoBERTTokenizer
from transformers import AutoModelForSequenceClassification

from const import LABELS_5


class KoBertPredictor:
    def __init__(self, model_dir: str) -> None:
        self._tokenizer = KoBERTTokenizer.from_pretrained(model_dir)
        self._model = AutoModelForSequenceClassification.from_pretrained(model_dir)
        self._model.eval()

    @staticmethod
    def _split_sentences(text: str) -> list[str]:
        sentences = [sentence.strip() for sentence in text.split(".") if sentence.strip()]
        return sentences or [text.strip()]

    @staticmethod
    def _average_probabilities(results: list[dict[str, float]]) -> dict[str, float]:
        if not results:
            return {label: 0.0 for label in LABELS_5}

        sums = {label: 0.0 for label in LABELS_5}
        for probability in results:
            for label, score in probability.items():
                sums[label] += score

        count = len(results)
        return {label: sums[label] / count for label in LABELS_5}

    @staticmethod
    def _format_percent(probabilities: dict[str, float]) -> dict[str, float]:
        return {label: round(probabilities[label] * 100, 2) for label in LABELS_5}

    def _predict_batch(self, texts: list[str]) -> list[dict[str, float]]:
        inputs = self._tokenizer(
            texts,
            return_tensors="pt",
            truncation=True,
            padding=True,
            max_length=128,
        )

        with torch.no_grad():
            logits = self._model(**inputs).logits
            probabilities = torch.softmax(logits, dim=-1)

        return [
            {LABELS_5[index]: float(probability[index]) for index in range(len(LABELS_5))}
            for probability in probabilities
        ]

    def predict(self, text: str, aggregation: str) -> dict[str, float]:
        stripped_text = text.strip()

        if aggregation == "full":
            [raw] = self._predict_batch([stripped_text])
            return self._format_percent(raw)

        sentences = self._split_sentences(stripped_text)

        if aggregation == "split_avg":
            raw_probabilities = self._predict_batch(sentences)
            averaged = self._average_probabilities(raw_probabilities)
            return self._format_percent(averaged)

        # default: overall_avg
        raw_probabilities = self._predict_batch([stripped_text] + sentences)
        averaged = self._average_probabilities(raw_probabilities)
        return self._format_percent(averaged)
