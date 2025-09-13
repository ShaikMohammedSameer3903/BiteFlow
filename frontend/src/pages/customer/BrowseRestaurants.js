import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchRestaurants, setSearchQuery, setFilters } from '../../store/slices/restaurantSlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const BrowseRestaurants = () => {
  const dispatch = useDispatch();
  const { restaurants, isLoading, error, searchQuery, filters } = useSelector(state => state.restaurant);
  const [localSearchQuery, setLocalSearchQuery] = useState(searchQuery);

  useEffect(() => {
    dispatch(fetchRestaurants());
  }, [dispatch]);

  const handleSearch = (e) => {
    e.preventDefault();
    dispatch(setSearchQuery(localSearchQuery));
    dispatch(fetchRestaurants({ search: localSearchQuery, ...filters }));
  };

  const handleFilterChange = (filterType, value) => {
    const newFilters = { ...filters, [filterType]: value };
    dispatch(setFilters(newFilters));
    dispatch(fetchRestaurants({ search: searchQuery, ...newFilters }));
  };

  const filteredRestaurants = restaurants.filter(restaurant => {
    const matchesSearch = !searchQuery || 
      restaurant.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      restaurant.cuisine.toLowerCase().includes(searchQuery.toLowerCase());
    
    const matchesCuisine = !filters.cuisine || restaurant.cuisine === filters.cuisine;
    const matchesRating = !filters.rating || restaurant.rating >= filters.rating;
    
    return matchesSearch && matchesCuisine && matchesRating;
  });

  const cuisineTypes = [...new Set(restaurants.map(r => r.cuisine))];

  if (isLoading) {
    return <LoadingSpinner text="Loading restaurants..." />;
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Browse Restaurants</h1>
        <p className="text-gray-600">Discover amazing food from restaurants near you</p>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-8">
        <form onSubmit={handleSearch} className="mb-4">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1">
              <input
                type="text"
                placeholder="Search restaurants or cuisines..."
                value={localSearchQuery}
                onChange={(e) => setLocalSearchQuery(e.target.value)}
                className="input-field"
              />
            </div>
            <button type="submit" className="btn-primary">
              <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              Search
            </button>
          </div>
        </form>

        <div className="flex flex-wrap gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Cuisine</label>
            <select
              value={filters.cuisine}
              onChange={(e) => handleFilterChange('cuisine', e.target.value)}
              className="input-field w-40"
            >
              <option value="">All Cuisines</option>
              {cuisineTypes.map(cuisine => (
                <option key={cuisine} value={cuisine}>{cuisine}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Min Rating</label>
            <select
              value={filters.rating}
              onChange={(e) => handleFilterChange('rating', Number(e.target.value))}
              className="input-field w-32"
            >
              <option value={0}>Any Rating</option>
              <option value={3}>3+ Stars</option>
              <option value={4}>4+ Stars</option>
              <option value={4.5}>4.5+ Stars</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Price Range</label>
            <select
              value={filters.priceRange}
              onChange={(e) => handleFilterChange('priceRange', e.target.value)}
              className="input-field w-32"
            >
              <option value="">Any Price</option>
              <option value="$">$ Budget</option>
              <option value="$$">$$ Moderate</option>
              <option value="$$$">$$$ Expensive</option>
            </select>
          </div>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md mb-6">
          {error}
        </div>
      )}

      {/* Results */}
      <div className="mb-4">
        <p className="text-gray-600">
          {filteredRestaurants.length} restaurant{filteredRestaurants.length !== 1 ? 's' : ''} found
        </p>
      </div>

      {/* Restaurant Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredRestaurants.map(restaurant => (
          <Link
            key={restaurant.id}
            to={`/restaurants/${restaurant.id}`}
            className="card-hover"
          >
            <div className="aspect-w-16 aspect-h-9 mb-4">
              <img
                src={restaurant.imageUrl || '/api/placeholder/400/200'}
                alt={restaurant.name}
                className="w-full h-48 object-cover rounded-lg"
              />
            </div>
            
            <div className="flex justify-between items-start mb-2">
              <h3 className="text-lg font-semibold text-gray-900">{restaurant.name}</h3>
              <div className="flex items-center">
                <svg className="w-4 h-4 text-yellow-400 mr-1" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
                <span className="text-sm font-medium text-gray-900">{restaurant.rating}</span>
              </div>
            </div>
            
            <p className="text-gray-600 text-sm mb-2">{restaurant.cuisine}</p>
            <p className="text-gray-500 text-sm mb-3">{restaurant.address}</p>
            
            <div className="flex justify-between items-center">
              <div className="flex items-center text-sm text-gray-500">
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {restaurant.deliveryTime} min
              </div>
              <div className="flex items-center text-sm text-gray-500">
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                </svg>
                ${restaurant.deliveryFee} delivery
              </div>
            </div>
            
            {restaurant.isOpen ? (
              <div className="mt-3 text-sm text-green-600 font-medium">Open now</div>
            ) : (
              <div className="mt-3 text-sm text-red-600 font-medium">Closed</div>
            )}
          </Link>
        ))}
      </div>

      {filteredRestaurants.length === 0 && !isLoading && (
        <div className="text-center py-12">
          <svg className="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          <h3 className="text-lg font-medium text-gray-900 mb-2">No restaurants found</h3>
          <p className="text-gray-600">Try adjusting your search criteria or filters.</p>
        </div>
      )}
    </div>
  );
};

export default BrowseRestaurants;
