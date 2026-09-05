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
  availableImeis?: string[];
  unit?: string;
  hsnCode?: string;
}

let cachedProducts: Product[] | null = null;
let lastCacheTime = 0;
const CACHE_TTL_MS = 30000; // 30 seconds

export const productService = {
  clearCache: () => {
    cachedProducts = null;
    lastCacheTime = 0;
  },

  getAllProducts: async (search?: string, forceRefresh = false): Promise<Product[]> => {
    // If no search and not forcing refresh, use cache if still fresh
    if (!search && !forceRefresh && cachedProducts && (Date.now() - lastCacheTime < CACHE_TTL_MS)) {
      return cachedProducts;
    }

    const params = search ? { search } : {};
    const response = await api.get('/products', { params });
    
    if (!search) {
      cachedProducts = response.data;
      lastCacheTime = Date.now();
    }
    return response.data;
  },

  createProduct: async (data: Product) => {
    productService.clearCache();
    const response = await api.post('/products', data);
    return response.data;
  },

  updateProduct: async (id: number, data: Product) => {
    productService.clearCache();
    const response = await api.put(`/products/${id}`, data);
    return response.data;
  },

  deleteProduct: async (id: number) => {
    productService.clearCache();
    const response = await api.delete(`/products/${id}`);
    return response.data;
  },
};
