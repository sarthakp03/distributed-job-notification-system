FROM node:latest
WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
COPY . .
RUN npm install
RUN npm run build
EXPOSE 3005
CMD ["npm", "start"]
