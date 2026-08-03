import api from './api';

export interface Product {
  id?: number;
  name: string;
  description?: string;
  brand: string;
  category: string;
  price: number;
  gstPercentage: number;
  stock: number;
  sku?: string;
  imeiNumber?: string;
  unit?: string;
}

export const productService = {
  getAllProducts: async (search?: string) => {
    const params = search ? { search } : {};
    const response = await api.get('/products', { params });
    return response.data;
  },

  createProduct: async (data: Product) => {
    const response = await api.post('/products', data);
    return response.data;
  },

  updateProduct: async (id: number, data: Product) => {
    const response = await api.put(`/products/${id}`, data);
    return response.data;
  },

  deleteProduct: async (id: number) => {
    const response = await api.delete(`/products/${id}`);
    return response.data;
  },
};
