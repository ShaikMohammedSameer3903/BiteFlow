import os
from typing import Optional
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # Database
    DATABASE_URL: str = os.getenv("DATABASE_URL", "mysql://root:shaik@localhost:3306/food_delivery_ai")
    
    # Redis
    REDIS_URL: str = os.getenv("REDIS_URL", "redis://localhost:6379")
    
    # JWT
    JWT_SECRET_KEY: str = os.getenv("JWT_SECRET", "your-secret-key")
    JWT_ALGORITHM: str = "HS256"
    
    # OpenAI
    OPENAI_API_KEY: Optional[str] = os.getenv("OPENAI_API_KEY")
    
    # Google Maps
    GOOGLE_MAPS_API_KEY: Optional[str] = os.getenv("GOOGLE_MAPS_API_KEY")
    
    # External Services
    USER_SERVICE_URL: str = os.getenv("USER_SERVICE_URL", "http://localhost:8081")
    RESTAURANT_SERVICE_URL: str = os.getenv("RESTAURANT_SERVICE_URL", "http://localhost:8082")
    DELIVERY_SERVICE_URL: str = os.getenv("DELIVERY_SERVICE_URL", "http://localhost:8083")
    PAYMENT_SERVICE_URL: str = os.getenv("PAYMENT_SERVICE_URL", "http://localhost:8084")
    
    # ML Models
    MODEL_UPDATE_INTERVAL: int = 3600  # 1 hour in seconds
    MIN_TRAINING_DATA: int = 100
    
    class Config:
        env_file = ".env"

settings = Settings()
