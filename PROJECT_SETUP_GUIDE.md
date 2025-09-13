# Food Delivery Platform - Complete Setup & Run Guide

## Prerequisites
1. **Docker Desktop** - Download from https://www.docker.com/products/docker-desktop/
2. **Node.js** (v16 or higher) - Download from https://nodejs.org/
3. **Java 17** - Download from https://adoptium.net/
4. **Maven** - Download from https://maven.apache.org/download.cgi

## Step 1: Get Your API Keys (IMPORTANT - Replace Placeholder Keys)

### 1.1 Google Maps API Key
1. Go to https://console.cloud.google.com/
2. Create a new project or select existing one
3. Enable "Maps JavaScript API" and "Places API"
4. Go to "Credentials" → "Create Credentials" → "API Key"
5. Copy your API key

### 1.2 Stripe API Keys
1. Go to https://dashboard.stripe.com/register
2. Create account and verify email
3. Go to "Developers" → "API Keys"
4. Copy both "Publishable key" and "Secret key"

### 1.3 OpenAI API Key
1. Go to https://platform.openai.com/signup
2. Create account and add payment method
3. Go to "API Keys" → "Create new secret key"
4. Copy your API key

### 1.4 Update .env File
Replace the placeholder keys in `.env` file with your real keys:
```env
STRIPE_SECRET_KEY=sk_test_your_real_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_real_stripe_publishable_key
OPENAI_API_KEY=sk-your_real_openai_api_key
GOOGLE_MAPS_API_KEY=AIzaSy_your_real_google_maps_api_key
```

## Step 2: Project Setup

### 2.1 Clone/Navigate to Project
```bash
cd c:\Users\shaik\CascadeProjects\food-delivery-platform
```

### 2.2 Install Frontend Dependencies
```bash
cd frontend
npm install
cd ..
```

## Step 3: Running the Project

### Method 1: Using Docker (Recommended)

#### 3.1 Start All Services
```bash
docker-compose up --build
```

This will start:
- MySQL Database (port 3306)
- Redis (port 6379)
- User Service (port 8081)
- Restaurant Service (port 8082)
- Delivery Service (port 8083)
- Payment Service (port 8084)
- AI Service (port 8085)
- API Gateway (port 8888)
- Frontend (port 3000)

#### 3.2 Access the Application
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8888
- **MySQL**: localhost:3306 (username: root, password: shaik)

### Method 2: Manual Setup (Alternative)

#### 3.1 Start Database & Redis
```bash
docker-compose up mysql redis -d
```

#### 3.2 Start Backend Services (in separate terminals)
```bash
# Terminal 1 - User Service
cd backend/user-service
mvn spring-boot:run

# Terminal 2 - Restaurant Service
cd backend/restaurant-service
mvn spring-boot:run

# Terminal 3 - Delivery Service
cd backend/delivery-service
mvn spring-boot:run

# Terminal 4 - Payment Service
cd backend/payment-service
mvn spring-boot:run

# Terminal 5 - API Gateway
cd backend/api-gateway
mvn spring-boot:run
```

#### 3.3 Start AI Service
```bash
cd ai-service
pip install -r requirements.txt
python app/main.py
```

#### 3.4 Start Frontend
```bash
cd frontend
npm start
```

## Step 4: Verify Everything is Working

### 4.1 Check Services Status
Visit these URLs to verify services are running:
- http://localhost:8081/actuator/health (User Service)
- http://localhost:8082/actuator/health (Restaurant Service)
- http://localhost:8083/actuator/health (Delivery Service)
- http://localhost:8084/actuator/health (Payment Service)
- http://localhost:8888/actuator/health (API Gateway)
- http://localhost:8085/health (AI Service)

### 4.2 Check Frontend
- Open http://localhost:3000
- You should see the food delivery platform homepage

## Step 5: Database Setup

The MySQL database will be automatically created with the following credentials:
- **Host**: localhost:3306
- **Database**: food_delivery
- **Username**: root
- **Password**: shaik
- **Root Password**: shaik

## Troubleshooting

### Common Issues:

1. **Port Already in Use**
   ```bash
   # Stop all Docker containers
   docker-compose down
   # Kill processes using ports
   netstat -ano | findstr :3000
   taskkill /PID <PID_NUMBER> /F
   ```

2. **Database Connection Issues**
   - Ensure MySQL container is running: `docker ps`
   - Check database logs: `docker-compose logs mysql`

3. **Frontend Not Loading**
   - Clear browser cache
   - Check if API Gateway is running on port 8888
   - Verify .env file has correct API_URL

4. **API Keys Not Working**
   - Ensure you've replaced ALL placeholder keys with real ones
   - Check API key permissions and billing status
   - Restart services after updating keys

### Useful Commands:

```bash
# View all running containers
docker ps

# View logs for specific service
docker-compose logs [service-name]

# Restart specific service
docker-compose restart [service-name]

# Stop all services
docker-compose down

# Remove all containers and volumes
docker-compose down -v

# Rebuild and start
docker-compose up --build --force-recreate
```

## Project Structure
```
food-delivery-platform/
├── frontend/          # React.js frontend
├── backend/           # Spring Boot microservices
│   ├── api-gateway/   # API Gateway (port 8888)
│   ├── user-service/  # User management (port 8081)
│   ├── restaurant-service/ # Restaurant management (port 8082)
│   ├── delivery-service/   # Delivery management (port 8083)
│   └── payment-service/    # Payment processing (port 8084)
├── ai-service/        # Python AI service (port 8085)
├── docker-compose.yml # Docker configuration
└── .env              # Environment variables
```

## Success Indicators
✅ All Docker containers are running
✅ Frontend loads at http://localhost:3000
✅ API Gateway responds at http://localhost:8888
✅ Database connection successful
✅ Real maps display (with your Google Maps API key)
✅ No console errors in browser

## Next Steps
1. Replace placeholder API keys with real ones
2. Test user registration and login
3. Test restaurant listings
4. Test order placement
5. Test payment processing (with Stripe test cards)

For support, check the logs using `docker-compose logs [service-name]`
