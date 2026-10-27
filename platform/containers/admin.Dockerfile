# RED admin dashboard — static SPA. Source: services/admin-dashboard.
# Build context MUST be the repository root. The SPA talks to relative /api and
# /ws paths, so no runtime API URL needs to be injected here.
FROM node:22-alpine AS builder
WORKDIR /build
COPY services/admin-dashboard/package.json services/admin-dashboard/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY services/admin-dashboard/ ./
RUN npm run build

FROM nginx:1.27-alpine
COPY platform/proxy/dashboard.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /build/dist /usr/share/nginx/html
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://127.0.0.1:3000/ >/dev/null || exit 1
