from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from pydantic import BaseModel
from typing import List, Dict, Optional
from app.services.ml_models import MLModelService
from app.core.database import get_db
from app.models.user_interaction import UserInteraction
import json

router = APIRouter()

class RecommendationRequest(BaseModel):
    user_id: int
    context: Optional[Dict] = None
    limit: int = 10

class MenuRecommendationRequest(BaseModel):
    user_id: int
    restaurant_id: int
    limit: int = 5

class InteractionLog(BaseModel):
    user_id: int
    restaurant_id: Optional[int] = None
    menu_item_id: Optional[int] = None
    interaction_type: str  # view, order, rating, search
    interaction_data: Optional[Dict] = None
    rating: Optional[float] = None

@router.get("/restaurants/{user_id}")
async def get_restaurant_recommendations(
    user_id: int,
    limit: int = 10,
    ml_service: MLModelService = Depends(lambda: None)
):
    """Get restaurant recommendations for a user"""
    try:
        if not ml_service:
            # Get from app state
            from fastapi import Request
            ml_service = Request.app.state.ml_service
        
        recommendations = await ml_service.get_restaurant_recommendations(user_id, limit)
        
        # Log recommendation
        await ml_service.log_recommendation(
            user_id=user_id,
            recommendation_type="restaurant",
            recommended_items=[r['restaurant_id'] for r in recommendations]
        )
        
        return {
            "recommendations": recommendations,
            "user_id": user_id,
            "total": len(recommendations)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting recommendations: {str(e)}")

@router.get("/menu/{user_id}/{restaurant_id}")
async def get_menu_recommendations(
    user_id: int,
    restaurant_id: int,
    limit: int = 5,
    ml_service: MLModelService = Depends(lambda: None)
):
    """Get menu item recommendations for a user at a specific restaurant"""
    try:
        if not ml_service:
            from fastapi import Request
            ml_service = Request.app.state.ml_service
        
        recommendations = await ml_service.get_menu_recommendations(user_id, restaurant_id, limit)
        
        # Log recommendation
        await ml_service.log_recommendation(
            user_id=user_id,
            recommendation_type="menu_item",
            recommended_items=[r['menu_item_id'] for r in recommendations],
            context_data={"restaurant_id": restaurant_id}
        )
        
        return {
            "recommendations": recommendations,
            "user_id": user_id,
            "restaurant_id": restaurant_id,
            "total": len(recommendations)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting menu recommendations: {str(e)}")

@router.post("/log-interaction")
async def log_user_interaction(
    interaction: InteractionLog,
    db = Depends(get_db)
):
    """Log user interaction for improving recommendations"""
    try:
        db_interaction = UserInteraction(
            user_id=interaction.user_id,
            restaurant_id=interaction.restaurant_id,
            menu_item_id=interaction.menu_item_id,
            interaction_type=interaction.interaction_type,
            interaction_data=json.dumps(interaction.interaction_data) if interaction.interaction_data else None,
            rating=interaction.rating
        )
        
        db.add(db_interaction)
        db.commit()
        db.refresh(db_interaction)
        
        return {
            "message": "Interaction logged successfully",
            "interaction_id": db_interaction.id
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error logging interaction: {str(e)}")

@router.get("/popular/restaurants")
async def get_popular_restaurants(
    limit: int = 10,
    cuisine: Optional[str] = None,
    db = Depends(get_db)
):
    """Get popular restaurants based on user interactions"""
    try:
        # Query popular restaurants from interactions
        query = db.query(UserInteraction.restaurant_id).filter(
            UserInteraction.interaction_type.in_(['order', 'view']),
            UserInteraction.restaurant_id.isnot(None)
        )
        
        if cuisine:
            # This would need restaurant data to filter by cuisine
            # For now, we'll return all popular restaurants
            pass
        
        # Count interactions per restaurant
        interactions = query.all()
        restaurant_counts = {}
        for interaction in interactions:
            restaurant_id = interaction.restaurant_id
            restaurant_counts[restaurant_id] = restaurant_counts.get(restaurant_id, 0) + 1
        
        # Sort by popularity
        popular_restaurants = sorted(restaurant_counts.items(), key=lambda x: x[1], reverse=True)[:limit]
        
        return {
            "popular_restaurants": [
                {
                    "restaurant_id": restaurant_id,
                    "interaction_count": count,
                    "popularity_score": count
                }
                for restaurant_id, count in popular_restaurants
            ],
            "total": len(popular_restaurants)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting popular restaurants: {str(e)}")

@router.get("/trending/cuisines")
async def get_trending_cuisines(
    limit: int = 10,
    time_window_hours: int = 24,
    db = Depends(get_db)
):
    """Get trending cuisines based on recent interactions"""
    try:
        from datetime import datetime, timedelta
        
        # Get interactions from the last time window
        cutoff_time = datetime.utcnow() - timedelta(hours=time_window_hours)
        
        interactions = db.query(UserInteraction).filter(
            UserInteraction.interaction_type.in_(['order', 'view', 'search']),
            UserInteraction.created_at >= cutoff_time,
            UserInteraction.interaction_data.isnot(None)
        ).all()
        
        # Count cuisine mentions
        cuisine_counts = {}
        for interaction in interactions:
            try:
                if interaction.interaction_data:
                    data = json.loads(interaction.interaction_data)
                    cuisine = data.get('cuisine')
                    if cuisine:
                        cuisine_counts[cuisine] = cuisine_counts.get(cuisine, 0) + 1
            except:
                continue
        
        # Sort by trending score
        trending_cuisines = sorted(cuisine_counts.items(), key=lambda x: x[1], reverse=True)[:limit]
        
        return {
            "trending_cuisines": [
                {
                    "cuisine": cuisine,
                    "interaction_count": count,
                    "trending_score": count
                }
                for cuisine, count in trending_cuisines
            ],
            "time_window_hours": time_window_hours,
            "total": len(trending_cuisines)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting trending cuisines: {str(e)}")

@router.post("/feedback")
async def submit_recommendation_feedback(
    user_id: int,
    recommendation_type: str,
    recommended_item_id: int,
    feedback: str,  # positive, negative, neutral
    db = Depends(get_db)
):
    """Submit feedback on recommendations to improve the model"""
    try:
        # Log feedback as interaction
        feedback_interaction = UserInteraction(
            user_id=user_id,
            restaurant_id=recommended_item_id if recommendation_type == 'restaurant' else None,
            menu_item_id=recommended_item_id if recommendation_type == 'menu_item' else None,
            interaction_type='feedback',
            interaction_data=json.dumps({
                'recommendation_type': recommendation_type,
                'feedback': feedback
            })
        )
        
        db.add(feedback_interaction)
        db.commit()
        
        return {
            "message": "Feedback submitted successfully",
            "user_id": user_id,
            "recommendation_type": recommendation_type,
            "feedback": feedback
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error submitting feedback: {str(e)}")

@router.get("/personalized/{user_id}")
async def get_personalized_recommendations(
    user_id: int,
    recommendation_type: str = "mixed",  # restaurant, menu_item, mixed
    limit: int = 10,
    ml_service: MLModelService = Depends(lambda: None),
    db = Depends(get_db)
):
    """Get personalized recommendations based on user's complete interaction history"""
    try:
        if not ml_service:
            from fastapi import Request
            ml_service = Request.app.state.ml_service
        
        # Get user's interaction history
        interactions = db.query(UserInteraction).filter(
            UserInteraction.user_id == user_id
        ).order_by(UserInteraction.created_at.desc()).limit(100).all()
        
        # Analyze user preferences
        preferences = {
            'cuisines': {},
            'restaurants': {},
            'price_ranges': {},
            'order_times': []
        }
        
        for interaction in interactions:
            if interaction.interaction_data:
                try:
                    data = json.loads(interaction.interaction_data)
                    
                    # Track cuisine preferences
                    cuisine = data.get('cuisine')
                    if cuisine:
                        preferences['cuisines'][cuisine] = preferences['cuisines'].get(cuisine, 0) + 1
                    
                    # Track restaurant preferences
                    if interaction.restaurant_id:
                        preferences['restaurants'][interaction.restaurant_id] = \
                            preferences['restaurants'].get(interaction.restaurant_id, 0) + 1
                    
                    # Track order timing
                    preferences['order_times'].append(interaction.created_at.hour)
                    
                except:
                    continue
        
        # Get recommendations based on type
        recommendations = []
        
        if recommendation_type in ['restaurant', 'mixed']:
            restaurant_recs = await ml_service.get_restaurant_recommendations(user_id, limit // 2 if recommendation_type == 'mixed' else limit)
            recommendations.extend([{**rec, 'type': 'restaurant'} for rec in restaurant_recs])
        
        # Add context-based recommendations
        current_hour = datetime.now().hour
        context_recommendations = []
        
        # Time-based recommendations
        if 11 <= current_hour <= 14:
            context_recommendations.append({
                'type': 'context',
                'message': 'Perfect time for lunch! Here are some quick options.',
                'context': 'lunch_time'
            })
        elif 18 <= current_hour <= 21:
            context_recommendations.append({
                'type': 'context',
                'message': 'Dinner time! How about trying something new?',
                'context': 'dinner_time'
            })
        
        return {
            "recommendations": recommendations,
            "context_recommendations": context_recommendations,
            "user_preferences": {
                "top_cuisines": dict(sorted(preferences['cuisines'].items(), key=lambda x: x[1], reverse=True)[:3]),
                "favorite_restaurants": list(preferences['restaurants'].keys())[:3],
                "common_order_times": preferences['order_times'][-10:] if preferences['order_times'] else []
            },
            "user_id": user_id,
            "total": len(recommendations)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting personalized recommendations: {str(e)}")
