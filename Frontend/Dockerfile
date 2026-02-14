FROM node:22
WORKDIR /app
COPY package*.json ./
RUN npm install
# This line below is the most important—it copies style.css and index.jsx
COPY . . 
EXPOSE 5173
CMD ["npm", "run", "server"]