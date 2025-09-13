import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchOrders } from '../../store/slices/orderSlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const CustomerDashboard = () => {
  const dispatch = useDispatch();
  const { user } = useSelector(state => state.auth);
  const { orders, isLoading } = useSelector(state => state.order);

  useEffect(() => {
    dispatch(fetchOrders());
  }, [dispatch]);

  const recentOrders = orders.slice(0, 5);
  const activeOrders = orders.filter(order => 
    ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'PICKED_UP'].includes(order.status)
  );

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

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Welcome Section */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Welcome back, {user?.firstName || 'Customer'}!
        </h1>
        <p className="text-gray-600">Manage your orders and discover new restaurants</p>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <Link to="/restaurants" className="card-hover text-center">
          <div className="text-primary-600 mb-4">
            <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Browse Restaurants</h3>
          <p className="text-gray-600">Discover new cuisines and favorite dishes</p>
        </Link>

        <Link to="/cart" className="card-hover text-center">
          <div className="text-primary-600 mb-4">
            <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 1.5M7 13l-1.5-1.5m0 0L4 15m0 0h3m6 0h3m-3 0v-2a2 2 0 00-2-2H9a2 2 0 00-2 2v2" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">View Cart</h3>
          <p className="text-gray-600">Review and checkout your selected items</p>
        </Link>

        <div className="card text-center">
          <div className="text-primary-600 mb-4">
            <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">My Profile</h3>
          <p className="text-gray-600">Update your personal information</p>
        </div>
      </div>

      {/* Active Orders */}
      {activeOrders.length > 0 && (
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Active Orders</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {activeOrders.map(order => (
              <Link
                key={order.id}
                to={`/orders/${order.id}/track`}
                className="card-hover"
              >
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <h3 className="font-semibold text-gray-900">Order #{order.id}</h3>
                    <p className="text-sm text-gray-600">{order.restaurant?.name}</p>
                  </div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(order.status)}`}>
                    {order.status.replace('_', ' ')}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-lg font-bold text-primary-600">
                    ${order.totalAmount?.toFixed(2)}
                  </span>
                  <span className="text-sm text-gray-500">
                    {new Date(order.orderTime).toLocaleDateString()}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      {/* Recent Orders */}
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Recent Orders</h2>
          <Link to="/orders" className="text-primary-600 hover:text-primary-700 font-medium">
            View All Orders
          </Link>
        </div>

        {isLoading ? (
          <LoadingSpinner />
        ) : recentOrders.length === 0 ? (
          <div className="text-center py-8">
            <svg className="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <h3 className="text-lg font-medium text-gray-900 mb-2">No orders yet</h3>
            <p className="text-gray-600 mb-4">Start by browsing our amazing restaurants</p>
            <Link to="/restaurants" className="btn-primary">
              Order Now
            </Link>
          </div>
        ) : (
          <div className="space-y-4">
            {recentOrders.map(order => (
              <div key={order.id} className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:shadow-md transition duration-200">
                <div className="flex items-center space-x-4">
                  <img
                    src={order.restaurant?.imageUrl || '/api/placeholder/60/60'}
                    alt={order.restaurant?.name}
                    className="w-12 h-12 rounded-lg object-cover"
                  />
                  <div>
                    <h3 className="font-semibold text-gray-900">Order #{order.id}</h3>
                    <p className="text-sm text-gray-600">{order.restaurant?.name}</p>
                    <p className="text-xs text-gray-500">
                      {new Date(order.orderTime).toLocaleDateString()}
                    </p>
                  </div>
                </div>
                
                <div className="text-right">
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(order.status)} mb-2 inline-block`}>
                    {order.status.replace('_', ' ')}
                  </span>
                  <p className="font-bold text-primary-600">${order.totalAmount?.toFixed(2)}</p>
                </div>
                
                <div className="flex space-x-2">
                  <Link
                    to={`/orders/${order.id}/track`}
                    className="btn-outline text-sm"
                  >
                    Track
                  </Link>
                  {order.status === 'DELIVERED' && (
                    <button className="btn-primary text-sm">
                      Reorder
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerDashboard;
