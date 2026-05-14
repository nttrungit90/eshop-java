# Copies a pre-built React SPA into nginx.
#
# Build on the host first (cd clients/webapp && npm install && npm run build);
# this Dockerfile then just bundles the dist/ output. Matches the pattern used
# by every Java service in this repo and avoids npm-registry network issues
# inside the docker builder.

FROM nginx:alpine

COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
