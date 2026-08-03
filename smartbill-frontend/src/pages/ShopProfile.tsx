import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { shopService, type Shop } from '@/services/shop';

const shopSchema = z.object({
  name: z.string().min(1, 'Shop Name is required'),
  ownerName: z.string().min(1, 'Owner Name is required'),
  address: z.string().min(1, 'Address is required'),
  state: z.string().optional(),
  pincode: z.string().optional(),
  phone: z.string().min(10, 'Valid phone number is required'),
  email: z.string().email('Invalid email').optional().or(z.literal('')),
  gstin: z.string().min(15, 'GSTIN must be 15 characters').max(15),
  logoUrl: z.string().url('Invalid URL').optional().or(z.literal('')),
  termsAndConditions: z.string().optional(),
});

export const ShopProfile: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [isFetching, setIsFetching] = useState(true);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Shop>({
    resolver: zodResolver(shopSchema),
  });

  useEffect(() => {
    loadShop();
  }, []);

  const loadShop = async () => {
    try {
      setIsFetching(true);
      const data = await shopService.getShop();
      if (data) {
        reset(data);
      }
    } catch (err: any) {
      toast.error('Failed to load shop details.');
    } finally {
      setIsFetching(false);
    }
  };

  const onSubmit = async (data: Shop) => {
    try {
      setIsLoading(true);
      const updated = await shopService.updateShop(data);
      reset(updated);
      toast.success('Shop details saved successfully.');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to save shop details.');
    } finally {
      setIsLoading(false);
    }
  };

  if (isFetching) {
    return <div className="p-4">Loading shop profile...</div>;
  }

  return (
    <div className="max-w-3xl mx-auto bg-white shadow rounded-lg p-6">
      <h2 className="text-2xl font-bold text-slate-900 mb-6">Shop Profile</h2>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Input
            label="Shop Name"
            {...register('name', { required: 'Shop name is required' })}
            error={errors.name?.message}
          />
          <Input
            label="Owner Name"
            {...register('ownerName', { required: 'Owner name is required' })}
            error={errors.ownerName?.message}
          />
          <div className="md:col-span-2">
            <Input
              label="Address"
              {...register('address', { required: 'Address is required' })}
              error={errors.address?.message}
            />
          </div>
          <Input
            label="State"
            placeholder="e.g. Uttar Pradesh"
            {...register('state')}
            error={errors.state?.message}
          />
          <Input
            label="Pincode"
            {...register('pincode')}
            error={errors.pincode?.message}
          />
          <Input
            label="Phone Number"
            {...register('phone', { 
              required: 'Phone number is required',
              pattern: { value: /^[0-9]{10}$/, message: 'Must be a 10 digit number' }
            })}
            error={errors.phone?.message}
          />
          <Input
            label="Email Address (Optional)"
            type="email"
            {...register('email')}
            error={errors.email?.message}
          />
          <Input
            label="GSTIN"
            {...register('gstin', { 
              required: 'GSTIN is required',
              pattern: {
                value: /^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$/,
                message: 'Invalid GSTIN format'
              }
            })}
            error={errors.gstin?.message}
          />
          <Input
            label="Logo URL (Optional)"
            placeholder="https://example.com/logo.png"
            {...register('logoUrl')}
            error={errors.logoUrl?.message}
          />
          <div className="md:col-span-2">
            <label className="block text-sm font-medium leading-6 text-slate-900 mb-2">
              Terms & Conditions
            </label>
            <textarea
              {...register('termsAndConditions')}
              rows={4}
              className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
              placeholder="1. Goods once sold..."
            />
          </div>
        </div>

        <div className="flex justify-end">
          <Button type="submit" isLoading={isLoading}>
            Save Profile
          </Button>
        </div>
      </form>
    </div>
  );
};
