import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// Async thunks
export const fetchDeliveries = createAsyncThunk(
  'delivery/fetchDeliveries',
  async (params = {}, { rejectWithValue }) => {
    try {
      const response = await api.get('/api/deliveries', { params });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch deliveries');
    }
  }
);

export const fetchDeliveryById = createAsyncThunk(
  'delivery/fetchDeliveryById',
  async (deliveryId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/api/deliveries/${deliveryId}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to fetch delivery');
    }
  }
);

export const acceptDelivery = createAsyncThunk(
  'delivery/acceptDelivery',
  async (deliveryId, { rejectWithValue }) => {
    try {
      const response = await api.put(`/api/deliveries/${deliveryId}/accept`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to accept delivery');
    }
  }
);

export const updateDeliveryStatus = createAsyncThunk(
  'delivery/updateDeliveryStatus',
  async ({ deliveryId, status }, { rejectWithValue }) => {
    try {
      const response = await api.put(`/api/deliveries/${deliveryId}/status`, { status });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to update delivery status');
    }
  }
);

export const updateLocation = createAsyncThunk(
  'delivery/updateLocation',
  async ({ deliveryId, latitude, longitude }, { rejectWithValue }) => {
    try {
      const response = await api.put(`/api/deliveries/${deliveryId}/location`, {
        latitude,
        longitude
      });
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || 'Failed to update location');
    }
  }
);

const deliverySlice = createSlice({
  name: 'delivery',
  initialState: {
    deliveries: [],
    currentDelivery: null,
    availableDeliveries: [],
    isLoading: false,
    error: null,
    currentLocation: null,
  },
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setCurrentLocation: (state, action) => {
      state.currentLocation = action.payload;
    },
    clearCurrentDelivery: (state) => {
      state.currentDelivery = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Deliveries
      .addCase(fetchDeliveries.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDeliveries.fulfilled, (state, action) => {
        state.isLoading = false;
        state.deliveries = action.payload;
        state.error = null;
      })
      .addCase(fetchDeliveries.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Fetch Delivery By ID
      .addCase(fetchDeliveryById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchDeliveryById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.currentDelivery = action.payload;
        state.error = null;
      })
      .addCase(fetchDeliveryById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Accept Delivery
      .addCase(acceptDelivery.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(acceptDelivery.fulfilled, (state, action) => {
        state.isLoading = false;
        const acceptedDelivery = action.payload;
        state.currentDelivery = acceptedDelivery;
        state.deliveries.push(acceptedDelivery);
        state.availableDeliveries = state.availableDeliveries.filter(
          delivery => delivery.id !== acceptedDelivery.id
        );
        state.error = null;
      })
      .addCase(acceptDelivery.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Update Delivery Status
      .addCase(updateDeliveryStatus.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateDeliveryStatus.fulfilled, (state, action) => {
        state.isLoading = false;
        const updatedDelivery = action.payload;
        const index = state.deliveries.findIndex(delivery => delivery.id === updatedDelivery.id);
        if (index !== -1) {
          state.deliveries[index] = updatedDelivery;
        }
        if (state.currentDelivery?.id === updatedDelivery.id) {
          state.currentDelivery = updatedDelivery;
        }
        state.error = null;
      })
      .addCase(updateDeliveryStatus.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Update Location
      .addCase(updateLocation.pending, (state) => {
        state.error = null;
      })
      .addCase(updateLocation.fulfilled, (state, action) => {
        const updatedDelivery = action.payload;
        const index = state.deliveries.findIndex(delivery => delivery.id === updatedDelivery.id);
        if (index !== -1) {
          state.deliveries[index] = updatedDelivery;
        }
        if (state.currentDelivery?.id === updatedDelivery.id) {
          state.currentDelivery = updatedDelivery;
        }
        state.error = null;
      })
      .addCase(updateLocation.rejected, (state, action) => {
        state.error = action.payload;
      });
  },
});

export const { clearError, setCurrentLocation, clearCurrentDelivery } = deliverySlice.actions;
export default deliverySlice.reducer;
