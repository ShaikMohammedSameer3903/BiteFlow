# Food Delivery Platform - AI Service

The AI Service provides intelligent features for the food delivery platform including personalized recommendations, route optimization, and an interactive chatbot powered by machine learning and natural language processing.

## Features

### 🤖 Machine Learning Recommendations
- **Restaurant Recommendations**: Personalized restaurant suggestions based on user preferences and order history
- **Menu Item Recommendations**: Smart menu item suggestions within restaurants
- **Collaborative Filtering**: User-based and item-based recommendation algorithms
- **Popularity-based Recommendations**: Trending restaurants and cuisines
- **Real-time Model Training**: Continuous learning from user interactions

### 🗺️ Route Optimization
- **Delivery Route Optimization**: Efficient route planning for delivery personnel using nearest neighbor algorithms
- **Travel Time Prediction**: Accurate delivery time estimates using distance and traffic data
- **Driver Assignment**: Optimal delivery person selection based on location, capacity, and ratings
- **Demand Prediction**: Forecasting delivery demand by area and time
- **Google Maps Integration**: Real-time distance and travel time calculations

### 💬 Intelligent Chatbot
- **Natural Language Processing**: Understanding user queries and providing contextual responses
- **OpenAI Integration**: Advanced conversational AI capabilities
- **Intent Recognition**: Automatic detection of user intents (restaurant search, order tracking, etc.)
- **Personalized Responses**: Context-aware responses based on user history
- **Multi-turn Conversations**: Maintaining conversation context across multiple interactions

## Technology Stack

- **Framework**: FastAPI (Python)
- **Machine Learning**: scikit-learn, pandas, numpy
- **NLP**: OpenAI GPT-3.5-turbo
- **Database**: PostgreSQL with SQLAlchemy ORM
- **Caching**: Redis
- **Background Tasks**: Celery
- **Geolocation**: geopy, Google Maps API
- **Authentication**: JWT tokens

## Project Structure

```
ai-service/
├── app/
│   ├── core/
│   │   ├── config.py          # Configuration settings
│   │   ├── database.py        # Database connection
│   │   └── security.py        # JWT authentication
│   ├── models/
│   │   └── user_interaction.py # Database models
│   ├── routers/
│   │   ├── recommendations.py  # Recommendation endpoints
│   │   ├── routing.py         # Route optimization endpoints
│   │   └── chatbot.py         # Chatbot endpoints
│   ├── services/
│   │   ├── ml_models.py       # ML model service
│   │   ├── route_optimizer.py # Route optimization service
│   │   └── chatbot.py         # Chatbot service
│   └── main.py               # FastAPI application
├── models/                   # Trained ML models (created at runtime)
├── requirements.txt          # Python dependencies
├── .env.example             # Environment variables template
└── README.md               # This file
```

## Installation

### Prerequisites
- Python 3.8+
- PostgreSQL
- Redis
- OpenAI API key (optional, for advanced chatbot features)
- Google Maps API key (optional, for accurate routing)

### Setup

1. **Clone the repository** (if not already done):
   ```bash
   git clone <repository-url>
   cd food-delivery-platform/ai-service
   ```

2. **Create virtual environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Set up environment variables**:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

5. **Set up PostgreSQL database**:
   ```sql
   CREATE DATABASE food_delivery_ai;
   CREATE USER ai_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE food_delivery_ai TO ai_user;
   ```

6. **Start Redis server**:
   ```bash
   redis-server
   ```

7. **Run the service**:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8085 --reload
   ```

## Configuration

### Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `DATABASE_URL` | PostgreSQL connection string | Yes | - |
| `REDIS_URL` | Redis connection string | Yes | `redis://localhost:6379/0` |
| `JWT_SECRET_KEY` | Secret key for JWT tokens | Yes | - |
| `OPENAI_API_KEY` | OpenAI API key for chatbot | No | - |
| `GOOGLE_MAPS_API_KEY` | Google Maps API key for routing | No | - |
| `USER_SERVICE_URL` | User service URL | Yes | `http://localhost:8081` |
| `RESTAURANT_SERVICE_URL` | Restaurant service URL | Yes | `http://localhost:8082` |
| `DELIVERY_SERVICE_URL` | Delivery service URL | Yes | `http://localhost:8083` |
| `MIN_TRAINING_DATA` | Minimum data points for ML training | No | `50` |
| `MODEL_UPDATE_INTERVAL` | Model retraining interval (seconds) | No | `86400` |

## API Endpoints

### Recommendations

- `GET /api/recommendations/restaurants/{user_id}` - Get restaurant recommendations
- `GET /api/recommendations/menu/{user_id}/{restaurant_id}` - Get menu recommendations
- `POST /api/recommendations/log-interaction` - Log user interaction
- `GET /api/recommendations/popular/restaurants` - Get popular restaurants
- `GET /api/recommendations/trending/cuisines` - Get trending cuisines
- `GET /api/recommendations/personalized/{user_id}` - Get personalized recommendations
- `POST /api/recommendations/feedback` - Submit recommendation feedback

### Route Optimization

- `POST /api/routing/optimize-route` - Optimize delivery route
- `POST /api/routing/find-optimal-driver` - Find optimal delivery person
- `GET /api/routing/travel-time` - Calculate travel time
- `GET /api/routing/distance` - Calculate distance
- `POST /api/routing/predict-demand` - Predict delivery demand
- `GET /api/routing/geocode` - Convert address to coordinates
- `POST /api/routing/batch-optimize` - Batch route optimization

### Chatbot

- `POST /api/chatbot/start-session` - Start chat session
- `POST /api/chatbot/chat` - Send message to chatbot
- `GET /api/chatbot/history/{session_id}` - Get chat history
- `GET /api/chatbot/sessions/{user_id}` - Get user chat sessions
- `POST /api/chatbot/quick-help` - Get quick help responses
- `POST /api/chatbot/suggest-responses` - Get suggested responses
- `POST /api/chatbot/feedback` - Submit chat feedback
- `GET /api/chatbot/analytics/popular-queries` - Get popular queries

### System

- `GET /` - Service information
- `GET /health` - Health check
- `GET /api/status` - Detailed service status

## Machine Learning Models

### Recommendation System

The recommendation system uses multiple approaches:

1. **Collaborative Filtering**: Finds users with similar preferences and recommends items they liked
2. **Content-Based Filtering**: Recommends items similar to what the user has interacted with
3. **Popularity-Based**: Recommends trending and popular items
4. **Hybrid Approach**: Combines multiple methods for better accuracy

### Route Optimization

The route optimizer uses:

1. **Nearest Neighbor Algorithm**: Finds the shortest path through delivery points
2. **Distance Calculation**: Uses geopy for accurate distance measurements
3. **Travel Time Prediction**: Considers traffic patterns and delivery constraints
4. **Demand Forecasting**: Predicts busy areas and times

### Model Training

- Models are trained automatically on startup if sufficient data is available
- Continuous learning from user interactions
- Periodic retraining based on `MODEL_UPDATE_INTERVAL`
- Fallback to default models when insufficient training data

## Integration with Other Services

### User Service
- Fetches user profiles and preferences
- Validates JWT tokens

### Restaurant Service
- Retrieves restaurant and menu data
- Gets order history for recommendations

### Delivery Service
- Optimizes delivery routes
- Assigns optimal delivery personnel

### Frontend Integration
- Provides recommendation widgets
- Powers the chatbot interface
- Supplies route optimization data

## Development

### Running Tests
```bash
pytest tests/
```

### Code Style
```bash
black app/
flake8 app/
```

### Adding New Features

1. **New ML Model**: Add to `services/ml_models.py`
2. **New Route Algorithm**: Extend `services/route_optimizer.py`
3. **New Chatbot Intent**: Update `services/chatbot.py`
4. **New API Endpoint**: Add to appropriate router in `routers/`

## Deployment

### Docker Deployment
```bash
docker build -t food-delivery-ai .
docker run -p 8085:8085 --env-file .env food-delivery-ai
```

### Production Considerations

- Use production-grade PostgreSQL and Redis instances
- Set up proper logging and monitoring
- Configure load balancing for high availability
- Use environment-specific configuration
- Set up model versioning and rollback capabilities
- Implement proper error handling and retry logic

## Monitoring and Analytics

### Health Checks
- Service health endpoint at `/health`
- Model status monitoring
- Database connectivity checks

### Performance Metrics
- Recommendation accuracy tracking
- Route optimization efficiency
- Chatbot response quality
- API response times

### Logging
- Structured logging with correlation IDs
- User interaction logging for model improvement
- Error tracking and alerting

## Security

- JWT token validation for all protected endpoints
- Input validation and sanitization
- Rate limiting on API endpoints
- Secure handling of API keys
- Data privacy compliance

## Troubleshooting

### Common Issues

1. **Models not loading**: Check if training data is sufficient (`MIN_TRAINING_DATA`)
2. **Database connection errors**: Verify PostgreSQL connection string
3. **Redis connection issues**: Ensure Redis server is running
4. **OpenAI API errors**: Check API key and quota limits
5. **Google Maps errors**: Verify API key and enable required services

### Performance Optimization

- Increase `MODEL_UPDATE_INTERVAL` for less frequent retraining
- Use Redis caching for frequently accessed data
- Optimize database queries with proper indexing
- Consider model quantization for faster inference

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is part of the Food Delivery Platform and follows the same licensing terms.

## Support

For technical support or questions about the AI Service:
- Check the troubleshooting section
- Review API documentation
- Contact the development team
