import jwt
from datetime import datetime, timedelta
from typing import Dict, Any
from fastapi import HTTPException, status
from app.core.config import settings

def verify_token(token: str) -> Dict[str, Any]:
    """Verify JWT token and return payload"""
    try:
        payload = jwt.decode(
            token,
            settings.JWT_SECRET_KEY,
            algorithms=[settings.JWT_ALGORITHM]
        )
        
        # Check if token is expired
        exp = payload.get("exp")
        if exp and datetime.utcnow() > datetime.utcfromtimestamp(exp):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token expired"
            )
        
        return payload
    except jwt.InvalidTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token"
        )

def create_access_token(data: Dict[str, Any], expires_delta: timedelta = None) -> str:
    """Create JWT access token"""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(hours=24)
    
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.JWT_SECRET_KEY, algorithm=settings.JWT_ALGORITHM)
    return encoded_jwt
