# =============================================================================
# ATLAS DISTRIBUTED COMMERCE - MAKEFILE
# =============================================================================
# Automation for building, running, and managing the microservices architecture
# =============================================================================

.PHONY: help build-all build-shared build-infra build-services \
        up up-infra down restart logs logs-service \
        clean clean-volumes clean-images \
        health-check validate-compose

# Default target
.DEFAULT_GOAL := help

# =============================================================================
# VARIABLES
# =============================================================================
COMPOSE := docker-compose
MVN := mvn
MVN_OPTS := -Dmaven.test.skip=true -q

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m

# Service URLs for health checks
NGINX_URL := http://localhost
GATEWAY_URL := http://localhost:8080
EUREKA_URL := http://localhost:8761
USER_SERVICE_URL := http://localhost:8081
PRODUCT_SERVICE_URL := http://localhost:8082
ORDER_SERVICE_URL := http://localhost:8083
PAYMENT_SERVICE_URL := http://localhost:8084
NOTIFICATION_SERVICE_URL := http://localhost:8085

# =============================================================================
# HELP
# =============================================================================
help: ## Show this help message
	@echo "Atlas Distributed Commerce - Available Commands:"
	@echo ""
	@echo "BUILD COMMANDS:"
	@grep -E '^build-[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "DOCKER COMMANDS:"
	@grep -E '^(up|down|restart|logs|clean)[a-zA-Z_-]*:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "VALIDATION COMMANDS:"
	@grep -E '^(health|validate)[a-zA-Z_-]*:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'

# =============================================================================
# BUILD COMMANDS
# =============================================================================
build-all: build-shared build-infra build-services ## Build all Maven modules
	@echo "$(GREEN)✓ All modules built successfully$(NC)"

build-shared: ## Build shared modules (common-models, common-utils)
	@echo "$(YELLOW)Installing parent POM...$(NC)"
	$(MVN) install -N $(MVN_OPTS)
	@echo "$(YELLOW)Building shared modules...$(NC)"
	$(MVN) install -pl shared/common-models,shared/common-utils $(MVN_OPTS)
	@echo "$(GREEN)✓ Shared modules built$(NC)"

build-infra: ## Build infrastructure services (eureka, config-server, api-gateway)
	@echo "$(YELLOW)Building infrastructure services...$(NC)"
	$(MVN) package -pl infrastructure/eureka-server $(MVN_OPTS)
	$(MVN) package -pl infrastructure/config-server $(MVN_OPTS)
	$(MVN) package -pl infrastructure/api-gateway $(MVN_OPTS)
	@echo "$(GREEN)✓ Infrastructure services built$(NC)"

build-services: ## Build application services
	@echo "$(YELLOW)Building application services...$(NC)"
	$(MVN) package -pl services/user-service $(MVN_OPTS)
	$(MVN) package -pl services/product-service $(MVN_OPTS)
	$(MVN) package -pl services/order-service $(MVN_OPTS)
	$(MVN) package -pl services/payment-service $(MVN_OPTS)
	$(MVN) package -pl services/notification-service $(MVN_OPTS)
	@echo "$(GREEN)✓ Application services built$(NC)"

build-images: ## Build Docker images for all services
	@echo "$(YELLOW)Building Docker images...$(NC)"
	$(COMPOSE) build
	@echo "$(GREEN)✓ Docker images built$(NC)"

# =============================================================================
# DOCKER COMMANDS
# =============================================================================
up: validate-compose ## Start all containers
	@echo "$(YELLOW)Starting all containers...$(NC)"
	$(COMPOSE) up -d
	@echo "$(GREEN)✓ All containers started$(NC)"
	@echo "$(YELLOW)Waiting for services to be healthy...$(NC)"
	@sleep 10
	@$(MAKE) health-check

up-infra: ## Start infrastructure only (DBs, Redis, RabbitMQ, monitoring)
	@echo "$(YELLOW)Starting infrastructure containers...$(NC)"
	$(COMPOSE) up -d postgres-users postgres-products postgres-orders postgres-payments redis rabbitmq zipkin prometheus grafana
	@echo "$(GREEN)✓ Infrastructure containers started$(NC)"

up-core: ## Start core infrastructure services (Eureka, Config, Gateway)
	@echo "$(YELLOW)Starting core infrastructure services...$(NC)"
	$(COMPOSE) up -d eureka-server config-server api-gateway nginx
	@echo "$(GREEN)✓ Core infrastructure services started$(NC)"

up-apps: ## Start application services only
	@echo "$(YELLOW)Starting application services...$(NC)"
	$(COMPOSE) up -d user-service product-service order-service payment-service notification-service
	@echo "$(GREEN)✓ Application services started$(NC)"

down: ## Stop and remove all containers
	@echo "$(YELLOW)Stopping all containers...$(NC)"
	$(COMPOSE) down
	@echo "$(GREEN)✓ All containers stopped$(NC)"

restart: down up ## Restart all containers

logs: ## Show logs from all services
	$(COMPOSE) logs -f

logs-service: ## Show logs from a specific service (use: make logs-service SERVICE=user-service)
	$(COMPOSE) logs -f $(SERVICE)

# =============================================================================
# CLEANUP COMMANDS
# =============================================================================
clean: down ## Stop containers and remove volumes
	@echo "$(YELLOW)Cleaning up...$(NC)"
	$(COMPOSE) down -v --remove-orphans
	@echo "$(GREEN)✓ Cleanup complete$(NC)"

clean-volumes: ## Remove all Docker volumes
	@echo "$(YELLOW)Removing volumes...$(NC)"
	$(COMPOSE) down -v
	@echo "$(GREEN)✓ Volumes removed$(NC)"

clean-images: ## Remove all project Docker images
	@echo "$(YELLOW)Removing project images...$(NC)"
	docker images | grep "atlas" | awk '{print $$3}' | xargs -r docker rmi -f
	@echo "$(GREEN)✓ Images removed$(NC)"

clean-all: clean clean-images ## Full cleanup (containers, volumes, and images)
	@echo "$(GREEN)✓ Full cleanup complete$(NC)"

# =============================================================================
# VALIDATION COMMANDS
# =============================================================================
validate-compose: ## Validate docker-compose configuration
	@echo "$(YELLOW)Validating docker-compose configuration...$(NC)"
	@$(COMPOSE) config > /dev/null 2>&1 && echo "$(GREEN)✓ Configuration valid$(NC)" || (echo "$(RED)✗ Configuration invalid$(NC)" && exit 1)

health-check: ## Check health of all services
	@echo "$(YELLOW)Checking service health...$(NC)"
	@echo ""
	@echo "Nginx (Port 80):"
	@curl -sf $(NGINX_URL)/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""
	@echo "API Gateway (Port 8080):"
	@curl -sf $(GATEWAY_URL)/actuator/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""
	@echo "Eureka Server (Port 8761):"
	@curl -sf $(EUREKA_URL)/actuator/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""
	@echo "User Service (Port 8081):"
	@curl -sf $(USER_SERVICE_URL)/actuator/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""
	@echo "Product Service (Port 8082):"
	@curl -sf $(PRODUCT_SERVICE_URL)/actuator/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""
	@echo "Order Service (Port 8083):"
	@curl -sf $(ORDER_SERVICE_URL)/actuator/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""
	@echo "Payment Service (Port 8084):"
	@curl -sf $(PAYMENT_SERVICE_URL)/actuator/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""
	@echo "Notification Service (Port 8085):"
	@curl -sf $(NOTIFICATION_SERVICE_URL)/actuator/health > /dev/null 2>&1 && echo "  $(GREEN)✓ Healthy$(NC)" || echo "  $(RED)✗ Not responding$(NC)"
	@echo ""

# =============================================================================
# DEVELOPMENT SHORTCUTS
# =============================================================================
dev: build-all up ## Build all and start containers (full development setup)

quick-start: up-infra ## Quick start: infrastructure only
	@echo "$(YELLOW)Infrastructure is starting. Run 'make up-core' once DBs are healthy.$(NC)"
