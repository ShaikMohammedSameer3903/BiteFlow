from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import List, Dict, Tuple, Optional
from app.services.route_optimizer import RouteOptimizer

router = APIRouter()

class DeliveryLocation(BaseModel):
    address: str
    latitude: Optional[float] = None
    longitude: Optional[float] = None

class DeliveryRequest(BaseModel):
    id: int
    pickup_address: str
    delivery_address: str
    priority: int = 1  # 1 = normal, 2 = high, 3 = urgent
    estimated_prep_time: int = 15  # minutes
    order_value: float = 0.0

class RouteOptimizationRequest(BaseModel):
    delivery_person_id: int
    current_location: DeliveryLocation
    pending_deliveries: List[DeliveryRequest]

class DeliveryPersonLocation(BaseModel):
    id: int
    current_location: DeliveryLocation
    active_deliveries: List[int] = []
    rating: float = 5.0
    max_capacity: int = 5

class OptimalAssignmentRequest(BaseModel):
    pickup_location: DeliveryLocation
    available_drivers: List[DeliveryPersonLocation]

@router.post("/optimize-route")
async def optimize_delivery_route(
    request: RouteOptimizationRequest,
    route_optimizer: RouteOptimizer = Depends(RouteOptimizer)
):
    """Optimize delivery route for a delivery person"""
    try:
        # Convert current location to coordinates
        current_coords = None
        if request.current_location.latitude and request.current_location.longitude:
            current_coords = (request.current_location.latitude, request.current_location.longitude)
        else:
            current_coords = await route_optimizer.geocode_address(request.current_location.address)
        
        if not current_coords:
            raise HTTPException(status_code=400, detail="Could not geocode current location")
        
        # Convert delivery requests to format expected by optimizer
        deliveries = []
        for delivery in request.pending_deliveries:
            deliveries.append({
                'id': delivery.id,
                'pickup_address': delivery.pickup_address,
                'delivery_address': delivery.delivery_address,
                'priority': delivery.priority,
                'estimated_prep_time': delivery.estimated_prep_time,
                'order_value': delivery.order_value
            })
        
        # Optimize route
        optimized_route = await route_optimizer.optimize_delivery_route(current_coords, deliveries)
        
        # Calculate route metrics
        route_metrics = await route_optimizer.calculate_route_metrics(optimized_route)
        
        return {
            "delivery_person_id": request.delivery_person_id,
            "optimized_route": optimized_route,
            "route_metrics": route_metrics,
            "total_deliveries": len(optimized_route)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error optimizing route: {str(e)}")

@router.post("/find-optimal-driver")
async def find_optimal_delivery_person(
    request: OptimalAssignmentRequest,
    route_optimizer: RouteOptimizer = Depends(RouteOptimizer)
):
    """Find the optimal delivery person for a pickup"""
    try:
        # Convert pickup location to coordinates
        pickup_coords = None
        if request.pickup_location.latitude and request.pickup_location.longitude:
            pickup_coords = (request.pickup_location.latitude, request.pickup_location.longitude)
        else:
            pickup_coords = await route_optimizer.geocode_address(request.pickup_location.address)
        
        if not pickup_coords:
            raise HTTPException(status_code=400, detail="Could not geocode pickup location")
        
        # Convert driver data to format expected by optimizer
        drivers = []
        for driver in request.available_drivers:
            driver_coords = None
            if driver.current_location.latitude and driver.current_location.longitude:
                driver_coords = (driver.current_location.latitude, driver.current_location.longitude)
            else:
                driver_coords = await route_optimizer.geocode_address(driver.current_location.address)
            
            if driver_coords:
                drivers.append({
                    'id': driver.id,
                    'current_location': driver_coords,
                    'active_deliveries': driver.active_deliveries,
                    'rating': driver.rating,
                    'max_capacity': driver.max_capacity
                })
        
        # Find optimal driver
        optimal_driver = await route_optimizer.find_optimal_delivery_person(pickup_coords, drivers)
        
        if optimal_driver:
            return {
                "optimal_driver": optimal_driver,
                "pickup_location": request.pickup_location.dict(),
                "assignment_reason": f"Best match based on distance ({optimal_driver.get('distance_to_pickup', 0):.1f}km) and availability"
            }
        else:
            return {
                "optimal_driver": None,
                "message": "No suitable drivers available",
                "pickup_location": request.pickup_location.dict()
            }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error finding optimal driver: {str(e)}")

@router.get("/travel-time")
async def calculate_travel_time(
    origin_address: str,
    destination_address: str,
    mode: str = "driving",
    route_optimizer: RouteOptimizer = Depends(RouteOptimizer)
):
    """Calculate travel time between two addresses"""
    try:
        # Try Google Maps API first
        travel_time = await route_optimizer.get_travel_time_google(origin_address, destination_address, mode)
        
        if travel_time is not None:
            return {
                "origin": origin_address,
                "destination": destination_address,
                "travel_time_minutes": travel_time,
                "mode": mode,
                "source": "google_maps"
            }
        
        # Fallback to coordinate-based estimation
        origin_coords = await route_optimizer.geocode_address(origin_address)
        dest_coords = await route_optimizer.geocode_address(destination_address)
        
        if origin_coords and dest_coords:
            estimated_time = await route_optimizer.estimate_travel_time(origin_coords, dest_coords)
            return {
                "origin": origin_address,
                "destination": destination_address,
                "travel_time_minutes": estimated_time,
                "mode": mode,
                "source": "estimated"
            }
        else:
            raise HTTPException(status_code=400, detail="Could not geocode one or both addresses")
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error calculating travel time: {str(e)}")

@router.get("/distance")
async def calculate_distance(
    origin_address: str,
    destination_address: str,
    route_optimizer: RouteOptimizer = Depends(RouteOptimizer)
):
    """Calculate distance between two addresses"""
    try:
        # Geocode addresses
        origin_coords = await route_optimizer.geocode_address(origin_address)
        dest_coords = await route_optimizer.geocode_address(destination_address)
        
        if not origin_coords or not dest_coords:
            raise HTTPException(status_code=400, detail="Could not geocode one or both addresses")
        
        # Calculate distance
        distance_km = await route_optimizer.calculate_distance(origin_coords, dest_coords)
        
        return {
            "origin": origin_address,
            "destination": destination_address,
            "distance_km": round(distance_km, 2),
            "distance_miles": round(distance_km * 0.621371, 2),
            "origin_coords": origin_coords,
            "destination_coords": dest_coords
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error calculating distance: {str(e)}")

@router.post("/predict-demand")
async def predict_delivery_demand(
    area_center: DeliveryLocation,
    time_window_minutes: int = 60,
    route_optimizer: RouteOptimizer = Depends(RouteOptimizer)
):
    """Predict delivery demand for an area"""
    try:
        # Convert area center to coordinates
        area_coords = None
        if area_center.latitude and area_center.longitude:
            area_coords = (area_center.latitude, area_center.longitude)
        else:
            area_coords = await route_optimizer.geocode_address(area_center.address)
        
        if not area_coords:
            raise HTTPException(status_code=400, detail="Could not geocode area center")
        
        # Predict demand
        demand_prediction = await route_optimizer.predict_delivery_demand(area_coords, time_window_minutes)
        
        return {
            "area_center": area_center.dict(),
            "area_coords": area_coords,
            "demand_prediction": demand_prediction,
            "time_window_minutes": time_window_minutes
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error predicting demand: {str(e)}")

@router.get("/geocode")
async def geocode_address(
    address: str,
    route_optimizer: RouteOptimizer = Depends(RouteOptimizer)
):
    """Convert address to coordinates"""
    try:
        coords = await route_optimizer.geocode_address(address)
        
        if coords:
            return {
                "address": address,
                "latitude": coords[0],
                "longitude": coords[1],
                "success": True
            }
        else:
            return {
                "address": address,
                "latitude": None,
                "longitude": None,
                "success": False,
                "message": "Could not geocode address"
            }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error geocoding address: {str(e)}")

@router.post("/batch-optimize")
async def batch_optimize_routes(
    delivery_assignments: List[RouteOptimizationRequest],
    route_optimizer: RouteOptimizer = Depends(RouteOptimizer)
):
    """Optimize routes for multiple delivery persons"""
    try:
        results = []
        
        for assignment in delivery_assignments:
            try:
                # Convert current location to coordinates
                current_coords = None
                if assignment.current_location.latitude and assignment.current_location.longitude:
                    current_coords = (assignment.current_location.latitude, assignment.current_location.longitude)
                else:
                    current_coords = await route_optimizer.geocode_address(assignment.current_location.address)
                
                if current_coords:
                    # Convert delivery requests
                    deliveries = []
                    for delivery in assignment.pending_deliveries:
                        deliveries.append({
                            'id': delivery.id,
                            'pickup_address': delivery.pickup_address,
                            'delivery_address': delivery.delivery_address,
                            'priority': delivery.priority,
                            'estimated_prep_time': delivery.estimated_prep_time,
                            'order_value': delivery.order_value
                        })
                    
                    # Optimize route
                    optimized_route = await route_optimizer.optimize_delivery_route(current_coords, deliveries)
                    route_metrics = await route_optimizer.calculate_route_metrics(optimized_route)
                    
                    results.append({
                        "delivery_person_id": assignment.delivery_person_id,
                        "optimized_route": optimized_route,
                        "route_metrics": route_metrics,
                        "success": True
                    })
                else:
                    results.append({
                        "delivery_person_id": assignment.delivery_person_id,
                        "success": False,
                        "error": "Could not geocode current location"
                    })
                    
            except Exception as e:
                results.append({
                    "delivery_person_id": assignment.delivery_person_id,
                    "success": False,
                    "error": str(e)
                })
        
        return {
            "batch_results": results,
            "total_processed": len(delivery_assignments),
            "successful": len([r for r in results if r.get('success', False)]),
            "failed": len([r for r in results if not r.get('success', False)])
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error in batch optimization: {str(e)}")
