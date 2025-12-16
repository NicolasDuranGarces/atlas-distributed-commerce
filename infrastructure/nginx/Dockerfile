FROM nginx:1.25-alpine

LABEL maintainer="Atlas Commerce Team"
LABEL description="Nginx Reverse Proxy"

# Remove default config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom config
COPY nginx.conf /etc/nginx/nginx.conf

# Create non-root user
RUN addgroup -g 1001 atlas && adduser -u 1001 -G atlas -s /bin/sh -D atlas

# Set permissions
RUN chown -R atlas:atlas /var/cache/nginx && \
    chown -R atlas:atlas /var/log/nginx && \
    touch /var/run/nginx.pid && \
    chown -R atlas:atlas /var/run/nginx.pid

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget -q --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
