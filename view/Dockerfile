FROM node:10.16 as builder
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:1.17
RUN rm -rf /usr/share/nginx/html/*
ARG BACKEND_API_URL
ENV BACKEND_API_URL $BACKEND_API_URL
COPY --from=builder /app/dist /usr/share/nginx/html
COPY prod/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
COPY prod/run.sh /run.sh
RUN ["chmod", "+x", "/run.sh"]
ENTRYPOINT ["/run.sh"]
