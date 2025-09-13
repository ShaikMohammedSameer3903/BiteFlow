import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import pickle
import asyncio
import aiofiles
import json
from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta
import requests
from app.core.config import settings
from app.core.database import get_db
from app.models.user_interaction import UserInteraction, RecommendationLog

class MLModelService:
    def __init__(self):
        self.recommendation_model = None
        self.delivery_time_model = None
        self.cuisine_vectorizer = None
        self.restaurant_features = None
        self.menu_features = None
        self.scaler = StandardScaler()
        self.last_update = None

    async def initialize_models(self):
        """Initialize ML models on startup"""
        try:
            await self.load_models()
            if not self.models_exist():
                await self.train_initial_models()
            self.last_update = datetime.utcnow()
        except Exception as e:
            print(f"Error initializing models: {e}")
            await self.create_default_models()

    def models_exist(self) -> bool:
        """Check if trained models exist"""
        return (self.recommendation_model is not None and 
                self.delivery_time_model is not None and
                self.cuisine_vectorizer is not None)

    async def load_models(self):
        """Load pre-trained models from disk"""
        try:
            async with aiofiles.open('models/recommendation_model.pkl', 'rb') as f:
                content = await f.read()
                self.recommendation_model = pickle.loads(content)
            
            async with aiofiles.open('models/delivery_time_model.pkl', 'rb') as f:
                content = await f.read()
                self.delivery_time_model = pickle.loads(content)
            
            async with aiofiles.open('models/cuisine_vectorizer.pkl', 'rb') as f:
                content = await f.read()
                self.cuisine_vectorizer = pickle.loads(content)
                
            async with aiofiles.open('models/restaurant_features.json', 'r') as f:
                content = await f.read()
                self.restaurant_features = json.loads(content)
                
            async with aiofiles.open('models/menu_features.json', 'r') as f:
                content = await f.read()
                self.menu_features = json.loads(content)
                
        except FileNotFoundError:
            print("No pre-trained models found, will train new ones")

    async def save_models(self):
        """Save trained models to disk"""
        import os
        os.makedirs('models', exist_ok=True)
        
        async with aiofiles.open('models/recommendation_model.pkl', 'wb') as f:
            await f.write(pickle.dumps(self.recommendation_model))
        
        async with aiofiles.open('models/delivery_time_model.pkl', 'wb') as f:
            await f.write(pickle.dumps(self.delivery_time_model))
        
        async with aiofiles.open('models/cuisine_vectorizer.pkl', 'wb') as f:
            await f.write(pickle.dumps(self.cuisine_vectorizer))
            
        async with aiofiles.open('models/restaurant_features.json', 'w') as f:
            await f.write(json.dumps(self.restaurant_features))
            
        async with aiofiles.open('models/menu_features.json', 'w') as f:
            await f.write(json.dumps(self.menu_features))

    async def create_default_models(self):
        """Create default models when no training data is available"""
        # Simple default recommendation model
        self.recommendation_model = {
            'type': 'default',
            'popular_restaurants': [],
            'popular_cuisines': ['Italian', 'Chinese', 'Mexican', 'Indian', 'American']
        }
        
        # Simple default delivery time model
        self.delivery_time_model = RandomForestRegressor(n_estimators=10, random_state=42)
        # Train with dummy data
        X_dummy = np.random.rand(100, 5)  # distance, restaurant_load, time_of_day, weather, traffic
        y_dummy = np.random.randint(15, 60, 100)  # delivery times 15-60 minutes
        self.delivery_time_model.fit(X_dummy, y_dummy)
        
        # Default vectorizer
        self.cuisine_vectorizer = TfidfVectorizer(max_features=100)
        dummy_cuisines = ['Italian pizza pasta', 'Chinese noodles rice', 'Mexican tacos burrito']
        self.cuisine_vectorizer.fit(dummy_cuisines)
        
        self.restaurant_features = {}
        self.menu_features = {}

    async def train_initial_models(self):
        """Train models with initial data from the platform"""
        try:
            # Fetch data from other services
            restaurants_data = await self.fetch_restaurants_data()
            orders_data = await self.fetch_orders_data()
            
            if len(restaurants_data) < settings.MIN_TRAINING_DATA:
                await self.create_default_models()
                return
            
            # Train recommendation model
            await self.train_recommendation_model(restaurants_data, orders_data)
            
            # Train delivery time prediction model
            await self.train_delivery_time_model(orders_data)
            
            # Save models
            await self.save_models()
            
        except Exception as e:
            print(f"Error training initial models: {e}")
            await self.create_default_models()

    async def fetch_restaurants_data(self) -> List[Dict]:
        """Fetch restaurant data from restaurant service"""
        try:
            response = requests.get(f"{settings.RESTAURANT_SERVICE_URL}/api/restaurants")
            if response.status_code == 200:
                return response.json()
            return []
        except Exception as e:
            print(f"Error fetching restaurants data: {e}")
            return []

    async def fetch_orders_data(self) -> List[Dict]:
        """Fetch orders data from restaurant service"""
        try:
            response = requests.get(f"{settings.RESTAURANT_SERVICE_URL}/api/orders")
            if response.status_code == 200:
                return response.json()
            return []
        except Exception as e:
            print(f"Error fetching orders data: {e}")
            return []

    async def train_recommendation_model(self, restaurants_data: List[Dict], orders_data: List[Dict]):
        """Train collaborative filtering recommendation model"""
        try:
            # Create restaurant features matrix
            restaurant_features = {}
            cuisine_texts = []
            
            for restaurant in restaurants_data:
                features = {
                    'id': restaurant['id'],
                    'cuisine': restaurant.get('cuisine', ''),
                    'rating': restaurant.get('rating', 0),
                    'price_range': len(restaurant.get('priceRange', '$')),
                    'delivery_time': restaurant.get('deliveryTime', 30)
                }
                restaurant_features[restaurant['id']] = features
                cuisine_texts.append(f"{restaurant.get('cuisine', '')} {restaurant.get('name', '')}")
            
            # Train cuisine vectorizer
            if cuisine_texts:
                self.cuisine_vectorizer = TfidfVectorizer(max_features=100, stop_words='english')
                self.cuisine_vectorizer.fit(cuisine_texts)
            
            self.restaurant_features = restaurant_features
            
            # Simple popularity-based recommendation for now
            restaurant_popularity = {}
            for order in orders_data:
                restaurant_id = order.get('restaurantId')
                if restaurant_id:
                    restaurant_popularity[restaurant_id] = restaurant_popularity.get(restaurant_id, 0) + 1
            
            popular_restaurants = sorted(restaurant_popularity.items(), key=lambda x: x[1], reverse=True)
            
            self.recommendation_model = {
                'type': 'popularity',
                'popular_restaurants': [r[0] for r in popular_restaurants[:20]],
                'restaurant_popularity': restaurant_popularity
            }
            
        except Exception as e:
            print(f"Error training recommendation model: {e}")

    async def train_delivery_time_model(self, orders_data: List[Dict]):
        """Train delivery time prediction model"""
        try:
            if len(orders_data) < 10:
                await self.create_default_models()
                return
            
            # Prepare training data
            X = []
            y = []
            
            for order in orders_data:
                if order.get('status') == 'DELIVERED' and order.get('deliveryTime'):
                    # Features: distance (dummy), restaurant_load (dummy), hour_of_day, day_of_week, order_size
                    order_time = datetime.fromisoformat(order['orderTime'].replace('Z', '+00:00'))
                    features = [
                        5.0,  # dummy distance
                        np.random.randint(1, 10),  # dummy restaurant load
                        order_time.hour,
                        order_time.weekday(),
                        len(order.get('items', []))
                    ]
                    X.append(features)
                    y.append(order.get('deliveryTime', 30))
            
            if len(X) >= 10:
                X = np.array(X)
                y = np.array(y)
                
                # Scale features
                X_scaled = self.scaler.fit_transform(X)
                
                # Train model
                self.delivery_time_model = RandomForestRegressor(n_estimators=50, random_state=42)
                self.delivery_time_model.fit(X_scaled, y)
            else:
                await self.create_default_models()
                
        except Exception as e:
            print(f"Error training delivery time model: {e}")

    async def get_restaurant_recommendations(self, user_id: int, limit: int = 10) -> List[Dict]:
        """Get restaurant recommendations for a user"""
        try:
            if not self.recommendation_model:
                return []
            
            # Get user interaction history
            db = next(get_db())
            user_interactions = db.query(UserInteraction).filter(
                UserInteraction.user_id == user_id,
                UserInteraction.interaction_type.in_(['order', 'view', 'rating'])
            ).limit(100).all()
            
            if self.recommendation_model['type'] == 'popularity':
                # Return popular restaurants, excluding ones user has ordered from recently
                recent_restaurant_ids = set()
                for interaction in user_interactions[-10:]:  # Last 10 interactions
                    if interaction.restaurant_id:
                        recent_restaurant_ids.add(interaction.restaurant_id)
                
                recommendations = []
                for restaurant_id in self.recommendation_model['popular_restaurants']:
                    if restaurant_id not in recent_restaurant_ids and len(recommendations) < limit:
                        if restaurant_id in self.restaurant_features:
                            recommendations.append({
                                'restaurant_id': restaurant_id,
                                'score': self.recommendation_model['restaurant_popularity'].get(restaurant_id, 0),
                                'reason': 'Popular restaurant'
                            })
                
                return recommendations
            
            return []
            
        except Exception as e:
            print(f"Error getting restaurant recommendations: {e}")
            return []

    async def get_menu_recommendations(self, user_id: int, restaurant_id: int, limit: int = 5) -> List[Dict]:
        """Get menu item recommendations for a user at a specific restaurant"""
        try:
            # Simple popularity-based recommendations for menu items
            db = next(get_db())
            popular_items = db.query(UserInteraction).filter(
                UserInteraction.restaurant_id == restaurant_id,
                UserInteraction.interaction_type == 'order',
                UserInteraction.menu_item_id.isnot(None)
            ).all()
            
            item_popularity = {}
            for interaction in popular_items:
                item_id = interaction.menu_item_id
                item_popularity[item_id] = item_popularity.get(item_id, 0) + 1
            
            recommendations = []
            for item_id, count in sorted(item_popularity.items(), key=lambda x: x[1], reverse=True)[:limit]:
                recommendations.append({
                    'menu_item_id': item_id,
                    'score': count,
                    'reason': 'Popular item'
                })
            
            return recommendations
            
        except Exception as e:
            print(f"Error getting menu recommendations: {e}")
            return []

    async def predict_delivery_time(self, restaurant_id: int, delivery_address: str, order_size: int) -> int:
        """Predict delivery time for an order"""
        try:
            if not self.delivery_time_model:
                return 30  # Default 30 minutes
            
            # Prepare features
            current_time = datetime.utcnow()
            features = [
                5.0,  # dummy distance - in production, calculate from addresses
                np.random.randint(1, 10),  # dummy restaurant load
                current_time.hour,
                current_time.weekday(),
                order_size
            ]
            
            # Scale features
            features_scaled = self.scaler.transform([features])
            
            # Predict
            predicted_time = self.delivery_time_model.predict(features_scaled)[0]
            
            # Ensure reasonable bounds
            return max(15, min(int(predicted_time), 90))
            
        except Exception as e:
            print(f"Error predicting delivery time: {e}")
            return 30

    async def log_recommendation(self, user_id: int, recommendation_type: str, 
                               recommended_items: List[int], context_data: Dict = None):
        """Log recommendation for tracking performance"""
        try:
            db = next(get_db())
            log_entry = RecommendationLog(
                user_id=user_id,
                recommendation_type=recommendation_type,
                recommended_items=json.dumps(recommended_items),
                context_data=json.dumps(context_data) if context_data else None
            )
            db.add(log_entry)
            db.commit()
        except Exception as e:
            print(f"Error logging recommendation: {e}")

    async def should_retrain_models(self) -> bool:
        """Check if models should be retrained"""
        if not self.last_update:
            return True
        
        time_since_update = datetime.utcnow() - self.last_update
        return time_since_update.total_seconds() > settings.MODEL_UPDATE_INTERVAL
