from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Optional
from app.services.chatbot import ChatbotService
from app.core.security import get_current_user

router = APIRouter()

class ChatMessage(BaseModel):
    message: str
    session_id: Optional[str] = None

class ChatResponse(BaseModel):
    response: str
    session_id: str
    message_type: str
    metadata: Optional[Dict] = None

class ChatSessionRequest(BaseModel):
    user_id: int

@router.post("/start-session")
async def start_chat_session(
    request: ChatSessionRequest,
    chatbot_service: ChatbotService = Depends(ChatbotService)
):
    """Start a new chat session"""
    try:
        session_id = await chatbot_service.create_chat_session(request.user_id)
        
        return {
            "session_id": session_id,
            "user_id": request.user_id,
            "message": "Chat session started successfully",
            "welcome_message": "Hi! I'm your food delivery assistant. I can help you find restaurants, track orders, get recommendations, and answer any food-related questions. How can I help you today?"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error starting chat session: {str(e)}")

@router.post("/chat")
async def chat_with_bot(
    message: ChatMessage,
    user_id: int,
    chatbot_service: ChatbotService = Depends(ChatbotService)
):
    """Send a message to the chatbot"""
    try:
        # Create session if not provided
        session_id = message.session_id
        if not session_id:
            session_id = await chatbot_service.create_chat_session(user_id)
        
        # Generate response
        response = await chatbot_service.generate_response(session_id, message.message, user_id)
        
        return {
            "response": response['content'],
            "session_id": session_id,
            "message_type": response.get('type', 'general'),
            "metadata": response.get('metadata'),
            "user_id": user_id
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error processing chat message: {str(e)}")

@router.get("/history/{session_id}")
async def get_chat_history(
    session_id: str,
    limit: int = 20,
    chatbot_service: ChatbotService = Depends(ChatbotService)
):
    """Get chat history for a session"""
    try:
        history = await chatbot_service.get_chat_history(session_id, limit)
        
        return {
            "session_id": session_id,
            "messages": history,
            "total": len(history)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting chat history: {str(e)}")

@router.get("/sessions/{user_id}")
async def get_user_chat_sessions(
    user_id: int,
    limit: int = 10,
    chatbot_service: ChatbotService = Depends(ChatbotService)
):
    """Get chat sessions for a user"""
    try:
        from app.core.database import get_db
        from app.models.user_interaction import ChatSession
        
        db = next(get_db())
        sessions = db.query(ChatSession).filter(
            ChatSession.user_id == user_id
        ).order_by(ChatSession.created_at.desc()).limit(limit).all()
        
        session_list = []
        for session in sessions:
            # Get last message for preview
            last_messages = await chatbot_service.get_chat_history(session.session_id, 1)
            last_message = last_messages[0] if last_messages else None
            
            session_list.append({
                "session_id": session.session_id,
                "created_at": session.created_at.isoformat(),
                "is_active": session.is_active,
                "last_message": last_message['content'] if last_message else None,
                "last_message_time": last_message['timestamp'] if last_message else None
            })
        
        return {
            "user_id": user_id,
            "sessions": session_list,
            "total": len(session_list)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting user sessions: {str(e)}")

@router.post("/quick-help")
async def get_quick_help(
    category: str,  # restaurants, orders, account, general
    user_id: int,
    chatbot_service: ChatbotService = Depends(ChatbotService)
):
    """Get quick help responses for common categories"""
    try:
        quick_responses = {
            "restaurants": {
                "message": "I can help you find great restaurants! Here are some things I can do:",
                "options": [
                    "Search for restaurants by cuisine or name",
                    "Get personalized restaurant recommendations",
                    "Show popular restaurants in your area",
                    "Find restaurants with specific dietary options",
                    "Compare restaurant ratings and reviews"
                ]
            },
            "orders": {
                "message": "Need help with your orders? I can assist with:",
                "options": [
                    "Track your current orders",
                    "Check order history",
                    "Help with order modifications",
                    "Explain delivery times",
                    "Assist with order issues"
                ]
            },
            "account": {
                "message": "For account-related help, I can provide guidance on:",
                "options": [
                    "Account settings and preferences",
                    "Payment methods and billing",
                    "Delivery addresses",
                    "Notification settings",
                    "Privacy and security"
                ]
            },
            "general": {
                "message": "I'm here to help with all your food delivery needs! I can:",
                "options": [
                    "Find restaurants and recommend dishes",
                    "Help track and manage orders",
                    "Answer questions about food and cuisines",
                    "Provide customer support guidance",
                    "Assist with app navigation"
                ]
            }
        }
        
        response = quick_responses.get(category, quick_responses["general"])
        
        return {
            "category": category,
            "user_id": user_id,
            "help_message": response["message"],
            "help_options": response["options"],
            "suggested_questions": [
                f"Can you help me with {category}?",
                "What can you do?",
                "I need assistance"
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting quick help: {str(e)}")

@router.post("/suggest-responses")
async def get_suggested_responses(
    last_message: str,
    context: str,  # restaurant_search, order_tracking, recommendation, etc.
    chatbot_service: ChatbotService = Depends(ChatbotService)
):
    """Get suggested response options for the user"""
    try:
        suggestions = {
            "restaurant_search": [
                "Show me the menu",
                "What are the reviews like?",
                "How long is the delivery time?",
                "Do they have vegetarian options?",
                "Find similar restaurants"
            ],
            "order_tracking": [
                "When will my order arrive?",
                "Can I modify my order?",
                "Contact the restaurant",
                "Contact the delivery driver",
                "Cancel my order"
            ],
            "recommendation": [
                "Tell me more about this restaurant",
                "Show me different cuisine options",
                "Find something cheaper",
                "Find something nearby",
                "I'll take this recommendation"
            ],
            "menu_inquiry": [
                "What's popular here?",
                "Do you have vegetarian options?",
                "What's the spiciest dish?",
                "Show me desserts",
                "Add to cart"
            ],
            "general": [
                "Find restaurants near me",
                "Track my order",
                "Get recommendations",
                "Help with my account",
                "Contact support"
            ]
        }
        
        context_suggestions = suggestions.get(context, suggestions["general"])
        
        return {
            "last_message": last_message,
            "context": context,
            "suggested_responses": context_suggestions,
            "quick_actions": [
                "Find restaurants",
                "Track order",
                "Get help"
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting suggested responses: {str(e)}")

@router.post("/feedback")
async def submit_chat_feedback(
    session_id: str,
    rating: int,  # 1-5 stars
    feedback_text: Optional[str] = None,
    chatbot_service: ChatbotService = Depends(ChatbotService)
):
    """Submit feedback for a chat session"""
    try:
        # Save feedback as a special message
        await chatbot_service.save_message(
            session_id=session_id,
            message_type="feedback",
            content=f"User rating: {rating}/5" + (f" - {feedback_text}" if feedback_text else ""),
            metadata={
                "rating": rating,
                "feedback_text": feedback_text,
                "feedback_type": "session_rating"
            }
        )
        
        return {
            "session_id": session_id,
            "rating": rating,
            "feedback_text": feedback_text,
            "message": "Thank you for your feedback! It helps us improve our service."
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error submitting feedback: {str(e)}")

@router.get("/analytics/popular-queries")
async def get_popular_queries(
    time_window_hours: int = 24,
    limit: int = 10
):
    """Get popular chat queries for analytics"""
    try:
        from app.core.database import get_db
        from app.models.user_interaction import ChatMessage
        from datetime import datetime, timedelta
        import json
        from collections import Counter
        
        db = next(get_db())
        cutoff_time = datetime.utcnow() - timedelta(hours=time_window_hours)
        
        messages = db.query(ChatMessage).filter(
            ChatMessage.message_type == 'user',
            ChatMessage.created_at >= cutoff_time
        ).all()
        
        # Extract keywords and intents
        query_keywords = []
        for message in messages:
            # Simple keyword extraction
            words = message.content.lower().split()
            keywords = [word for word in words if len(word) > 3 and word.isalpha()]
            query_keywords.extend(keywords)
        
        # Count popular keywords
        keyword_counts = Counter(query_keywords)
        popular_keywords = keyword_counts.most_common(limit)
        
        return {
            "time_window_hours": time_window_hours,
            "total_messages": len(messages),
            "popular_keywords": [
                {"keyword": keyword, "count": count}
                for keyword, count in popular_keywords
            ],
            "analysis_time": datetime.utcnow().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error getting popular queries: {str(e)}")
