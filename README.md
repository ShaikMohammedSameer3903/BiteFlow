# Food Delivery Platform

A comprehensive, scalable microservices-based food delivery platform with AI-driven features, built with modern technologies and best practices.

## 🏗️ Architecture

This platform follows a microservices architecture with the following components:

### Backend Services (Spring Boot + Java 17)
- **User Service** (Port 8081): Authentication, user management, JWT tokens, profiles
- **Restaurant Service** (Port 8082): Restaurant management, menu operations, order processing
- **Delivery Service** (Port 8083): Delivery tracking, route management, driver assignments
- **Payment Service** (Port 8084): Secure payment processing with Stripe integration
- **API Gateway** (Port 8080): Request routing, load balancing, authentication middleware

### Frontend (React + Redux Toolkit)
- **Customer Interface**: Browse restaurants, place orders, track deliveries
- **Restaurant Dashboard**: Menu management, order processing, analytics
- **Delivery Personnel Interface**: Route optimization, delivery tracking, earnings
- **Admin Panel**: Platform management, analytics, user management

### AI Service (Python + FastAPI)
- **ML Recommendations**: Personalized restaurant and menu suggestions
- **Route Optimization**: Intelligent delivery route planning and driver assignment
- **Chatbot**: AI-powered customer support with OpenAI integration

### Infrastructure & Data
- **PostgreSQL**: Primary database for all services
- **Redis**: Caching and session management
- **Docker**: Containerization for all services
- **JWT**: Secure authentication across services

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (for backend services)
- **Node.js 16+** (for frontend)
- **Python 3.9+** (for AI service)
- **Docker & Docker Compose** (recommended for easy setup)
- **PostgreSQL 13+**
- **Redis 6+**

### Quick Start with Docker

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd food-delivery-platform
   ```

2. **Set up environment variables**:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration (API keys, etc.)
   ```

3. **Start all services**:
   ```bash
   docker-compose up -d
   ```

4. **Access the application**:
   - Frontend: http://localhost:3000
   - API Gateway: http://localhost:8080
   - Individual services: 8081-8085

### Manual Development Setup

#### Backend Services
```bash
# Each service directory
cd backend/user-service
./mvnw spring-boot:run

# Repeat for other services
```

#### Frontend
```bash
cd frontend
npm install
npm start
```

#### AI Service
```bash
cd ai-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8085
```

## 📁 Project Structure

```
food-delivery-platform/
├── backend/
│   ├── user-service/           # Authentication & user management
│   ├── restaurant-service/     # Restaurant & menu management
│   ├── delivery-service/       # Delivery tracking & management
│   ├── payment-service/        # Payment processing
│   ├── api-gateway/           # API routing & authentication
│   └── common/                # Shared utilities & DTOs
├── frontend/                  # React application
│   ├── src/
│   │   ├── components/        # Reusable UI components
│   │   ├── pages/            # Page components by role
│   │   ├── store/            # Redux store & slices
│   │   └── services/         # API service layer
│   ├── public/               # Static assets
│   └── package.json
├── ai-service/               # Python FastAPI AI service
│   ├── app/
│   │   ├── core/            # Configuration & database
│   │   ├── models/          # SQLAlchemy models
│   │   ├── routers/         # API endpoints
│   │   └── services/        # ML & AI services
│   └── requirements.txt
├── docker-compose.yml        # Multi-service orchestration
├── .env.example             # Environment variables template
└── README.md               # This file
```

## 🎯 Features

### Core Functionality
- **Multi-role Authentication**: Customer, Restaurant Owner, Delivery Personnel, Admin
- **Real-time Order Tracking**: Live updates on order status and delivery location
- **Secure Payment Processing**: Stripe integration with PCI compliance
- **Responsive Design**: Mobile-first UI with Tailwind CSS
- **Role-based Access Control**: JWT-based security across all services

### AI-Powered Features
- **Personalized Recommendations**: ML-driven restaurant and menu suggestions
- **Smart Route Optimization**: Efficient delivery route planning
- **Intelligent Chatbot**: AI customer support with natural language processing
- **Demand Prediction**: Forecasting busy areas and peak times
- **Dynamic Pricing**: AI-suggested pricing based on demand and supply

### Business Features
- **Multi-restaurant Support**: Platform for multiple restaurant partners
- **Real-time Analytics**: Comprehensive dashboards for all stakeholders
- **Order Management**: Complete order lifecycle from placement to delivery
- **Driver Management**: Delivery personnel onboarding and tracking
- **Payment Integration**: Multiple payment methods and secure processing

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: PostgreSQL 13+
- **Caching**: Redis
- **Authentication**: JWT tokens
- **API Documentation**: OpenAPI/Swagger

### Frontend
- **Framework**: React 18
- **State Management**: Redux Toolkit
- **Routing**: React Router v6
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **Build Tool**: Create React App

### AI Service
- **Framework**: FastAPI
- **Language**: Python 3.9+
- **ML Libraries**: scikit-learn, pandas, numpy
- **NLP**: OpenAI GPT-3.5-turbo
- **Geolocation**: geopy, Google Maps API
- **Background Tasks**: Celery + Redis

### DevOps & Infrastructure
- **Containerization**: Docker & Docker Compose
- **Database**: PostgreSQL with connection pooling
- **Caching**: Redis for sessions and data caching
- **Load Balancing**: API Gateway with Spring Cloud Gateway
- **Monitoring**: Health checks and metrics endpoints

## 🔧 Development

### Running Individual Services

Each service can be developed independently:

```bash
# Backend service
cd backend/user-service
./mvnw spring-boot:run

# Frontend
cd frontend
npm start

# AI Service
cd ai-service
uvicorn app.main:app --reload
```

### API Documentation

- **API Gateway**: http://localhost:8080/swagger-ui.html
- **Individual Services**: Each service exposes Swagger UI on `/swagger-ui.html`
- **AI Service**: http://localhost:8085/docs

### Testing

```bash
# Backend tests
./mvnw test

# Frontend tests
npm test

# AI Service tests
pytest
```

## 🔐 Security

- **JWT Authentication**: Secure token-based authentication
- **Password Hashing**: bcrypt for secure password storage
- **API Rate Limiting**: Protection against abuse
- **Input Validation**: Comprehensive input sanitization
- **CORS Configuration**: Proper cross-origin resource sharing
- **Environment Variables**: Secure configuration management

## 📊 Monitoring & Analytics

### Health Checks
- Service health endpoints at `/health`
- Database connectivity monitoring
- Redis connection status
- AI model status tracking

### Analytics Features
- **Order Analytics**: Revenue, popular items, peak times
- **User Analytics**: Registration trends, user behavior
- **Delivery Analytics**: Route efficiency, delivery times
- **Restaurant Analytics**: Performance metrics, ratings

## 🚀 Deployment

### Production Deployment

1. **Environment Setup**:
   ```bash
   # Production environment variables
   cp .env.example .env.production
   # Configure production values
   ```

2. **Database Setup**:
   ```bash
   # Create production databases
   createdb food_delivery_prod
   createdb food_delivery_ai_prod
   ```

3. **Docker Deployment**:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

### Scaling Considerations
- **Horizontal Scaling**: Multiple instances behind load balancer
- **Database Optimization**: Connection pooling, read replicas
- **Caching Strategy**: Redis cluster for high availability
- **CDN Integration**: Static asset delivery optimization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow existing code style and conventions
- Write comprehensive tests for new features
- Update documentation for API changes
- Ensure all services pass health checks

## 📝 API Documentation

### Authentication
All protected endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

### Demo Accounts
The platform includes demo accounts for testing:
- **Customer**: demo@customer.com / password123
- **Restaurant**: demo@restaurant.com / password123
- **Delivery**: demo@delivery.com / password123
- **Admin**: demo@admin.com / password123

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection**: Ensure PostgreSQL is running and credentials are correct
2. **Redis Connection**: Verify Redis server is accessible
3. **Port Conflicts**: Check if ports 3000, 8080-8085 are available
4. **Environment Variables**: Verify all required variables are set
5. **Docker Issues**: Try `docker-compose down && docker-compose up --build`

### Performance Optimization
- Enable database indexing for frequently queried fields
- Implement API response caching
- Use connection pooling for database connections
- Optimize Docker images for faster startup

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot community for excellent framework
- React team for the powerful frontend library
- FastAPI for the modern Python web framework
- OpenAI for AI capabilities
- All contributors and testers

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check existing documentation
- Review troubleshooting section
- Contact the development team

---

**Built with ❤️ for the food delivery community**
