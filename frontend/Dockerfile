FROM node:14.21.3-bullseye AS build
WORKDIR /build
COPY package.json package-lock.json ./
RUN CYPRESS_INSTALL_BINARY=0 HUSKY_SKIP_INSTALL=1 npm ci --no-audit
COPY . .
# npm 6 can skip the project's postinstall when installing as root.
RUN npm run postinstall && npm run build:prod

FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/cardwiki /usr/share/nginx/html
EXPOSE 80
