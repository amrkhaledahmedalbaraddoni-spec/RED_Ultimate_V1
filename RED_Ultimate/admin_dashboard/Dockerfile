# RED Admin Dashboard - Static Server
FROM node:22-alpine

WORKDIR /app
COPY package.json ./
RUN npm install --production 2>/dev/null || true

# Copy all source files (will be served via simple static server)
COPY . .

# Install a simple static file server
RUN npm install -g serve 2>/dev/null || true

EXPOSE 3000

CMD ["npx", "serve", "-s", "public", "-l", "3000"]
