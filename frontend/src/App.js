import React, { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { validateToken } from './store/slices/authSlice';

// Components
import Header from './components/common/Header';
import Footer from './components/common/Footer';
import LoadingSpinner from './components/common/LoadingSpinner';

// Pages
import Home from './pages/Home';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import BrowseRestaurants from './pages/customer/BrowseRestaurants';
import RestaurantDetails from './pages/customer/RestaurantDetails';
import Cart from './pages/customer/Cart';
import OrderTracking from './pages/customer/OrderTracking';
import CustomerDashboard from './pages/customer/Dashboard';
import RestaurantDashboard from './pages/restaurant/Dashboard';
import MenuManager from './pages/restaurant/MenuManager';
import DeliveryDashboard from './pages/delivery/Dashboard';
import AdminDashboard from './pages/admin/Dashboard';
import Analytics from './pages/admin/Analytics';

function App() {
  const dispatch = useDispatch();
  const { user, isLoading, token } = useSelector(state => state.auth);

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    if (storedToken && !user) {
      dispatch(validateToken(storedToken));
    }
  }, [dispatch, user]);

  if (isLoading) {
    return <LoadingSpinner />;
  }

  const ProtectedRoute = ({ children, allowedRoles = [] }) => {
    if (!user || !token) {
      return <Navigate to="/login" replace />;
    }
    
    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
      return <Navigate to="/" replace />;
    }
    
    return children;
  };

  const PublicRoute = ({ children }) => {
    if (user && token) {
      // Redirect based on user role
      switch (user.role) {
        case 'CUSTOMER':
          return <Navigate to="/customer/dashboard" replace />;
        case 'RESTAURANT':
          return <Navigate to="/restaurant/dashboard" replace />;
        case 'DELIVERY':
          return <Navigate to="/delivery/dashboard" replace />;
        case 'ADMIN':
          return <Navigate to="/admin/dashboard" replace />;
        default:
          return <Navigate to="/" replace />;
      }
    }
    return children;
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />
      <main className="flex-grow">
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Home />} />
          <Route path="/restaurants" element={<BrowseRestaurants />} />
          <Route path="/restaurants/:id" element={<RestaurantDetails />} />
          
          {/* Auth Routes */}
          <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
          <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
          
          {/* Customer Routes */}
          <Route path="/customer/dashboard" element={
            <ProtectedRoute allowedRoles={['CUSTOMER']}>
              <CustomerDashboard />
            </ProtectedRoute>
          } />
          <Route path="/cart" element={
            <ProtectedRoute allowedRoles={['CUSTOMER']}>
              <Cart />
            </ProtectedRoute>
          } />
          <Route path="/orders/:id/track" element={
            <ProtectedRoute allowedRoles={['CUSTOMER']}>
              <OrderTracking />
            </ProtectedRoute>
          } />
          
          {/* Restaurant Routes */}
          <Route path="/restaurant/dashboard" element={
            <ProtectedRoute allowedRoles={['RESTAURANT']}>
              <RestaurantDashboard />
            </ProtectedRoute>
          } />
          <Route path="/restaurant/menu" element={
            <ProtectedRoute allowedRoles={['RESTAURANT']}>
              <MenuManager />
            </ProtectedRoute>
          } />
          
          {/* Delivery Routes */}
          <Route path="/delivery/dashboard" element={
            <ProtectedRoute allowedRoles={['DELIVERY']}>
              <DeliveryDashboard />
            </ProtectedRoute>
          } />
          
          {/* Admin Routes */}
          <Route path="/admin/dashboard" element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AdminDashboard />
            </ProtectedRoute>
          } />
          <Route path="/admin/analytics" element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <Analytics />
            </ProtectedRoute>
          } />
          
          {/* Catch all route */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default App;
