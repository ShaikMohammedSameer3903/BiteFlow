# Jenkins CI/CD Setup Guide for Food Delivery Platform

## Overview

This guide will help you set up Jenkins CI/CD pipeline for the Food Delivery Platform. The pipeline includes automated testing, building, security scanning, and deployment across multiple environments.

## Prerequisites

### 1. Jenkins Server Requirements
- **Jenkins Version**: 2.400+ (LTS recommended)
- **Java**: OpenJDK 17 or higher
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Storage**: At least 50GB free space
- **Docker**: Docker Engine and Docker Compose installed

### 2. Required Jenkins Plugins

Install the following plugins in Jenkins:

#### Essential Plugins
```
- Pipeline
- Docker Pipeline
- Git
- GitHub Integration
- Credentials Binding
- NodeJS Plugin
- Maven Integration
- JUnit Plugin
- HTML Publisher
- Slack Notification (optional)
- Blue Ocean (optional, for better UI)
```

#### Installation Command
```bash
# Install plugins via Jenkins CLI (optional)
java -jar jenkins-cli.jar -s http://localhost:8080/ install-plugin \
  workflow-aggregator docker-workflow git github \
  credentials-binding nodejs maven-plugin junit \
  htmlpublisher-plugin slack build-timeout
```

### 3. System Tools Configuration

Configure the following tools in Jenkins (Manage Jenkins → Global Tool Configuration):

#### Maven
- Name: `Maven-3.9`
- Install automatically: ✓
- Version: `3.9.4`

#### NodeJS
- Name: `18`
- Install automatically: ✓
- Version: `18.17.0`

#### Docker
- Name: `docker`
- Install automatically: ✓

## Jenkins Pipeline Setup

### 1. Create Jenkins Credentials

Go to **Manage Jenkins → Manage Credentials → Global → Add Credentials**

#### Required Credentials:
```
1. stripe-secret-key (Secret text)
   - ID: stripe-secret-key
   - Secret: Your Stripe secret key

2. stripe-publishable-key (Secret text)
   - ID: stripe-publishable-key
   - Secret: Your Stripe publishable key

3. openai-api-key (Secret text)
   - ID: openai-api-key
   - Secret: Your OpenAI API key

4. google-maps-api-key (Secret text)
   - ID: google-maps-api-key
   - Secret: Your Google Maps API key

5. docker-registry-credentials (Username with password)
   - ID: docker-registry-credentials
   - Username: Your Docker registry username
   - Password: Your Docker registry password

6. ssh-staging-server (SSH Username with private key)
   - ID: ssh-staging-server
   - Username: deployment user
   - Private Key: SSH private key for staging server

7. ssh-production-server (SSH Username with private key)
   - ID: ssh-production-server
   - Username: deployment user
   - Private Key: SSH private key for production server
```

### 2. Create Pipeline Job

1. **New Item** → **Pipeline** → Enter name: `food-delivery-platform`
2. **Pipeline** section:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: Your repository URL
   - Branch Specifier: `*/main` (or your default branch)
   - Script Path: `Jenkinsfile`

### 3. Configure Webhooks (Optional)

For automatic builds on code changes:

#### GitHub Webhook:
1. Go to your GitHub repository → Settings → Webhooks
2. Add webhook:
   - Payload URL: `http://your-jenkins-server:8080/github-webhook/`
   - Content type: `application/json`
   - Events: `Just the push event`

## Environment-Specific Configurations

### 1. Staging Environment Setup

Create `docker-compose.staging.yml`:

```yaml
version: '3.8'

services:
  # Similar to main docker-compose.yml but with staging-specific configs
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: food_delivery_staging
    volumes:
      - mysql_staging_data:/var/lib/mysql

  # ... other services with staging configurations

volumes:
  mysql_staging_data:
```

### 2. Production Environment Setup

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  # Production-optimized configurations
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: food_delivery_prod
    volumes:
      - mysql_prod_data:/var/lib/mysql
    restart: unless-stopped

  # ... other services with production configurations

volumes:
  mysql_prod_data:
```

## Pipeline Features

### 1. Automated Testing
- **Unit Tests**: Java (JUnit), JavaScript (Jest), Python (pytest)
- **Integration Tests**: Full stack testing with Docker Compose
- **Code Coverage**: Reports generated for all services
- **Security Scanning**: OWASP dependency check, Docker image scanning

### 2. Build Process
- **Backend**: Maven builds for all Java microservices
- **Frontend**: npm build with optimization
- **AI Service**: Python package building and testing
- **Docker Images**: Multi-stage builds for production

### 3. Quality Gates
- **Code Linting**: ESLint (Frontend), Checkstyle (Backend), Flake8 (Python)
- **Security Scans**: Dependency vulnerability checks
- **Test Coverage**: Minimum coverage thresholds
- **Health Checks**: Service availability verification

### 4. Deployment Strategies
- **Staging**: Automatic deployment on develop branch
- **Production**: Manual approval required for main branch
- **Blue-Green**: Zero-downtime production deployments
- **Rollback**: Automatic rollback on deployment failure

## Running the Pipeline

### 1. Manual Trigger
1. Go to Jenkins dashboard
2. Click on `food-delivery-platform` job
3. Click **Build Now**

### 2. Automatic Trigger
- Push to any branch triggers the pipeline
- Different stages run based on branch:
  - `feature/*`: Build and test only
  - `develop`: Build, test, and deploy to staging
  - `main`: Build, test, and deploy to production (with approval)

### 3. Pipeline Stages Overview

```
1. Checkout - Get source code
2. Setup Infrastructure - Start test databases
3. Code Quality & Security - Linting and security scans
4. Build & Test - Compile and test all services
5. Integration Tests - Full stack testing
6. Build Docker Images - Create container images
7. Security Scan Images - Vulnerability scanning
8. Deploy to Staging - Staging environment deployment
9. Deploy to Production - Production deployment (manual approval)
```

## Monitoring and Notifications

### 1. Slack Integration (Optional)

Add Slack webhook URL in Jenkins:
1. **Manage Jenkins** → **Configure System**
2. **Slack** section:
   - Workspace: Your workspace name
   - Credential: Add Slack token
   - Default channel: `#deployments`

### 2. Email Notifications

Configure SMTP in Jenkins:
1. **Manage Jenkins** → **Configure System**
2. **E-mail Notification** section:
   - SMTP server: Your SMTP server
   - Configure authentication and security

## Troubleshooting

### Common Issues

#### 1. Docker Permission Issues
```bash
# Add jenkins user to docker group
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

#### 2. Maven Build Failures
```bash
# Ensure Maven settings.xml is configured
# Check Java version compatibility
mvn -version
```

#### 3. Node.js Build Issues
```bash
# Clear npm cache
npm cache clean --force
# Use specific Node.js version
nvm use 18
```

#### 4. Database Connection Issues
```bash
# Check if MySQL container is running
docker ps | grep mysql
# Check database logs
docker-compose -f docker-compose.test.yml logs mysql
```

### Pipeline Debugging

#### View Build Logs
1. Go to specific build number
2. Click **Console Output**
3. Check for error messages and stack traces

#### Docker Issues
```bash
# Clean up Docker resources
docker system prune -a
# Check Docker daemon status
sudo systemctl status docker
```

## Security Best Practices

### 1. Credentials Management
- Never hardcode secrets in Jenkinsfile
- Use Jenkins Credentials Store
- Rotate credentials regularly
- Use least privilege principle

### 2. Network Security
- Run Jenkins behind reverse proxy
- Use HTTPS for Jenkins UI
- Restrict access to Jenkins server
- Use VPN for remote access

### 3. Pipeline Security
- Scan Docker images for vulnerabilities
- Use official base images
- Keep dependencies updated
- Implement security gates in pipeline

## Performance Optimization

### 1. Build Performance
- Use Docker layer caching
- Parallel build stages
- Incremental builds where possible
- Optimize Docker images

### 2. Resource Management
- Set build timeouts
- Limit concurrent builds
- Clean up old builds automatically
- Monitor disk usage

## Maintenance

### 1. Regular Tasks
- Update Jenkins and plugins monthly
- Review and rotate credentials quarterly
- Clean up old builds and artifacts
- Monitor system resources

### 2. Backup Strategy
- Backup Jenkins configuration
- Backup build artifacts
- Document pipeline configurations
- Test restore procedures

## Support

For issues and questions:
1. Check Jenkins logs: `/var/log/jenkins/jenkins.log`
2. Review pipeline console output
3. Check Docker container logs
4. Verify network connectivity between services

## Additional Resources

- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)
- [Docker in Jenkins](https://www.jenkins.io/doc/book/pipeline/docker/)
- [Jenkins Best Practices](https://www.jenkins.io/doc/book/pipeline/pipeline-best-practices/)

---

**Note**: Replace placeholder values (server URLs, credentials, etc.) with your actual configuration values before using this pipeline.
