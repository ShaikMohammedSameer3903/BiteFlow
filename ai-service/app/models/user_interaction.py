from sqlalchemy import Column, Integer, String, DateTime, Text, Float, Boolean
from sqlalchemy.sql import func
from app.core.database import Base

class UserInteraction(Base):
    __tablename__ = "user_interactions"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False, index=True)
    restaurant_id = Column(Integer, nullable=True, index=True)
    menu_item_id = Column(Integer, nullable=True, index=True)
    interaction_type = Column(String(50), nullable=False)  # view, order, rating, search
    interaction_data = Column(Text, nullable=True)  # JSON data
    rating = Column(Float, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class RecommendationLog(Base):
    __tablename__ = "recommendation_logs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False, index=True)
    recommendation_type = Column(String(50), nullable=False)  # restaurant, menu_item, cuisine
    recommended_items = Column(Text, nullable=False)  # JSON array of recommended item IDs
    context_data = Column(Text, nullable=True)  # JSON context used for recommendation
    clicked_items = Column(Text, nullable=True)  # JSON array of clicked item IDs
    conversion_rate = Column(Float, default=0.0)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

class ChatSession(Base):
    __tablename__ = "chat_sessions"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False, index=True)
    session_id = Column(String(255), nullable=False, unique=True, index=True)
    is_active = Column(Boolean, default=True)
    context_data = Column(Text, nullable=True)  # JSON context for the session
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

class ChatMessage(Base):
    __tablename__ = "chat_messages"

    id = Column(Integer, primary_key=True, index=True)
    session_id = Column(String(255), nullable=False, index=True)
    message_type = Column(String(20), nullable=False)  # user, assistant, system
    content = Column(Text, nullable=False)
    metadata = Column(Text, nullable=True)  # JSON metadata
    created_at = Column(DateTime(timezone=True), server_default=func.now())
