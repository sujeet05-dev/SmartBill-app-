import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { MainLayout } from '@/components/layout/MainLayout';
import { Login } from '@/pages/Login';
import { Register } from '@/pages/Register';
import { ShopProfile } from '@/pages/ShopProfile';
import { Inventory } from '@/pages/Inventory';
import { InvoiceList } from '@/pages/InvoiceList';
import { NonGstInvoiceList } from '@/pages/NonGstInvoiceList';
import { MonthlyInvoices } from '@/pages/MonthlyInvoices';
import { CreateInvoice } from '@/pages/CreateInvoice';
import { CreateNonGstInvoice } from '@/pages/CreateNonGstInvoice';
import { Toaster } from 'react-hot-toast';

import { Dashboard } from '@/pages/Dashboard';

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
            <Route path="non-gst-invoices" element={<NonGstInvoiceList />} />
            <Route path="monthly-invoices" element={<MonthlyInvoices />} />
            <Route path="invoices/new" element={<CreateInvoice />} />
            <Route path="non-gst-invoices/new" element={<CreateNonGstInvoice />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
