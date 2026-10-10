# syntax=docker/dockerfile:1
FROM node:22-alpine AS builder
WORKDIR /app

# Copy dashboard package manifest
COPY dashboard/package*.json ./
RUN npm ci

# Copy dashboard frontend source and build
COPY dashboard/ ./
RUN npm run build

# Nginx production runtime
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

RUN rm -rf ./*
COPY --from=builder /app/dist .
COPY dashboard/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
