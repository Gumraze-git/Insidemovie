function calcPercentChange(current: number, previous: number): number | null {
    if (previous === 0) {
        return current === 0 ? 0 : 100;
    }
    const result = ((current - previous) / previous) * 100;
    return Math.round(result * 100) / 100;
}

export default calcPercentChange;
