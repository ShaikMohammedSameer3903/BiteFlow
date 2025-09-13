import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { updateQuantity, removeFromCart, clearCart } from '../../store/slices/cartSlice';
import { createOrder } from '../../store/slices/orderSlice';

const Cart = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { items, restaurant, total, deliveryFee, tax } = useSelector(state => state.cart);
  const { user } = useSelector(state => state.auth);
  const { isLoading } = useSelector(state => state.order);

  const subtotal = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

  const handleQuantityChange = (itemId, newQuantity) => {
    if (newQuantity <= 0) {
      dispatch(removeFromCart(itemId));
    } else {
      dispatch(updateQuantity({ itemId, quantity: newQuantity }));
    }
  };

  const handleCheckout = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    const orderData = {
      restaurantId: restaurant.id,
      items: items.map(item => ({
        menuItemId: item.id,
        quantity: item.quantity,
        price: item.price
      })),
      totalAmount: total,
      deliveryAddress: user.address || 'Default Address', // You might want to add address selection
      paymentMethod: 'CARD' // Default payment method
    };

    const result = await dispatch(createOrder(orderData));
    if (result.type === 'order/createOrder/fulfilled') {
      dispatch(clearCart());
      navigate(`/orders/${result.payload.id}/track`);
    }
  };

  if (items.length === 0) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center py-12">
          <svg className="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 1.5M7 13l-1.5-1.5m0 0L4 15m0 0h3m6 0h3m-3 0v-2a2 2 0 00-2-2H9a2 2 0 00-2 2v2" />
          </svg>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Your cart is empty</h2>
          <p className="text-gray-600 mb-6">Add some delicious items from our restaurants</p>
          <Link to="/restaurants" className="btn-primary">
            Browse Restaurants
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex flex-col lg:flex-row gap-8">
        {/* Cart Items */}
        <div className="lg:w-2/3">
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex justify-between items-center mb-6">
              <h1 className="text-2xl font-bold text-gray-900">Your Cart</h1>
              <button
                onClick={() => dispatch(clearCart())}
                className="text-red-600 hover:text-red-700 text-sm font-medium"
              >
                Clear Cart
              </button>
            </div>

            {/* Restaurant Info */}
            {restaurant && (
              <div className="border-b border-gray-200 pb-4 mb-6">
                <div className="flex items-center space-x-3">
                  <img
                    src={restaurant.imageUrl || '/api/placeholder/60/60'}
                    alt={restaurant.name}
                    className="w-12 h-12 rounded-lg object-cover"
                  />
                  <div>
                    <h3 className="font-semibold text-gray-900">{restaurant.name}</h3>
                    <p className="text-sm text-gray-600">{restaurant.cuisine}</p>
                  </div>
                </div>
              </div>
            )}

            {/* Cart Items */}
            <div className="space-y-4">
              {items.map(item => (
                <div key={item.id} className="flex items-center space-x-4 p-4 border border-gray-200 rounded-lg">
                  <img
                    src={item.imageUrl || '/api/placeholder/80/80'}
                    alt={item.name}
                    className="w-16 h-16 rounded-lg object-cover"
                  />
                  
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">{item.name}</h3>
                    <p className="text-sm text-gray-600">{item.description}</p>
                    <p className="text-lg font-bold text-primary-600">${item.price}</p>
                  </div>
                  
                  <div className="flex items-center space-x-3">
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                        className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100"
                      >
                        -
                      </button>
                      <span className="w-8 text-center font-medium">{item.quantity}</span>
                      <button
                        onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                        className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100"
                      >
                        +
                      </button>
                    </div>
                    
                    <button
                      onClick={() => dispatch(removeFromCart(item.id))}
                      className="text-red-600 hover:text-red-700 p-1"
                    >
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div className="lg:w-1/3">
          <div className="bg-white rounded-lg shadow-md p-6 sticky top-24">
            <h2 className="text-xl font-bold text-gray-900 mb-4">Order Summary</h2>
            
            <div className="space-y-3 mb-4">
              <div className="flex justify-between">
                <span className="text-gray-600">Subtotal</span>
                <span className="font-medium">${subtotal.toFixed(2)}</span>
              </div>
              
              <div className="flex justify-between">
                <span className="text-gray-600">Delivery Fee</span>
                <span className="font-medium">${deliveryFee.toFixed(2)}</span>
              </div>
              
              <div className="flex justify-between">
                <span className="text-gray-600">Tax</span>
                <span className="font-medium">${tax.toFixed(2)}</span>
              </div>
              
              <div className="border-t border-gray-200 pt-3">
                <div className="flex justify-between">
                  <span className="text-lg font-bold text-gray-900">Total</span>
                  <span className="text-lg font-bold text-gray-900">${total.toFixed(2)}</span>
                </div>
              </div>
            </div>

            <button
              onClick={handleCheckout}
              disabled={isLoading}
              className="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <div className="flex items-center justify-center">
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
                  Processing...
                </div>
              ) : (
                'Proceed to Checkout'
              )}
            </button>

            <div className="mt-4 text-xs text-gray-500 text-center">
              By placing your order, you agree to our Terms of Service and Privacy Policy
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;
