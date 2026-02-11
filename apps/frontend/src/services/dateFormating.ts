import dayjs from "dayjs";

const dateFormat = (date: string) => {
    return dayjs(date).format("YYYY. M. D. HH:mm");
};

export default dateFormat;
