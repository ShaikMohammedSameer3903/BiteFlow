import { createSlice } from '@reduxjs/toolkit';

const cartSlice = createSlice({
  name: 'cart',
  initialState: {
    items: [],
    restaurant: null,
    total: 0,
    deliveryFee: 2.99,
    tax: 0,
  },
  reducers: {
    addToCart: (state, action) => {
      const { item, restaurant } = action.payload;
      
      // If cart is empty or from different restaurant, clear cart
      if (state.items.length === 0 || state.restaurant?.id !== restaurant.id) {
        state.items = [];
        state.restaurant = restaurant;
      }
      
      const existingItem = state.items.find(cartItem => cartItem.id === item.id);
      
      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        state.items.push({ ...item, quantity: 1 });
      }
      
      cartSlice.caseReducers.calculateTotal(state);
    },
    
    removeFromCart: (state, action) => {
      const itemId = action.payload;
      state.items = state.items.filter(item => item.id !== itemId);
      
      if (state.items.length === 0) {
        state.restaurant = null;
      }
      
      cartSlice.caseReducers.calculateTotal(state);
    },
    
    updateQuantity: (state, action) => {
      const { itemId, quantity } = action.payload;
      const item = state.items.find(item => item.id === itemId);
      
      if (item) {
        if (quantity <= 0) {
          state.items = state.items.filter(item => item.id !== itemId);
        } else {
          item.quantity = quantity;
        }
      }
      
      if (state.items.length === 0) {
        state.restaurant = null;
      }
      
      cartSlice.caseReducers.calculateTotal(state);
    },
    
    clearCart: (state) => {
      state.items = [];
      state.restaurant = null;
      state.total = 0;
      state.tax = 0;
    },
    
    calculateTotal: (state) => {
      const subtotal = state.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
      state.tax = subtotal * 0.08; // 8% tax
      state.total = subtotal + state.tax + (state.items.length > 0 ? state.deliveryFee : 0);
    },
  },
});

export const { addToCart, removeFromCart, updateQuantity, clearCart } = cartSlice.actions;
export default cartSlice.reducer;
