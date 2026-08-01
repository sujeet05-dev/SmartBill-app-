import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { MainLayout } from '@/components/layout/MainLayout';
import { Login } from '@/pages/Login';
import { Register } from '@/pages/Register';
import { ShopProfile } from '@/pages/ShopProfile';
import { Inventory } from '@/pages/Inventory';
import { InvoiceList } from '@/pages/InvoiceList';
import { MonthlyInvoices } from '@/pages/MonthlyInvoices';
import { CreateInvoice } from '@/pages/CreateInvoice';
import { Toaster } from 'react-hot-toast';

// Placeholder for the Dashboard content
const Dashboard = () => (
  <div className="bg-white rounded-lg shadow p-6">
    <h2 className="text-2xl font-bold mb-4">Dashboard Overview</h2>
    <p className="text-slate-600">Welcome to SmartBill Admin Dashboard. Navigate using the sidebar.</p>
  </div>
);

function App() {
  return (
    <AuthProvider>
      <Toaster position="top-right" />
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          
          <Route path="/" element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }>
            <Route index element={<Dashboard />} />
            <Route path="shop" element={<ShopProfile />} />
            <Route path="inventory" element={<Inventory />} />
            <Route path="invoices" element={<InvoiceList />} />
            <Route path="monthly-invoices" element={<MonthlyInvoices />} />
            <Route path="invoices/new" element={<CreateInvoice />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
