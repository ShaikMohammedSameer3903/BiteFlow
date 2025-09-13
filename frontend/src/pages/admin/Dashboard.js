import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import { fetchOrders } from '../../store/slices/orderSlice';
import { fetchRestaurants } from '../../store/slices/restaurantSlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const AdminDashboard = () => {
  const dispatch = useDispatch();
  const { orders, isLoading: ordersLoading } = useSelector(state => state.order);
  const { restaurants, isLoading: restaurantsLoading } = useSelector(state => state.restaurant);

  useEffect(() => {
    dispatch(fetchOrders());
    dispatch(fetchRestaurants());
  }, [dispatch]);

  const todayOrders = orders.filter(order => {
    const today = new Date().toDateString();
    return new Date(order.orderTime).toDateString() === today;
  });

  const pendingRestaurants = restaurants.filter(r => !r.approved);
  const activeRestaurants = restaurants.filter(r => r.approved && r.isOpen);

  const todayRevenue = todayOrders.reduce((sum, order) => sum + (order.totalAmount || 0), 0);
  const platformFee = todayRevenue * 0.15; // 15% platform fee

  const ordersByStatus = orders.reduce((acc, order) => {
    acc[order.status] = (acc[order.status] || 0) + 1;
    return acc;
  }, {});

  const recentOrders = orders.slice(0, 10);

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
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Admin Dashboard</h1>
        <p className="text-gray-600">Platform overview and management</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-blue-100 text-blue-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Total Orders</p>
              <p className="text-2xl font-bold text-gray-900">{orders.length}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-green-100 text-green-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Active Restaurants</p>
              <p className="text-2xl font-bold text-gray-900">{activeRestaurants.length}</p>
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
              <p className="text-sm font-medium text-gray-600">Pending Approvals</p>
              <p className="text-2xl font-bold text-gray-900">{pendingRestaurants.length}</p>
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
              <p className="text-sm font-medium text-gray-600">Today's Revenue</p>
              <p className="text-2xl font-bold text-gray-900">${todayRevenue.toFixed(2)}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <Link to="/admin/restaurants" className="card-hover text-center">
          <div className="text-primary-600 mb-4">
            <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Manage Restaurants</h3>
          <p className="text-gray-600">Approve and manage restaurant partners</p>
        </Link>

        <Link to="/admin/users" className="card-hover text-center">
          <div className="text-primary-600 mb-4">
            <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">User Management</h3>
          <p className="text-gray-600">Manage customers and delivery drivers</p>
        </Link>

        <Link to="/admin/analytics" className="card-hover text-center">
          <div className="text-primary-600 mb-4">
            <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Analytics</h3>
          <p className="text-gray-600">View detailed reports and insights</p>
        </Link>

        <div className="card text-center">
          <div className="text-primary-600 mb-4">
            <svg className="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Platform Settings</h3>
          <p className="text-gray-600">Configure platform-wide settings</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Recent Orders */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-bold text-gray-900">Recent Orders</h2>
            <Link to="/admin/orders" className="text-primary-600 hover:text-primary-700 font-medium">
              View All
            </Link>
          </div>

          {ordersLoading ? (
            <LoadingSpinner />
          ) : recentOrders.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-600">No orders yet</p>
            </div>
          ) : (
            <div className="space-y-3">
              {recentOrders.map(order => (
                <div key={order.id} className="flex justify-between items-center p-3 border border-gray-200 rounded-lg">
                  <div>
                    <h4 className="font-medium text-gray-900">Order #{order.id}</h4>
                    <p className="text-sm text-gray-600">{order.restaurant?.name}</p>
                    <p className="text-xs text-gray-500">
                      {new Date(order.orderTime).toLocaleString()}
                    </p>
                  </div>
                  <div className="text-right">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(order.status)} mb-1 inline-block`}>
                      {order.status.replace('_', ' ')}
                    </span>
                    <p className="font-medium text-primary-600">${order.totalAmount?.toFixed(2)}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Order Status Overview */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Order Status Overview</h2>
          
          <div className="space-y-4">
            {Object.entries(ordersByStatus).map(([status, count]) => (
              <div key={status} className="flex justify-between items-center">
                <div className="flex items-center">
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(status)} mr-3`}>
                    {status.replace('_', ' ')}
                  </span>
                </div>
                <span className="font-medium text-gray-900">{count}</span>
              </div>
            ))}
          </div>

          <div className="mt-6 pt-6 border-t border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Revenue Breakdown</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Total Revenue</span>
                <span className="font-medium">${todayRevenue.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Platform Fee (15%)</span>
                <span className="font-medium text-primary-600">${platformFee.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Restaurant Share</span>
                <span className="font-medium">${(todayRevenue - platformFee).toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Pending Restaurant Approvals */}
      {pendingRestaurants.length > 0 && (
        <div className="bg-white rounded-lg shadow-md p-6 mt-8">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Pending Restaurant Approvals</h2>
          
          <div className="space-y-4">
            {pendingRestaurants.map(restaurant => (
              <div key={restaurant.id} className="flex justify-between items-center p-4 border border-gray-200 rounded-lg">
                <div className="flex items-center space-x-4">
                  <img
                    src={restaurant.imageUrl || '/api/placeholder/60/60'}
                    alt={restaurant.name}
                    className="w-12 h-12 rounded-lg object-cover"
                  />
                  <div>
                    <h3 className="font-semibold text-gray-900">{restaurant.name}</h3>
                    <p className="text-sm text-gray-600">{restaurant.cuisine}</p>
                    <p className="text-xs text-gray-500">{restaurant.address}</p>
                  </div>
                </div>
                
                <div className="flex space-x-2">
                  <button className="btn-outline text-sm">
                    Review
                  </button>
                  <button className="btn-primary text-sm">
                    Approve
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
