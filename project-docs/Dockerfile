FROM node:20-alpine
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install --production
COPY server.js .
COPY data/ data/
ENV DATA_DIR=/app/data
EXPOSE 3333
CMD ["node", "server.js"]
