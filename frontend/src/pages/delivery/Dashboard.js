import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchDeliveries, acceptDelivery, updateDeliveryStatus } from '../../store/slices/deliverySlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const DeliveryDashboard = () => {
  const dispatch = useDispatch();
  const { user } = useSelector(state => state.auth);
  const { deliveries, isLoading, error } = useSelector(state => state.delivery);

  useEffect(() => {
    dispatch(fetchDeliveries({ deliveryPersonId: user?.id }));
  }, [dispatch, user?.id]);

  const activeDeliveries = deliveries.filter(delivery => 
    ['ASSIGNED', 'PICKED_UP', 'IN_TRANSIT'].includes(delivery.status)
  );

  const availableDeliveries = deliveries.filter(delivery => 
    delivery.status === 'PENDING' && !delivery.deliveryPersonId
  );

  const completedToday = deliveries.filter(delivery => {
    const today = new Date().toDateString();
    return delivery.status === 'DELIVERED' && 
           new Date(delivery.deliveredAt).toDateString() === today;
  });

  const todayEarnings = completedToday.reduce((sum, delivery) => sum + (delivery.deliveryFee || 0), 0);

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'text-yellow-600 bg-yellow-100';
      case 'ASSIGNED':
        return 'text-blue-600 bg-blue-100';
      case 'PICKED_UP':
      case 'IN_TRANSIT':
        return 'text-purple-600 bg-purple-100';
      case 'DELIVERED':
        return 'text-green-600 bg-green-100';
      case 'CANCELLED':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const handleAcceptDelivery = async (deliveryId) => {
    await dispatch(acceptDelivery(deliveryId));
  };

  const handleStatusUpdate = async (deliveryId, status) => {
    await dispatch(updateDeliveryStatus({ deliveryId, status }));
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Delivery Dashboard
        </h1>
        <p className="text-gray-600">
          Welcome back, {user?.firstName || 'Driver'}! Manage your deliveries and earnings.
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-blue-100 text-blue-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Active Deliveries</p>
              <p className="text-2xl font-bold text-gray-900">{activeDeliveries.length}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-yellow-100 text-yellow-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Available Orders</p>
              <p className="text-2xl font-bold text-gray-900">{availableDeliveries.length}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-green-100 text-green-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Completed Today</p>
              <p className="text-2xl font-bold text-gray-900">{completedToday.length}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-purple-100 text-purple-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Today's Earnings</p>
              <p className="text-2xl font-bold text-gray-900">${todayEarnings.toFixed(2)}</p>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md mb-6">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Active Deliveries */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Active Deliveries</h2>
          
          {activeDeliveries.length === 0 ? (
            <div className="text-center py-8">
              <svg className="w-12 h-12 text-gray-400 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              <p className="text-gray-600">No active deliveries</p>
            </div>
          ) : (
            <div className="space-y-4">
              {activeDeliveries.map(delivery => (
                <div key={delivery.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <h3 className="font-semibold text-gray-900">Order #{delivery.orderId}</h3>
                      <p className="text-sm text-gray-600">{delivery.restaurant?.name}</p>
                      <p className="text-xs text-gray-500">Tracking: {delivery.trackingCode}</p>
                    </div>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(delivery.status)}`}>
                      {delivery.status.replace('_', ' ')}
                    </span>
                  </div>
                  
                  <div className="text-sm text-gray-600 mb-3">
                    <p><strong>Pickup:</strong> {delivery.pickupAddress}</p>
                    <p><strong>Delivery:</strong> {delivery.deliveryAddress}</p>
                  </div>
                  
                  <div className="flex justify-between items-center">
                    <span className="font-medium text-primary-600">${delivery.deliveryFee}</span>
                    <div className="flex space-x-2">
                      {delivery.status === 'ASSIGNED' && (
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'PICKED_UP')}
                          className="btn-primary text-sm"
                        >
                          Mark Picked Up
                        </button>
                      )}
                      {delivery.status === 'PICKED_UP' && (
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'IN_TRANSIT')}
                          className="btn-primary text-sm"
                        >
                          Start Delivery
                        </button>
                      )}
                      {delivery.status === 'IN_TRANSIT' && (
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'DELIVERED')}
                          className="btn-secondary text-sm"
                        >
                          Mark Delivered
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Available Deliveries */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Available Deliveries</h2>
          
          {isLoading ? (
            <LoadingSpinner />
          ) : availableDeliveries.length === 0 ? (
            <div className="text-center py-8">
              <svg className="w-12 h-12 text-gray-400 mx-auto mb-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-gray-600">No available deliveries</p>
            </div>
          ) : (
            <div className="space-y-4">
              {availableDeliveries.map(delivery => (
                <div key={delivery.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <h3 className="font-semibold text-gray-900">Order #{delivery.orderId}</h3>
                      <p className="text-sm text-gray-600">{delivery.restaurant?.name}</p>
                      <p className="text-xs text-gray-500">
                        Distance: {delivery.estimatedDistance || 'N/A'} km
                      </p>
                    </div>
                    <span className="font-medium text-primary-600">${delivery.deliveryFee}</span>
                  </div>
                  
                  <div className="text-sm text-gray-600 mb-3">
                    <p><strong>Pickup:</strong> {delivery.pickupAddress}</p>
                    <p><strong>Delivery:</strong> {delivery.deliveryAddress}</p>
                  </div>
                  
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">
                      Est. time: {delivery.estimatedTime || '30'} min
                    </span>
                    <button
                      onClick={() => handleAcceptDelivery(delivery.id)}
                      disabled={isLoading}
                      className="btn-primary text-sm disabled:opacity-50"
                    >
                      Accept Delivery
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Recent Deliveries */}
      <div className="bg-white rounded-lg shadow-md p-6 mt-8">
        <h2 className="text-xl font-bold text-gray-900 mb-4">Recent Deliveries</h2>
        
        {deliveries.filter(d => d.status === 'DELIVERED').slice(0, 5).length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-600">No completed deliveries yet</p>
          </div>
        ) : (
          <div className="space-y-3">
            {deliveries
              .filter(d => d.status === 'DELIVERED')
              .slice(0, 5)
              .map(delivery => (
                <div key={delivery.id} className="flex justify-between items-center p-3 border border-gray-200 rounded-lg">
                  <div>
                    <h4 className="font-medium text-gray-900">Order #{delivery.orderId}</h4>
                    <p className="text-sm text-gray-600">{delivery.restaurant?.name}</p>
                    <p className="text-xs text-gray-500">
                      {new Date(delivery.deliveredAt).toLocaleString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <span className="font-medium text-green-600">${delivery.deliveryFee}</span>
                    <p className="text-xs text-gray-500">Completed</p>
                  </div>
                </div>
              ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default DeliveryDashboard;
