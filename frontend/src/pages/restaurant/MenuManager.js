import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchMenuItems, createMenuItem, updateMenuItem, deleteMenuItem } from '../../store/slices/restaurantSlice';
import { fetchRestaurants } from '../../store/slices/restaurantSlice';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const MenuManager = () => {
  const dispatch = useDispatch();
  const { user } = useSelector(state => state.auth);
  const { menuItems, restaurants, isLoading, error } = useSelector(state => state.restaurant);
  
  const [showModal, setShowModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    category: '',
    available: true,
    imageUrl: ''
  });

  const myRestaurant = restaurants.find(r => r.ownerId === user?.id);

  useEffect(() => {
    dispatch(fetchRestaurants({ ownerId: user?.id }));
  }, [dispatch, user?.id]);

  useEffect(() => {
    if (myRestaurant?.id) {
      dispatch(fetchMenuItems(myRestaurant.id));
    }
  }, [dispatch, myRestaurant?.id]);

  const categories = [...new Set(menuItems.map(item => item.category))];

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const itemData = {
      ...formData,
      price: parseFloat(formData.price)
    };

    if (editingItem) {
      await dispatch(updateMenuItem({
        restaurantId: myRestaurant.id,
        itemId: editingItem.id,
        menuItem: itemData
      }));
    } else {
      await dispatch(createMenuItem({
        restaurantId: myRestaurant.id,
        menuItem: itemData
      }));
    }

    setShowModal(false);
    setEditingItem(null);
    setFormData({
      name: '',
      description: '',
      price: '',
      category: '',
      available: true,
      imageUrl: ''
    });
  };

  const handleEdit = (item) => {
    setEditingItem(item);
    setFormData({
      name: item.name,
      description: item.description,
      price: item.price.toString(),
      category: item.category,
      available: item.available,
      imageUrl: item.imageUrl || ''
    });
    setShowModal(true);
  };

  const handleDelete = async (itemId) => {
    if (window.confirm('Are you sure you want to delete this menu item?')) {
      await dispatch(deleteMenuItem({
        restaurantId: myRestaurant.id,
        itemId
      }));
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  if (!myRestaurant) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">Restaurant not found</h2>
          <p className="text-gray-600">Please set up your restaurant profile first.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Menu Manager</h1>
          <p className="text-gray-600">Manage your restaurant's menu items</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="btn-primary"
        >
          Add New Item
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md mb-6">
          {error}
        </div>
      )}

      {/* Menu Items */}
      <div className="bg-white rounded-lg shadow-md p-6">
        {isLoading ? (
          <LoadingSpinner />
        ) : menuItems.length === 0 ? (
          <div className="text-center py-8">
            <svg className="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2v2M7 7h10" />
            </svg>
            <h3 className="text-lg font-medium text-gray-900 mb-2">No menu items yet</h3>
            <p className="text-gray-600 mb-4">Add your first menu item to get started</p>
            <button
              onClick={() => setShowModal(true)}
              className="btn-primary"
            >
              Add Menu Item
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {categories.map(category => (
              <div key={category} className="border-b border-gray-200 pb-6 last:border-b-0">
                <h2 className="text-xl font-semibold text-gray-900 mb-4">{category}</h2>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {menuItems
                    .filter(item => item.category === category)
                    .map(item => (
                      <div key={item.id} className="border border-gray-200 rounded-lg p-4">
                        <div className="flex justify-between items-start mb-3">
                          <div className="flex-1">
                            <h3 className="font-semibold text-gray-900">{item.name}</h3>
                            <p className="text-sm text-gray-600 mb-2">{item.description}</p>
                            <p className="text-lg font-bold text-primary-600">${item.price}</p>
                          </div>
                          {item.imageUrl && (
                            <img
                              src={item.imageUrl}
                              alt={item.name}
                              className="w-16 h-16 rounded-lg object-cover ml-3"
                            />
                          )}
                        </div>
                        
                        <div className="flex justify-between items-center">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                            item.available 
                              ? 'bg-green-100 text-green-800' 
                              : 'bg-red-100 text-red-800'
                          }`}>
                            {item.available ? 'Available' : 'Unavailable'}
                          </span>
                          
                          <div className="flex space-x-2">
                            <button
                              onClick={() => handleEdit(item)}
                              className="text-primary-600 hover:text-primary-700 text-sm font-medium"
                            >
                              Edit
                            </button>
                            <button
                              onClick={() => handleDelete(item.id)}
                              className="text-red-600 hover:text-red-700 text-sm font-medium"
                            >
                              Delete
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
            <div className="mt-3">
              <h3 className="text-lg font-medium text-gray-900 mb-4">
                {editingItem ? 'Edit Menu Item' : 'Add New Menu Item'}
              </h3>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Name
                  </label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    className="input-field"
                    placeholder="Item name"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Description
                  </label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows={3}
                    className="input-field"
                    placeholder="Item description"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Price ($)
                    </label>
                    <input
                      type="number"
                      name="price"
                      value={formData.price}
                      onChange={handleChange}
                      step="0.01"
                      min="0"
                      required
                      className="input-field"
                      placeholder="0.00"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Category
                    </label>
                    <input
                      type="text"
                      name="category"
                      value={formData.category}
                      onChange={handleChange}
                      required
                      className="input-field"
                      placeholder="e.g., Appetizers"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Image URL (optional)
                  </label>
                  <input
                    type="url"
                    name="imageUrl"
                    value={formData.imageUrl}
                    onChange={handleChange}
                    className="input-field"
                    placeholder="https://example.com/image.jpg"
                  />
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    name="available"
                    checked={formData.available}
                    onChange={handleChange}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                  />
                  <label className="ml-2 block text-sm text-gray-900">
                    Available for ordering
                  </label>
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => {
                      setShowModal(false);
                      setEditingItem(null);
                      setFormData({
                        name: '',
                        description: '',
                        price: '',
                        category: '',
                        available: true,
                        imageUrl: ''
                      });
                    }}
                    className="btn-outline"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="btn-primary disabled:opacity-50"
                  >
                    {isLoading ? 'Saving...' : (editingItem ? 'Update' : 'Add')} Item
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MenuManager;
