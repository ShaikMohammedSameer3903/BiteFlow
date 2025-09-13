import openai
import json
import uuid
from typing import Dict, List, Optional, Any
from datetime import datetime
import requests
from app.core.config import settings
from app.core.database import get_db
from app.models.user_interaction import ChatSession, ChatMessage

class ChatbotService:
    def __init__(self):
        if settings.OPENAI_API_KEY:
            openai.api_key = settings.OPENAI_API_KEY
        self.system_prompt = self._get_system_prompt()

    def _get_system_prompt(self) -> str:
        return """You are a helpful AI assistant for a food delivery platform. You can help users with:

1. Finding restaurants and menu items
2. Placing orders and tracking deliveries
3. Answering questions about food, cuisines, and dietary restrictions
4. Providing recommendations based on preferences
5. Helping with account and order issues

Guidelines:
- Be friendly, helpful, and concise
- Always prioritize food safety and accurate information
- If you need to perform actions (like placing orders), ask for confirmation
- For complex issues, suggest contacting customer support
- Keep responses focused on food delivery and related topics

Current capabilities:
- Restaurant search and recommendations
- Menu browsing and item details
- Order status checking
- General food and cuisine information
- Basic troubleshooting

If asked about topics outside food delivery, politely redirect to relevant topics."""

    async def create_chat_session(self, user_id: int) -> str:
        """Create a new chat session"""
        try:
            session_id = str(uuid.uuid4())
            db = next(get_db())
            
            session = ChatSession(
                user_id=user_id,
                session_id=session_id,
                is_active=True
            )
            db.add(session)
            db.commit()
            
            return session_id
        except Exception as e:
            print(f"Error creating chat session: {e}")
            return str(uuid.uuid4())

    async def get_chat_history(self, session_id: str, limit: int = 20) -> List[Dict]:
        """Get chat history for a session"""
        try:
            db = next(get_db())
            messages = db.query(ChatMessage).filter(
                ChatMessage.session_id == session_id
            ).order_by(ChatMessage.created_at.desc()).limit(limit).all()
            
            return [
                {
                    'type': msg.message_type,
                    'content': msg.content,
                    'timestamp': msg.created_at.isoformat(),
                    'metadata': json.loads(msg.metadata) if msg.metadata else None
                }
                for msg in reversed(messages)
            ]
        except Exception as e:
            print(f"Error getting chat history: {e}")
            return []

    async def save_message(self, session_id: str, message_type: str, content: str, metadata: Dict = None):
        """Save a message to the chat history"""
        try:
            db = next(get_db())
            message = ChatMessage(
                session_id=session_id,
                message_type=message_type,
                content=content,
                metadata=json.dumps(metadata) if metadata else None
            )
            db.add(message)
            db.commit()
        except Exception as e:
            print(f"Error saving message: {e}")

    async def get_user_context(self, user_id: int) -> Dict:
        """Get user context for personalized responses"""
        try:
            # Get user's recent orders
            response = requests.get(f"{settings.RESTAURANT_SERVICE_URL}/api/orders", 
                                  params={'customerId': user_id, 'limit': 5})
            recent_orders = response.json() if response.status_code == 200 else []
            
            # Get user's favorite restaurants (from order history)
            favorite_restaurants = {}
            for order in recent_orders:
                restaurant_id = order.get('restaurantId')
                if restaurant_id:
                    favorite_restaurants[restaurant_id] = favorite_restaurants.get(restaurant_id, 0) + 1
            
            # Get user preferences from interactions
            db = next(get_db())
            from app.models.user_interaction import UserInteraction
            interactions = db.query(UserInteraction).filter(
                UserInteraction.user_id == user_id
            ).limit(50).all()
            
            preferred_cuisines = {}
            for interaction in interactions:
                if interaction.interaction_data:
                    try:
                        data = json.loads(interaction.interaction_data)
                        cuisine = data.get('cuisine')
                        if cuisine:
                            preferred_cuisines[cuisine] = preferred_cuisines.get(cuisine, 0) + 1
                    except:
                        pass
            
            return {
                'recent_orders': recent_orders[:3],  # Last 3 orders
                'favorite_restaurants': dict(sorted(favorite_restaurants.items(), 
                                                  key=lambda x: x[1], reverse=True)[:3]),
                'preferred_cuisines': dict(sorted(preferred_cuisines.items(), 
                                                key=lambda x: x[1], reverse=True)[:3]),
                'total_orders': len(recent_orders)
            }
        except Exception as e:
            print(f"Error getting user context: {e}")
            return {}

    async def generate_response(self, session_id: str, user_message: str, user_id: int) -> Dict:
        """Generate AI response to user message"""
        try:
            # Save user message
            await self.save_message(session_id, 'user', user_message)
            
            # Get user context
            user_context = await self.get_user_context(user_id)
            
            # Get recent chat history
            chat_history = await self.get_chat_history(session_id, limit=10)
            
            # Check for specific intents
            intent = await self.detect_intent(user_message)
            
            if intent['type'] == 'restaurant_search':
                response = await self.handle_restaurant_search(intent['parameters'], user_context)
            elif intent['type'] == 'order_status':
                response = await self.handle_order_status(intent['parameters'], user_id)
            elif intent['type'] == 'recommendation':
                response = await self.handle_recommendation_request(user_context)
            elif intent['type'] == 'menu_inquiry':
                response = await self.handle_menu_inquiry(intent['parameters'])
            else:
                # Use OpenAI for general conversation
                response = await self.generate_openai_response(user_message, chat_history, user_context)
            
            # Save assistant response
            await self.save_message(session_id, 'assistant', response['content'], response.get('metadata'))
            
            return response
            
        except Exception as e:
            print(f"Error generating response: {e}")
            fallback_response = {
                'content': "I'm sorry, I'm having trouble processing your request right now. Please try again or contact customer support for assistance.",
                'type': 'error',
                'metadata': {'error': str(e)}
            }
            await self.save_message(session_id, 'assistant', fallback_response['content'], fallback_response.get('metadata'))
            return fallback_response

    async def detect_intent(self, message: str) -> Dict:
        """Detect user intent from message"""
        message_lower = message.lower()
        
        # Restaurant search intent
        if any(word in message_lower for word in ['restaurant', 'food', 'cuisine', 'find', 'search', 'looking for']):
            return {
                'type': 'restaurant_search',
                'parameters': {'query': message}
            }
        
        # Order status intent
        if any(word in message_lower for word in ['order', 'delivery', 'status', 'track', 'where is']):
            return {
                'type': 'order_status',
                'parameters': {'query': message}
            }
        
        # Recommendation intent
        if any(word in message_lower for word in ['recommend', 'suggest', 'what should', 'popular', 'best']):
            return {
                'type': 'recommendation',
                'parameters': {'query': message}
            }
        
        # Menu inquiry intent
        if any(word in message_lower for word in ['menu', 'items', 'dishes', 'what do they have']):
            return {
                'type': 'menu_inquiry',
                'parameters': {'query': message}
            }
        
        return {
            'type': 'general',
            'parameters': {'query': message}
        }

    async def handle_restaurant_search(self, parameters: Dict, user_context: Dict) -> Dict:
        """Handle restaurant search requests"""
        try:
            query = parameters.get('query', '')
            
            # Search restaurants
            response = requests.get(f"{settings.RESTAURANT_SERVICE_URL}/api/restaurants", 
                                  params={'search': query, 'limit': 5})
            
            if response.status_code == 200:
                restaurants = response.json()
                
                if restaurants:
                    content = "I found these restaurants for you:\n\n"
                    for i, restaurant in enumerate(restaurants[:3], 1):
                        content += f"{i}. **{restaurant['name']}**\n"
                        content += f"   - Cuisine: {restaurant.get('cuisine', 'N/A')}\n"
                        content += f"   - Rating: {restaurant.get('rating', 'N/A')}⭐\n"
                        content += f"   - Delivery: {restaurant.get('deliveryTime', 'N/A')} min\n\n"
                    
                    content += "Would you like more details about any of these restaurants or their menus?"
                    
                    return {
                        'content': content,
                        'type': 'restaurant_search',
                        'metadata': {
                            'restaurants': restaurants[:3],
                            'search_query': query
                        }
                    }
                else:
                    return {
                        'content': f"I couldn't find any restaurants matching '{query}'. Try searching for a different cuisine or restaurant name.",
                        'type': 'restaurant_search',
                        'metadata': {'search_query': query}
                    }
            else:
                return {
                    'content': "I'm having trouble searching for restaurants right now. Please try again in a moment.",
                    'type': 'error'
                }
                
        except Exception as e:
            print(f"Error handling restaurant search: {e}")
            return {
                'content': "I encountered an error while searching for restaurants. Please try again.",
                'type': 'error'
            }

    async def handle_order_status(self, parameters: Dict, user_id: int) -> Dict:
        """Handle order status requests"""
        try:
            # Get user's recent orders
            response = requests.get(f"{settings.RESTAURANT_SERVICE_URL}/api/orders", 
                                  params={'customerId': user_id, 'limit': 3})
            
            if response.status_code == 200:
                orders = response.json()
                
                if orders:
                    active_orders = [o for o in orders if o['status'] not in ['DELIVERED', 'CANCELLED']]
                    
                    if active_orders:
                        content = "Here are your active orders:\n\n"
                        for order in active_orders:
                            content += f"**Order #{order['id']}**\n"
                            content += f"- Restaurant: {order.get('restaurant', {}).get('name', 'N/A')}\n"
                            content += f"- Status: {order['status'].replace('_', ' ')}\n"
                            content += f"- Total: ${order.get('totalAmount', 0):.2f}\n\n"
                    else:
                        content = "You don't have any active orders right now. Your recent orders:\n\n"
                        for order in orders[:2]:
                            content += f"**Order #{order['id']}** - {order['status'].replace('_', ' ')}\n"
                            content += f"- Restaurant: {order.get('restaurant', {}).get('name', 'N/A')}\n"
                            content += f"- Total: ${order.get('totalAmount', 0):.2f}\n\n"
                    
                    return {
                        'content': content,
                        'type': 'order_status',
                        'metadata': {'orders': orders}
                    }
                else:
                    return {
                        'content': "You haven't placed any orders yet. Would you like me to help you find some great restaurants?",
                        'type': 'order_status'
                    }
            else:
                return {
                    'content': "I'm having trouble accessing your order information. Please try again or check your order history in the app.",
                    'type': 'error'
                }
                
        except Exception as e:
            print(f"Error handling order status: {e}")
            return {
                'content': "I encountered an error while checking your orders. Please try again.",
                'type': 'error'
            }

    async def handle_recommendation_request(self, user_context: Dict) -> Dict:
        """Handle recommendation requests"""
        try:
            content = "Based on your preferences, here are my recommendations:\n\n"
            
            # Use user's favorite cuisines
            preferred_cuisines = user_context.get('preferred_cuisines', {})
            if preferred_cuisines:
                top_cuisine = list(preferred_cuisines.keys())[0]
                content += f"Since you enjoy {top_cuisine} food, I'd recommend exploring more {top_cuisine} restaurants.\n\n"
            
            # Get popular restaurants
            response = requests.get(f"{settings.RESTAURANT_SERVICE_URL}/api/restaurants", 
                                  params={'limit': 3, 'sortBy': 'rating'})
            
            if response.status_code == 200:
                restaurants = response.json()
                content += "**Top-rated restaurants:**\n"
                for i, restaurant in enumerate(restaurants[:3], 1):
                    content += f"{i}. {restaurant['name']} ({restaurant.get('cuisine', 'N/A')}) - {restaurant.get('rating', 'N/A')}⭐\n"
                
                content += "\nWould you like to see the menu for any of these restaurants?"
            
            return {
                'content': content,
                'type': 'recommendation',
                'metadata': {
                    'user_preferences': user_context,
                    'recommended_restaurants': restaurants[:3] if 'restaurants' in locals() else []
                }
            }
            
        except Exception as e:
            print(f"Error handling recommendation: {e}")
            return {
                'content': "I'd be happy to recommend some great restaurants! What type of cuisine are you in the mood for?",
                'type': 'recommendation'
            }

    async def handle_menu_inquiry(self, parameters: Dict) -> Dict:
        """Handle menu inquiry requests"""
        return {
            'content': "I'd be happy to help you with menu information! Please specify which restaurant you're interested in, and I can show you their menu items.",
            'type': 'menu_inquiry'
        }

    async def generate_openai_response(self, user_message: str, chat_history: List[Dict], user_context: Dict) -> Dict:
        """Generate response using OpenAI"""
        try:
            if not settings.OPENAI_API_KEY:
                return {
                    'content': "I'm here to help with your food delivery needs! You can ask me about restaurants, orders, recommendations, or any food-related questions.",
                    'type': 'general'
                }
            
            # Prepare conversation history
            messages = [{"role": "system", "content": self.system_prompt}]
            
            # Add user context
            if user_context:
                context_msg = f"User context: {json.dumps(user_context, indent=2)}"
                messages.append({"role": "system", "content": context_msg})
            
            # Add chat history
            for msg in chat_history[-5:]:  # Last 5 messages
                role = "user" if msg['type'] == 'user' else "assistant"
                messages.append({"role": role, "content": msg['content']})
            
            # Add current message
            messages.append({"role": "user", "content": user_message})
            
            # Generate response
            response = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=messages,
                max_tokens=300,
                temperature=0.7
            )
            
            content = response.choices[0].message.content.strip()
            
            return {
                'content': content,
                'type': 'general',
                'metadata': {'model': 'gpt-3.5-turbo'}
            }
            
        except Exception as e:
            print(f"Error generating OpenAI response: {e}")
            return {
                'content': "I'm here to help with your food delivery needs! You can ask me about restaurants, menu items, orders, or get recommendations.",
                'type': 'general'
            }
