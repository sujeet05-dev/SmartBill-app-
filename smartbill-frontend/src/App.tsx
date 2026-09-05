import { lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { MainLayout } from '@/components/layout/MainLayout';
import { Login } from '@/pages/Login';
import { Register } from '@/pages/Register';
import { Dashboard } from '@/pages/Dashboard';
import { Toaster } from 'react-hot-toast';

// Lazy-load non-critical routes to minimize initial bundle size and speed up login/dashboard
const ShopProfile = lazy(() => import('@/pages/ShopProfile').then((m) => ({ default: m.ShopProfile })));
const Inventory = lazy(() => import('@/pages/Inventory').then((m) => ({ default: m.Inventory })));
const InvoiceList = lazy(() => import('@/pages/InvoiceList').then((m) => ({ default: m.InvoiceList })));
const NonGstInvoiceList = lazy(() => import('@/pages/NonGstInvoiceList').then((m) => ({ default: m.NonGstInvoiceList })));
const MonthlyInvoices = lazy(() => import('@/pages/MonthlyInvoices').then((m) => ({ default: m.MonthlyInvoices })));
const CreateInvoice = lazy(() => import('@/pages/CreateInvoice').then((m) => ({ default: m.CreateInvoice })));
const CreateNonGstInvoice = lazy(() => import('@/pages/CreateNonGstInvoice').then((m) => ({ default: m.CreateNonGstInvoice })));

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
