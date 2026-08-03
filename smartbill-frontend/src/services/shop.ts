import api from './api';

export interface Shop {
  id?: number;
  name: string;
  ownerName: string;
  address: string;
  state?: string;
  pincode?: string;
  phone: string;
  email?: string;
  gstin: string;
  logoUrl?: string;
  termsAndConditions?: string;
}

export const shopService = {
  getShop: async () => {
    const response = await api.get('/shop');
    return response.data;
  },

  updateShop: async (data: Shop) => {
    const response = await api.put('/shop', data);
    return response.data;
  },
};
