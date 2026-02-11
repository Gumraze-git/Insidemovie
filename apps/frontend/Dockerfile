FROM node:24-slim AS build
WORKDIR /movie
ENV NODE_OPTIONS=--max_old_space_size=4096
COPY . .
RUN npm install

RUN npm run build

FROM nginx:alpine
COPY --from=build /movie/dist /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]