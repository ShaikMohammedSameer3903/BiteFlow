import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchOrders } from '../../store/slices/orderSlice';
import { fetchRestaurants } from '../../store/slices/restaurantSlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const Analytics = () => {
  const dispatch = useDispatch();
  const { orders, isLoading: ordersLoading } = useSelector(state => state.order);
  const { restaurants, isLoading: restaurantsLoading } = useSelector(state => state.restaurant);
  const [timeRange, setTimeRange] = useState('7days');

  useEffect(() => {
    dispatch(fetchOrders());
    dispatch(fetchRestaurants());
  }, [dispatch]);

  const getFilteredOrders = () => {
    const now = new Date();
    const cutoffDate = new Date();
    
    switch (timeRange) {
      case '24hours':
        cutoffDate.setHours(now.getHours() - 24);
        break;
      case '7days':
        cutoffDate.setDate(now.getDate() - 7);
        break;
      case '30days':
        cutoffDate.setDate(now.getDate() - 30);
        break;
      case '90days':
        cutoffDate.setDate(now.getDate() - 90);
        break;
      default:
        return orders;
    }
    
    return orders.filter(order => new Date(order.orderTime) >= cutoffDate);
  };

  const filteredOrders = getFilteredOrders();
  const totalRevenue = filteredOrders.reduce((sum, order) => sum + (order.totalAmount || 0), 0);
  const platformFee = totalRevenue * 0.15;
  const averageOrderValue = filteredOrders.length > 0 ? totalRevenue / filteredOrders.length : 0;

  const ordersByStatus = filteredOrders.reduce((acc, order) => {
    acc[order.status] = (acc[order.status] || 0) + 1;
    return acc;
  }, {});

  const topRestaurants = restaurants
    .map(restaurant => ({
      ...restaurant,
      orderCount: filteredOrders.filter(order => order.restaurantId === restaurant.id).length,
      revenue: filteredOrders
        .filter(order => order.restaurantId === restaurant.id)
        .reduce((sum, order) => sum + (order.totalAmount || 0), 0)
    }))
    .sort((a, b) => b.revenue - a.revenue)
    .slice(0, 5);

  const ordersByDay = filteredOrders.reduce((acc, order) => {
    const day = new Date(order.orderTime).toLocaleDateString();
    acc[day] = (acc[day] || 0) + 1;
    return acc;
  }, {});

  const revenueByDay = filteredOrders.reduce((acc, order) => {
    const day = new Date(order.orderTime).toLocaleDateString();
    acc[day] = (acc[day] || 0) + (order.totalAmount || 0);
    return acc;
  }, {});

  const isLoading = ordersLoading || restaurantsLoading;

  if (isLoading) {
    return <LoadingSpinner text="Loading analytics..." />;
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Analytics Dashboard</h1>
          <p className="text-gray-600">Platform performance and insights</p>
        </div>
        
        <select
          value={timeRange}
          onChange={(e) => setTimeRange(e.target.value)}
          className="input-field w-40"
        >
          <option value="24hours">Last 24 Hours</option>
          <option value="7days">Last 7 Days</option>
          <option value="30days">Last 30 Days</option>
          <option value="90days">Last 90 Days</option>
        </select>
      </div>

      {/* Key Metrics */}
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
              <p className="text-2xl font-bold text-gray-900">{filteredOrders.length}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-green-100 text-green-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinecap="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Total Revenue</p>
              <p className="text-2xl font-bold text-gray-900">${totalRevenue.toFixed(2)}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-purple-100 text-purple-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Avg Order Value</p>
              <p className="text-2xl font-bold text-gray-900">${averageOrderValue.toFixed(2)}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center">
            <div className="p-3 rounded-full bg-yellow-100 text-yellow-600 mr-4">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-gray-600">Platform Earnings</p>
              <p className="text-2xl font-bold text-gray-900">${platformFee.toFixed(2)}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        {/* Order Status Distribution */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Order Status Distribution</h2>
          
          <div className="space-y-4">
            {Object.entries(ordersByStatus).map(([status, count]) => {
              const percentage = ((count / filteredOrders.length) * 100).toFixed(1);
              return (
                <div key={status}>
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-sm font-medium text-gray-700">
                      {status.replace('_', ' ')}
                    </span>
                    <span className="text-sm text-gray-600">{count} ({percentage}%)</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-primary-600 h-2 rounded-full"
                      style={{ width: `${percentage}%` }}
                    ></div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Top Performing Restaurants */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Top Performing Restaurants</h2>
          
          <div className="space-y-4">
            {topRestaurants.map((restaurant, index) => (
              <div key={restaurant.id} className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <span className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center text-primary-600 font-bold text-sm">
                    {index + 1}
                  </span>
                  <div>
                    <h3 className="font-medium text-gray-900">{restaurant.name}</h3>
                    <p className="text-sm text-gray-600">{restaurant.orderCount} orders</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-bold text-primary-600">${restaurant.revenue.toFixed(2)}</p>
                  <p className="text-xs text-gray-500">Revenue</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Orders by Day */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Orders by Day</h2>
          
          <div className="space-y-3">
            {Object.entries(ordersByDay)
              .sort(([a], [b]) => new Date(a) - new Date(b))
              .slice(-7)
              .map(([day, count]) => {
                const maxCount = Math.max(...Object.values(ordersByDay));
                const percentage = (count / maxCount) * 100;
                
                return (
                  <div key={day}>
                    <div className="flex justify-between items-center mb-1">
                      <span className="text-sm font-medium text-gray-700">{day}</span>
                      <span className="text-sm text-gray-600">{count} orders</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-blue-600 h-2 rounded-full"
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                  </div>
                );
              })}
          </div>
        </div>

        {/* Revenue by Day */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Revenue by Day</h2>
          
          <div className="space-y-3">
            {Object.entries(revenueByDay)
              .sort(([a], [b]) => new Date(a) - new Date(b))
              .slice(-7)
              .map(([day, revenue]) => {
                const maxRevenue = Math.max(...Object.values(revenueByDay));
                const percentage = (revenue / maxRevenue) * 100;
                
                return (
                  <div key={day}>
                    <div className="flex justify-between items-center mb-1">
                      <span className="text-sm font-medium text-gray-700">{day}</span>
                      <span className="text-sm text-gray-600">${revenue.toFixed(2)}</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-green-600 h-2 rounded-full"
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                  </div>
                );
              })}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Analytics;
