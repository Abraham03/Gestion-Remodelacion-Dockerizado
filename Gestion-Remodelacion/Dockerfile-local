# --- STAGE 1: Build the Angular application ---
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .

# Build for production
RUN npm run build
# --- STAGE 2: Serve the Angular application with Nginx ---
FROM nginx:alpine

# Copy the built Angular app to Nginx's HTML directory
COPY --from=build /app/dist/gestion-remodelacion/browser /usr/share/nginx/html

# Copy a custom Nginx configuration (opcional pero recomendado para SPAs)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]