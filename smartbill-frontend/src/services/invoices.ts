import api from './api';
import { type Product } from './products';

export interface InvoiceItemCreate {
  productId: number;
  quantity: number;
}

export interface InvoiceCreate {
  customerName?: string;
  customerMobile?: string;
  paymentMethod: 'CASH' | 'CARD' | 'UPI';
  items: InvoiceItemCreate[];
}

export interface InvoiceItemResponse {
  id: number;
  product: Product;
  quantity: number;
  unitPrice: number;
  gstPercentage: number;
  gstAmount: number;
  totalAmount: number;
}

export interface InvoiceResponse {
  id: number;
  invoiceNumber: string;
  date: string;
  customerName: string;
  customerMobile: string;
  paymentMethod: string;
  subTotal: number;
  totalGst: number;
  grandTotal: number;
  items: InvoiceItemResponse[];
}

export const invoiceService = {
  getAllInvoices: async (search?: string) => {
    const params = search ? { search } : {};
    const response = await api.get('/invoices', { params });
    return response.data;
  },

  getInvoiceById: async (id: number) => {
    const response = await api.get(`/invoices/${id}`);
    return response.data;
  },

  createInvoice: async (data: InvoiceCreate) => {
    const response = await api.post('/invoices', data);
    return response.data;
  },

  downloadPdf: async (id: number) => {
    const response = await api.get(`/invoices/${id}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },

  deleteInvoice: async (id: number) => {
    const response = await api.delete(`/invoices/${id}`);
    return response.data;
  },
};
