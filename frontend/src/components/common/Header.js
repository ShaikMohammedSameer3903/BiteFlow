import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../../store/slices/authSlice';

const Header = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user, token } = useSelector(state => state.auth);
  const { items } = useSelector(state => state.cart);
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/');
  };

  const getDashboardLink = () => {
    if (!user) return '/';
    switch (user.role) {
      case 'CUSTOMER':
        return '/customer/dashboard';
      case 'RESTAURANT':
        return '/restaurant/dashboard';
      case 'DELIVERY':
        return '/delivery/dashboard';
      case 'ADMIN':
        return '/admin/dashboard';
      default:
        return '/';
    }
  };

  const cartItemCount = items.reduce((total, item) => total + item.quantity, 0);

  return (
    <header className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-lg">F</span>
            </div>
            <span className="text-xl font-bold text-gray-900">FoodDelivery</span>
          </Link>

          {/* Navigation */}
          <nav className="hidden md:flex items-center space-x-8">
            <Link to="/restaurants" className="text-gray-700 hover:text-primary-600 transition duration-200">
              Restaurants
            </Link>
            
            {user && token ? (
              <>
                <Link to={getDashboardLink()} className="text-gray-700 hover:text-primary-600 transition duration-200">
                  Dashboard
                </Link>
                
                {user.role === 'CUSTOMER' && (
                  <Link to="/cart" className="relative text-gray-700 hover:text-primary-600 transition duration-200">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 1.5M7 13l-1.5-1.5m0 0L4 15m0 0h3m6 0h3m-3 0v-2a2 2 0 00-2-2H9a2 2 0 00-2 2v2" />
                    </svg>
                    {cartItemCount > 0 && (
                      <span className="absolute -top-2 -right-2 bg-accent-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                        {cartItemCount}
                      </span>
                    )}
                  </Link>
                )}
                
                <div className="relative">
                  <button
                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                    className="flex items-center space-x-2 text-gray-700 hover:text-primary-600 transition duration-200"
                  >
                    <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                      <span className="text-primary-600 font-medium text-sm">
                        {user.firstName?.charAt(0) || user.email?.charAt(0)}
                      </span>
                    </div>
                    <span className="hidden lg:block">{user.firstName || user.email}</span>
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </button>
                  
                  {isMenuOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50">
                      <Link
                        to="/profile"
                        className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                        onClick={() => setIsMenuOpen(false)}
                      >
                        Profile
                      </Link>
                      <button
                        onClick={handleLogout}
                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        Sign out
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center space-x-4">
                <Link to="/login" className="text-gray-700 hover:text-primary-600 transition duration-200">
                  Sign in
                </Link>
                <Link to="/register" className="btn-primary">
                  Sign up
                </Link>
              </div>
            )}
          </nav>

          {/* Mobile menu button */}
          <button
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            className="md:hidden p-2 rounded-md text-gray-700 hover:text-primary-600 hover:bg-gray-100"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>

        {/* Mobile menu */}
        {isMenuOpen && (
          <div className="md:hidden py-4 border-t border-gray-200">
            <div className="flex flex-col space-y-4">
              <Link
                to="/restaurants"
                className="text-gray-700 hover:text-primary-600 transition duration-200"
                onClick={() => setIsMenuOpen(false)}
              >
                Restaurants
              </Link>
              
              {user && token ? (
                <>
                  <Link
                    to={getDashboardLink()}
                    className="text-gray-700 hover:text-primary-600 transition duration-200"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Dashboard
                  </Link>
                  
                  {user.role === 'CUSTOMER' && (
                    <Link
                      to="/cart"
                      className="text-gray-700 hover:text-primary-600 transition duration-200 flex items-center space-x-2"
                      onClick={() => setIsMenuOpen(false)}
                    >
                      <span>Cart</span>
                      {cartItemCount > 0 && (
                        <span className="bg-accent-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                          {cartItemCount}
                        </span>
                      )}
                    </Link>
                  )}
                  
                  <Link
                    to="/profile"
                    className="text-gray-700 hover:text-primary-600 transition duration-200"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Profile
                  </Link>
                  
                  <button
                    onClick={handleLogout}
                    className="text-left text-gray-700 hover:text-primary-600 transition duration-200"
                  >
                    Sign out
                  </button>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    className="text-gray-700 hover:text-primary-600 transition duration-200"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Sign in
                  </Link>
                  <Link
                    to="/register"
                    className="btn-primary inline-block text-center"
                    onClick={() => setIsMenuOpen(false)}
                  >
                    Sign up
                  </Link>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header;
