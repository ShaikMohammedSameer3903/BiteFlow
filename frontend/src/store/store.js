import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import cartReducer from './slices/cartSlice';
import orderReducer from './slices/orderSlice';
import restaurantReducer from './slices/restaurantSlice';
import deliveryReducer from './slices/deliverySlice';

const store = configureStore({
  reducer: {
    auth: authReducer,
    cart: cartReducer,
    order: orderReducer,
    restaurant: restaurantReducer,
    delivery: deliveryReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
});

export default store;
