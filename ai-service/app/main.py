from fastapi import FastAPI, HTTPException, Depends, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import uvicorn
import os
from contextlib import asynccontextmanager
from app.core.config import settings
from app.core.database import engine, Base
from app.routers import recommendations, routing, chatbot
from app.services.ml_models import MLModelService
from app.services.route_optimizer import RouteOptimizer
from app.services.chatbot import ChatbotService

# Global service instances
ml_service = None
route_optimizer = None
chatbot_service = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    global ml_service, route_optimizer, chatbot_service
    
    # Create database tables
    Base.metadata.create_all(bind=engine)
    
    # Initialize services
    ml_service = MLModelService()
    route_optimizer = RouteOptimizer()
    chatbot_service = ChatbotService()
    
    # Initialize ML models
    await ml_service.initialize_models()
    
    # Store in app state
    app.state.ml_service = ml_service
    app.state.route_optimizer = route_optimizer
    app.state.chatbot_service = chatbot_service
    
    print("AI Service started successfully!")
    print(f"- ML Models initialized: {ml_service.models_exist()}")
    print(f"- Route optimizer ready")
    print(f"- Chatbot service ready")
    yield
    
    # Shutdown
    print("AI Service shutting down...")

app = FastAPI(
    title="Food Delivery AI Service",
    description="AI-powered recommendations, route optimization, and chatbot for food delivery platform",
    version="1.0.0",
    lifespan=lifespan
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(recommendations.router, prefix="/api/recommendations", tags=["recommendations"])
app.include_router(routing.router, prefix="/api/routing", tags=["routing"])
app.include_router(chatbot.router, prefix="/api/chatbot", tags=["chatbot"])

@app.get("/")
async def root():
    return {
        "message": "Food Delivery AI Service", 
        "version": "1.0.0", 
        "status": "running",
        "services": {
            "recommendations": "active",
            "route_optimization": "active", 
            "chatbot": "active"
        }
    }

@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "ai-service",
        "models_loaded": ml_service is not None and ml_service.models_exist(),
        "database": "connected",
        "services": {
            "ml_models": ml_service is not None,
            "route_optimizer": route_optimizer is not None,
            "chatbot": chatbot_service is not None
        }
    }

@app.get("/api/status")
async def service_status():
    """Get detailed service status"""
    return {
        "timestamp": "2024-01-01T00:00:00Z",
        "services": {
            "recommendations": {
                "status": "active",
                "models_loaded": ml_service is not None and ml_service.models_exist(),
                "last_training": ml_service.last_update.isoformat() if ml_service and ml_service.last_update else None
            },
            "routing": {
                "status": "active",
                "google_maps_enabled": bool(settings.GOOGLE_MAPS_API_KEY)
            },
            "chatbot": {
                "status": "active", 
                "openai_enabled": bool(settings.OPENAI_API_KEY)
            }
        }
    }

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8085,
        reload=True
    )
