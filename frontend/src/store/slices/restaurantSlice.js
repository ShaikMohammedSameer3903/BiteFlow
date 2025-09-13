import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// Async thunks
export const fetchRestaurants = createAsyncThunk(
  'restaurant/fetchRestaurants',
  async (params = {}, { rejectWithValue }) => {
    try {
      const response = await api.get('/api/restaurants', { params });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch restaurants');
    }
  }
);

export const fetchRestaurantById = createAsyncThunk(
  'restaurant/fetchRestaurantById',
  async (restaurantId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/api/restaurants/${restaurantId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch restaurant');
    }
  }
);

export const fetchMenuItems = createAsyncThunk(
  'restaurant/fetchMenuItems',
  async (restaurantId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/api/restaurants/${restaurantId}/menu`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch menu items');
    }
  }
);

export const createMenuItem = createAsyncThunk(
  'restaurant/createMenuItem',
  async ({ restaurantId, menuItem }, { rejectWithValue }) => {
    try {
      const response = await api.post(`/api/restaurants/${restaurantId}/menu`, menuItem);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to create menu item');
    }
  }
);

export const updateMenuItem = createAsyncThunk(
  'restaurant/updateMenuItem',
  async ({ restaurantId, itemId, menuItem }, { rejectWithValue }) => {
    try {
      const response = await api.put(`/api/restaurants/${restaurantId}/menu/${itemId}`, menuItem);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to update menu item');
    }
  }
);

export const deleteMenuItem = createAsyncThunk(
  'restaurant/deleteMenuItem',
  async ({ restaurantId, itemId }, { rejectWithValue }) => {
    try {
      await api.delete(`/api/restaurants/${restaurantId}/menu/${itemId}`);
      return itemId;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to delete menu item');
    }
  }
);

const restaurantSlice = createSlice({
  name: 'restaurant',
  initialState: {
    restaurants: [],
    currentRestaurant: null,
    menuItems: [],
    isLoading: false,
    error: null,
    searchQuery: '',
    filters: {
      cuisine: '',
      rating: 0,
      priceRange: '',
    },
  },
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setSearchQuery: (state, action) => {
      state.searchQuery = action.payload;
    },
    setFilters: (state, action) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearCurrentRestaurant: (state) => {
      state.currentRestaurant = null;
      state.menuItems = [];
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Restaurants
      .addCase(fetchRestaurants.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchRestaurants.fulfilled, (state, action) => {
        state.isLoading = false;
        state.restaurants = action.payload;
        state.error = null;
      })
      .addCase(fetchRestaurants.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Fetch Restaurant By ID
      .addCase(fetchRestaurantById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchRestaurantById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.currentRestaurant = action.payload;
        state.error = null;
      })
      .addCase(fetchRestaurantById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Fetch Menu Items
      .addCase(fetchMenuItems.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchMenuItems.fulfilled, (state, action) => {
        state.isLoading = false;
        state.menuItems = action.payload;
        state.error = null;
      })
      .addCase(fetchMenuItems.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Create Menu Item
      .addCase(createMenuItem.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(createMenuItem.fulfilled, (state, action) => {
        state.isLoading = false;
        state.menuItems.push(action.payload);
        state.error = null;
      })
      .addCase(createMenuItem.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Update Menu Item
      .addCase(updateMenuItem.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateMenuItem.fulfilled, (state, action) => {
        state.isLoading = false;
        const updatedItem = action.payload;
        const index = state.menuItems.findIndex(item => item.id === updatedItem.id);
        if (index !== -1) {
          state.menuItems[index] = updatedItem;
        }
        state.error = null;
      })
      .addCase(updateMenuItem.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Delete Menu Item
      .addCase(deleteMenuItem.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteMenuItem.fulfilled, (state, action) => {
        state.isLoading = false;
        state.menuItems = state.menuItems.filter(item => item.id !== action.payload);
        state.error = null;
      })
      .addCase(deleteMenuItem.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      });
  },
});

export const { clearError, setSearchQuery, setFilters, clearCurrentRestaurant } = restaurantSlice.actions;
export default restaurantSlice.reducer;
