import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchRestaurantById, fetchMenuItems } from '../../store/slices/restaurantSlice';
import { addToCart } from '../../store/slices/cartSlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const RestaurantDetails = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const { currentRestaurant, menuItems, isLoading, error } = useSelector(state => state.restaurant);
  const { user } = useSelector(state => state.auth);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [quantities, setQuantities] = useState({});

  useEffect(() => {
    if (id) {
      dispatch(fetchRestaurantById(id));
      dispatch(fetchMenuItems(id));
    }
  }, [dispatch, id]);

  const categories = [...new Set(menuItems.map(item => item.category))];

  const filteredMenuItems = selectedCategory 
    ? menuItems.filter(item => item.category === selectedCategory)
    : menuItems;

  const handleAddToCart = (item) => {
    if (!user) {
      alert('Please login to add items to cart');
      return;
    }
    
    const quantity = quantities[item.id] || 1;
    for (let i = 0; i < quantity; i++) {
      dispatch(addToCart({ item, restaurant: currentRestaurant }));
    }
    setQuantities({ ...quantities, [item.id]: 1 });
  };

  const updateQuantity = (itemId, quantity) => {
    setQuantities({ ...quantities, [itemId]: Math.max(1, quantity) });
  };

  if (isLoading) {
    return <LoadingSpinner text="Loading restaurant details..." />;
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">
          {error}
        </div>
      </div>
    );
  }

  if (!currentRestaurant) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">Restaurant not found</h2>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Restaurant Header */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden mb-8">
        <div className="aspect-w-16 aspect-h-6">
          <img
            src={currentRestaurant.imageUrl || '/api/placeholder/800/300'}
            alt={currentRestaurant.name}
            className="w-full h-64 object-cover"
          />
        </div>
        
        <div className="p-6">
          <div className="flex justify-between items-start mb-4">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{currentRestaurant.name}</h1>
              <p className="text-gray-600 mb-2">{currentRestaurant.description}</p>
              <p className="text-gray-500">{currentRestaurant.address}</p>
            </div>
            
            <div className="text-right">
              <div className="flex items-center mb-2">
                <svg className="w-5 h-5 text-yellow-400 mr-1" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
                <span className="font-medium">{currentRestaurant.rating}</span>
              </div>
              
              <div className="flex items-center text-sm text-gray-500 mb-1">
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {currentRestaurant.deliveryTime} min delivery
              </div>
              
              <div className="flex items-center text-sm text-gray-500">
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                </svg>
                ${currentRestaurant.deliveryFee} delivery fee
              </div>
            </div>
          </div>
          
          <div className="flex items-center space-x-4">
            <span className="bg-primary-100 text-primary-800 px-3 py-1 rounded-full text-sm font-medium">
              {currentRestaurant.cuisine}
            </span>
            {currentRestaurant.isOpen ? (
              <span className="bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
                Open now
              </span>
            ) : (
              <span className="bg-red-100 text-red-800 px-3 py-1 rounded-full text-sm font-medium">
                Closed
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Menu Section */}
      <div className="flex flex-col lg:flex-row gap-8">
        {/* Category Filter */}
        {categories.length > 0 && (
          <div className="lg:w-1/4">
            <div className="bg-white rounded-lg shadow-md p-6 sticky top-24">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Categories</h3>
              <div className="space-y-2">
                <button
                  onClick={() => setSelectedCategory('')}
                  className={`w-full text-left px-3 py-2 rounded-md transition duration-200 ${
                    !selectedCategory 
                      ? 'bg-primary-100 text-primary-800' 
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  All Items
                </button>
                {categories.map(category => (
                  <button
                    key={category}
                    onClick={() => setSelectedCategory(category)}
                    className={`w-full text-left px-3 py-2 rounded-md transition duration-200 ${
                      selectedCategory === category 
                        ? 'bg-primary-100 text-primary-800' 
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    {category}
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Menu Items */}
        <div className="flex-1">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">
              {selectedCategory || 'Menu'}
            </h2>
            
            {filteredMenuItems.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500">No menu items available</p>
              </div>
            ) : (
              <div className="space-y-6">
                {filteredMenuItems.map(item => (
                  <div key={item.id} className="flex items-center space-x-4 p-4 border border-gray-200 rounded-lg hover:shadow-md transition duration-200">
                    <div className="w-24 h-24 flex-shrink-0">
                      <img
                        src={item.imageUrl || '/api/placeholder/100/100'}
                        alt={item.name}
                        className="w-full h-full object-cover rounded-lg"
                      />
                    </div>
                    
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold text-gray-900 mb-1">{item.name}</h3>
                      <p className="text-gray-600 text-sm mb-2">{item.description}</p>
                      <div className="flex items-center justify-between">
                        <span className="text-lg font-bold text-primary-600">${item.price}</span>
                        
                        {item.available ? (
                          <div className="flex items-center space-x-3">
                            <div className="flex items-center space-x-2">
                              <button
                                onClick={() => updateQuantity(item.id, (quantities[item.id] || 1) - 1)}
                                className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100"
                              >
                                -
                              </button>
                              <span className="w-8 text-center">{quantities[item.id] || 1}</span>
                              <button
                                onClick={() => updateQuantity(item.id, (quantities[item.id] || 1) + 1)}
                                className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100"
                              >
                                +
                              </button>
                            </div>
                            
                            <button
                              onClick={() => handleAddToCart(item)}
                              className="btn-primary"
                            >
                              Add to Cart
                            </button>
                          </div>
                        ) : (
                          <span className="text-red-500 font-medium">Unavailable</span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RestaurantDetails;
