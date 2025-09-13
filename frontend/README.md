# Food Delivery Platform - Frontend

A modern React-based frontend for the Food Delivery Platform with role-based interfaces for customers, restaurants, delivery drivers, and administrators.

## Features

### Customer Interface
- Browse restaurants and menus
- Add items to cart and place orders
- Real-time order tracking
- User profile management
- Order history

### Restaurant Interface
- Restaurant dashboard with order management
- Menu item management (CRUD operations)
- Order status updates
- Sales analytics

### Delivery Interface
- Delivery dashboard
- Accept and manage deliveries
- Real-time location updates
- Earnings tracking

### Admin Interface
- Platform overview and analytics
- Restaurant approval management
- User management
- Revenue tracking

## Tech Stack

- **React 18** - Frontend framework
- **Redux Toolkit** - State management
- **React Router** - Navigation
- **Tailwind CSS** - Styling
- **Axios** - HTTP client

## Getting Started

### Prerequisites
- Node.js 16+ and npm
- Backend services running on port 8080

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create environment file:
```bash
cp .env.example .env
```

3. Update environment variables in `.env`:
```
REACT_APP_API_URL=http://localhost:8080
REACT_APP_STRIPE_PUBLISHABLE_KEY=your_stripe_key
REACT_APP_GOOGLE_MAPS_API_KEY=your_google_maps_key
```

4. Start the development server:
```bash
npm start
```

The application will be available at `http://localhost:3000`.

## Available Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm run eject` - Eject from Create React App

## Project Structure

```
src/
├── components/          # Reusable components
│   └── common/         # Common UI components
├── pages/              # Page components
│   ├── auth/          # Authentication pages
│   ├── customer/      # Customer interface
│   ├── restaurant/    # Restaurant interface
│   ├── delivery/      # Delivery interface
│   └── admin/         # Admin interface
├── store/             # Redux store
│   └── slices/        # Redux slices
├── services/          # API services
└── App.js             # Main app component
```

## Authentication

The app uses JWT-based authentication with role-based access control:
- **CUSTOMER** - Access to customer features
- **RESTAURANT** - Access to restaurant management
- **DELIVERY** - Access to delivery features
- **ADMIN** - Access to admin panel

## API Integration

The frontend communicates with the backend microservices through the API Gateway at `http://localhost:8080`. All requests are automatically authenticated using JWT tokens stored in localStorage.

## Styling

The app uses Tailwind CSS for styling with a custom design system:
- Primary colors: Blue palette
- Secondary colors: Green palette
- Accent colors: Red palette

Custom CSS classes are defined in `src/index.css` for common UI patterns.

## State Management

Redux Toolkit is used for state management with the following slices:
- `authSlice` - Authentication state
- `cartSlice` - Shopping cart state
- `orderSlice` - Order management
- `restaurantSlice` - Restaurant data
- `deliverySlice` - Delivery management

## Demo Accounts

For testing purposes, demo accounts are available:
- Customer: `customer@demo.com` / `password`
- Restaurant: `restaurant@demo.com` / `password`
- Delivery: `delivery@demo.com` / `password`
- Admin: `admin@demo.com` / `password`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
