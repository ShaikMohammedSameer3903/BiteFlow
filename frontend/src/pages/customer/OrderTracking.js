import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchOrderById } from '../../store/slices/orderSlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const OrderTracking = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const { currentOrder, isLoading, error } = useSelector(state => state.order);
  const [refreshInterval, setRefreshInterval] = useState(null);

  useEffect(() => {
    if (id) {
      dispatch(fetchOrderById(id));
      
      // Set up auto-refresh for active orders
      const interval = setInterval(() => {
        dispatch(fetchOrderById(id));
      }, 30000); // Refresh every 30 seconds
      
      setRefreshInterval(interval);
      
      return () => {
        if (interval) clearInterval(interval);
      };
    }
  }, [dispatch, id]);

  const getStatusStep = (status) => {
    const steps = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'PICKED_UP', 'DELIVERED'];
    return steps.indexOf(status);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'text-yellow-600 bg-yellow-100';
      case 'CONFIRMED':
      case 'PREPARING':
        return 'text-blue-600 bg-blue-100';
      case 'READY':
      case 'PICKED_UP':
        return 'text-purple-600 bg-purple-100';
      case 'DELIVERED':
        return 'text-green-600 bg-green-100';
      case 'CANCELLED':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getEstimatedTime = (status) => {
    switch (status) {
      case 'PENDING':
        return '2-5 minutes';
      case 'CONFIRMED':
        return '15-25 minutes';
      case 'PREPARING':
        return '10-20 minutes';
      case 'READY':
        return '5-10 minutes';
      case 'PICKED_UP':
        return '15-30 minutes';
      default:
        return '';
    }
  };

  if (isLoading) {
    return <LoadingSpinner text="Loading order details..." />;
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">
          {error}
        </div>
      </div>
    );
  }

  if (!currentOrder) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">Order not found</h2>
        </div>
      </div>
    );
  }

  const statusStep = getStatusStep(currentOrder.status);
  const statusSteps = [
    { key: 'PENDING', label: 'Order Placed', icon: '📝' },
    { key: 'CONFIRMED', label: 'Confirmed', icon: '✅' },
    { key: 'PREPARING', label: 'Preparing', icon: '👨‍🍳' },
    { key: 'READY', label: 'Ready', icon: '🍽️' },
    { key: 'PICKED_UP', label: 'Picked Up', icon: '🚗' },
    { key: 'DELIVERED', label: 'Delivered', icon: '🎉' }
  ];

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="bg-white rounded-lg shadow-md p-6 mb-8">
        <div className="flex justify-between items-start mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">Order #{currentOrder.id}</h1>
            <p className="text-gray-600">Placed on {new Date(currentOrder.orderTime).toLocaleString()}</p>
          </div>
          <div className="text-right">
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(currentOrder.status)}`}>
              {currentOrder.status.replace('_', ' ')}
            </span>
            {getEstimatedTime(currentOrder.status) && (
              <p className="text-sm text-gray-600 mt-1">
                ETA: {getEstimatedTime(currentOrder.status)}
              </p>
            )}
          </div>
        </div>

        {/* Order Status Timeline */}
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Order Status</h2>
          <div className="flex items-center justify-between">
            {statusSteps.map((step, index) => (
              <div key={step.key} className="flex flex-col items-center flex-1">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center text-lg mb-2 ${
                  index <= statusStep 
                    ? 'bg-primary-600 text-white' 
                    : 'bg-gray-200 text-gray-400'
                }`}>
                  {index <= statusStep ? '✓' : step.icon}
                </div>
                <span className={`text-xs text-center ${
                  index <= statusStep ? 'text-primary-600 font-medium' : 'text-gray-400'
                }`}>
                  {step.label}
                </span>
                {index < statusSteps.length - 1 && (
                  <div className={`h-1 w-full mt-2 ${
                    index < statusStep ? 'bg-primary-600' : 'bg-gray-200'
                  }`} />
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Restaurant Info */}
        <div className="border-t border-gray-200 pt-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Restaurant</h3>
          <div className="flex items-center space-x-3">
            <img
              src={currentOrder.restaurant?.imageUrl || '/api/placeholder/60/60'}
              alt={currentOrder.restaurant?.name}
              className="w-12 h-12 rounded-lg object-cover"
            />
            <div>
              <h4 className="font-medium text-gray-900">{currentOrder.restaurant?.name}</h4>
              <p className="text-sm text-gray-600">{currentOrder.restaurant?.address}</p>
            </div>
          </div>
        </div>

        {/* Order Items */}
        <div className="border-t border-gray-200 pt-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Order Items</h3>
          <div className="space-y-3">
            {currentOrder.items?.map((item, index) => (
              <div key={index} className="flex justify-between items-center">
                <div className="flex items-center space-x-3">
                  <span className="w-6 h-6 bg-gray-100 rounded-full flex items-center justify-center text-sm font-medium">
                    {item.quantity}
                  </span>
                  <span className="text-gray-900">{item.menuItem?.name || item.name}</span>
                </div>
                <span className="font-medium">${(item.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Delivery Info */}
        <div className="border-t border-gray-200 pt-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-3">Delivery Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-600">Delivery Address</p>
              <p className="font-medium">{currentOrder.deliveryAddress}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Payment Method</p>
              <p className="font-medium">{currentOrder.paymentMethod}</p>
            </div>
          </div>
        </div>

        {/* Order Total */}
        <div className="border-t border-gray-200 pt-6">
          <div className="flex justify-between items-center">
            <span className="text-lg font-semibold text-gray-900">Total Amount</span>
            <span className="text-xl font-bold text-primary-600">${currentOrder.totalAmount?.toFixed(2)}</span>
          </div>
        </div>

        {/* Contact Support */}
        <div className="mt-6 p-4 bg-gray-50 rounded-lg">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-medium text-gray-900">Need help with your order?</h4>
              <p className="text-sm text-gray-600">Contact our support team</p>
            </div>
            <button className="btn-outline">
              Contact Support
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderTracking;
