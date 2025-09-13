import numpy as np
from typing import List, Dict, Tuple, Optional
from geopy.distance import geodesic
from geopy.geocoders import Nominatim
import asyncio
import aiohttp
from app.core.config import settings

class RouteOptimizer:
    def __init__(self):
        self.geocoder = Nominatim(user_agent="food-delivery-ai")
        self.google_maps_api_key = settings.GOOGLE_MAPS_API_KEY

    async def geocode_address(self, address: str) -> Optional[Tuple[float, float]]:
        """Convert address to coordinates"""
        try:
            location = self.geocoder.geocode(address)
            if location:
                return (location.latitude, location.longitude)
            return None
        except Exception as e:
            print(f"Error geocoding address {address}: {e}")
            return None

    async def calculate_distance(self, coord1: Tuple[float, float], coord2: Tuple[float, float]) -> float:
        """Calculate distance between two coordinates in kilometers"""
        try:
            return geodesic(coord1, coord2).kilometers
        except Exception as e:
            print(f"Error calculating distance: {e}")
            return 0.0

    async def get_travel_time_google(self, origin: str, destination: str, mode: str = "driving") -> Optional[int]:
        """Get travel time using Google Maps API"""
        if not self.google_maps_api_key:
            return None
        
        try:
            url = "https://maps.googleapis.com/maps/api/distancematrix/json"
            params = {
                "origins": origin,
                "destinations": destination,
                "mode": mode,
                "units": "metric",
                "key": self.google_maps_api_key
            }
            
            async with aiohttp.ClientSession() as session:
                async with session.get(url, params=params) as response:
                    if response.status == 200:
                        data = await response.json()
                        if (data["status"] == "OK" and 
                            data["rows"] and 
                            data["rows"][0]["elements"] and
                            data["rows"][0]["elements"][0]["status"] == "OK"):
                            
                            duration = data["rows"][0]["elements"][0]["duration"]["value"]
                            return duration // 60  # Convert seconds to minutes
            
            return None
        except Exception as e:
            print(f"Error getting travel time from Google Maps: {e}")
            return None

    async def estimate_travel_time(self, origin_coords: Tuple[float, float], 
                                 dest_coords: Tuple[float, float]) -> int:
        """Estimate travel time based on distance and average speed"""
        try:
            distance_km = await self.calculate_distance(origin_coords, dest_coords)
            
            # Estimate based on average urban driving speed (25 km/h including stops)
            avg_speed_kmh = 25
            time_hours = distance_km / avg_speed_kmh
            time_minutes = int(time_hours * 60)
            
            # Add buffer time for pickup/dropoff
            buffer_minutes = 5
            
            return max(5, time_minutes + buffer_minutes)  # Minimum 5 minutes
            
        except Exception as e:
            print(f"Error estimating travel time: {e}")
            return 30  # Default 30 minutes

    async def optimize_delivery_route(self, delivery_person_location: Tuple[float, float],
                                    deliveries: List[Dict]) -> List[Dict]:
        """Optimize delivery route using nearest neighbor algorithm"""
        try:
            if not deliveries:
                return []

            # Convert addresses to coordinates
            delivery_coords = []
            for delivery in deliveries:
                pickup_coords = await self.geocode_address(delivery.get('pickup_address', ''))
                delivery_coords_dest = await self.geocode_address(delivery.get('delivery_address', ''))
                
                if pickup_coords and delivery_coords_dest:
                    delivery_coords.append({
                        'delivery': delivery,
                        'pickup_coords': pickup_coords,
                        'delivery_coords': delivery_coords_dest
                    })

            if not delivery_coords:
                return deliveries

            # Simple nearest neighbor optimization
            optimized_route = []
            current_location = delivery_person_location
            remaining_deliveries = delivery_coords.copy()

            while remaining_deliveries:
                # Find nearest pickup location
                nearest_delivery = None
                min_distance = float('inf')
                
                for delivery_info in remaining_deliveries:
                    distance = await self.calculate_distance(current_location, delivery_info['pickup_coords'])
                    if distance < min_distance:
                        min_distance = distance
                        nearest_delivery = delivery_info

                if nearest_delivery:
                    # Add to optimized route
                    pickup_time = await self.estimate_travel_time(current_location, nearest_delivery['pickup_coords'])
                    delivery_time = await self.estimate_travel_time(
                        nearest_delivery['pickup_coords'], 
                        nearest_delivery['delivery_coords']
                    )
                    
                    optimized_delivery = nearest_delivery['delivery'].copy()
                    optimized_delivery.update({
                        'estimated_pickup_time': pickup_time,
                        'estimated_delivery_time': delivery_time,
                        'total_time': pickup_time + delivery_time,
                        'pickup_coords': nearest_delivery['pickup_coords'],
                        'delivery_coords': nearest_delivery['delivery_coords']
                    })
                    
                    optimized_route.append(optimized_delivery)
                    
                    # Update current location to delivery destination
                    current_location = nearest_delivery['delivery_coords']
                    remaining_deliveries.remove(nearest_delivery)

            return optimized_route

        except Exception as e:
            print(f"Error optimizing delivery route: {e}")
            return deliveries

    async def calculate_route_metrics(self, route: List[Dict]) -> Dict:
        """Calculate metrics for a delivery route"""
        try:
            total_distance = 0.0
            total_time = 0
            total_deliveries = len(route)
            
            for i, delivery in enumerate(route):
                if 'total_time' in delivery:
                    total_time += delivery['total_time']
                
                if i > 0 and 'pickup_coords' in delivery and 'delivery_coords' in route[i-1]:
                    # Distance from previous delivery to current pickup
                    distance = await self.calculate_distance(
                        route[i-1]['delivery_coords'],
                        delivery['pickup_coords']
                    )
                    total_distance += distance
                
                if 'pickup_coords' in delivery and 'delivery_coords' in delivery:
                    # Distance from pickup to delivery
                    distance = await self.calculate_distance(
                        delivery['pickup_coords'],
                        delivery['delivery_coords']
                    )
                    total_distance += distance

            return {
                'total_distance_km': round(total_distance, 2),
                'total_time_minutes': total_time,
                'total_deliveries': total_deliveries,
                'average_time_per_delivery': round(total_time / max(1, total_deliveries), 2),
                'efficiency_score': round((total_deliveries / max(1, total_time)) * 100, 2)
            }

        except Exception as e:
            print(f"Error calculating route metrics: {e}")
            return {
                'total_distance_km': 0.0,
                'total_time_minutes': 0,
                'total_deliveries': len(route),
                'average_time_per_delivery': 0.0,
                'efficiency_score': 0.0
            }

    async def find_optimal_delivery_person(self, pickup_location: Tuple[float, float],
                                         available_drivers: List[Dict]) -> Optional[Dict]:
        """Find the optimal delivery person for a pickup"""
        try:
            if not available_drivers:
                return None

            best_driver = None
            min_score = float('inf')

            for driver in available_drivers:
                driver_location = driver.get('current_location')
                if not driver_location:
                    continue

                # Calculate distance to pickup
                distance = await self.calculate_distance(driver_location, pickup_location)
                travel_time = await self.estimate_travel_time(driver_location, pickup_location)
                
                # Calculate score based on distance, current load, and rating
                current_deliveries = len(driver.get('active_deliveries', []))
                driver_rating = driver.get('rating', 5.0)
                
                # Lower score is better
                score = (distance * 0.4 + 
                        travel_time * 0.3 + 
                        current_deliveries * 5 + 
                        (5.0 - driver_rating) * 2)

                if score < min_score:
                    min_score = score
                    best_driver = driver.copy()
                    best_driver.update({
                        'distance_to_pickup': distance,
                        'estimated_arrival_time': travel_time,
                        'assignment_score': score
                    })

            return best_driver

        except Exception as e:
            print(f"Error finding optimal delivery person: {e}")
            return None

    async def predict_delivery_demand(self, area_coords: Tuple[float, float], 
                                    time_window: int = 60) -> Dict:
        """Predict delivery demand for an area in the next time window"""
        try:
            # This is a simplified prediction model
            # In production, this would use historical data and ML models
            
            import datetime
            current_hour = datetime.datetime.now().hour
            current_day = datetime.datetime.now().weekday()
            
            # Base demand factors
            base_demand = 10
            
            # Time-based factors
            if 11 <= current_hour <= 14:  # Lunch time
                time_factor = 2.0
            elif 18 <= current_hour <= 21:  # Dinner time
                time_factor = 2.5
            elif 21 <= current_hour <= 23:  # Late night
                time_factor = 1.5
            else:
                time_factor = 0.5
            
            # Day-based factors
            if current_day in [5, 6]:  # Weekend
                day_factor = 1.3
            else:
                day_factor = 1.0
            
            # Weather factor (simplified)
            weather_factor = 1.2  # Assume slightly higher demand due to weather
            
            predicted_orders = int(base_demand * time_factor * day_factor * weather_factor)
            
            return {
                'predicted_orders': predicted_orders,
                'time_window_minutes': time_window,
                'confidence_score': 0.75,  # Simplified confidence
                'factors': {
                    'time_factor': time_factor,
                    'day_factor': day_factor,
                    'weather_factor': weather_factor
                }
            }

        except Exception as e:
            print(f"Error predicting delivery demand: {e}")
            return {
                'predicted_orders': 10,
                'time_window_minutes': time_window,
                'confidence_score': 0.5,
                'factors': {}
            }
