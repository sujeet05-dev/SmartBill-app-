import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { productService, Product } from '@/services/products';

const productSchema = z.object({
  name: z.string().min(1, 'Product name is required'),
  brand: z.string().min(1, 'Brand is required'),
  category: z.string().min(1, 'Category is required'),
  sku: z.string().optional(),
  price: z.preprocess((val) => Number(val), z.number().min(0, 'Price must be positive')),
  gstPercentage: z.preprocess((val) => Number(val), z.number().min(0).max(100, 'GST must be between 0 and 100')),
  stock: z.preprocess((val) => Number(val), z.number().int().min(0, 'Stock cannot be negative')),
});

type ProductForm = z.infer<typeof productSchema>;

interface ProductFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  product?: Product;
  onSaved: () => void;
}

export const ProductFormModal: React.FC<ProductFormModalProps> = ({ 
  isOpen, 
  onClose, 
  product,
  onSaved 
}) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProductForm>({
    resolver: zodResolver(productSchema),
  });

  useEffect(() => {
    if (isOpen) {
      if (product) {
        reset(product);
      } else {
        reset({
          name: '',
          brand: '',
          category: '',
          price: 0,
          gstPercentage: 18,
          stock: 0,
          sku: ''
        });
      }
    }
  }, [isOpen, product, reset]);

  const onSubmit = async (data: ProductForm) => {
    try {
      if (product?.id) {
        await productService.updateProduct(product.id, data);
        toast.success('Product updated successfully!');
      } else {
        await productService.createProduct(data);
        toast.success('Product added successfully!');
      }
      onSaved();
      onClose();
    } catch (err: any) {
      setApiError(err.response?.data?.message || 'An error occurred while saving the product.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal 
      isOpen={isOpen} 
      onClose={onClose} 
      title={product ? 'Edit Product' : 'Add New Product'}
    >
      {apiError && (
        <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-md text-sm">
          {apiError}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="sm:col-span-2">
            <Input
              label="Product Name"
              {...register('name', { required: 'Name is required' })}
              error={errors.name?.message}
            />
          </div>
          
          <Input
            label="Brand"
            {...register('brand', { required: 'Brand is required' })}
            error={errors.brand?.message}
          />
          
          <Input
            label="Category"
            {...register('category', { required: 'Category is required' })}
            error={errors.category?.message}
          />
          
          <Input
            label="Selling Price (₹)"
            type="number"
            step="0.01"
            min="0"
            {...register('price', { 
              required: 'Price is required',
              min: { value: 0, message: 'Price cannot be negative' }
            })}
            error={errors.price?.message}
          />
          
          <Input
            label="GST Percentage (%)"
            type="number"
            step="0.1"
            min="0"
            {...register('gstPercentage', { 
              required: 'GST % is required',
              min: { value: 0, message: 'GST cannot be negative' }
            })}
            error={errors.gstPercentage?.message}
          />
          
          <Input
            label="Stock Quantity"
            type="number"
            min="0"
            {...register('stock', { 
              required: 'Stock is required',
              min: { value: 0, message: 'Stock cannot be negative' }
            })}
            error={errors.stock?.message}
          />
          
          <Input
            label="SKU (Optional)"
            {...register('sku')}
            error={errors.sku?.message}
          />
        </div>

        <div className="mt-5 sm:mt-6 sm:grid sm:grid-flow-row-dense sm:grid-cols-2 sm:gap-3">
          <Button 
            type="submit" 
            isLoading={isLoading} 
            className="w-full sm:col-start-2"
          >
            {product ? 'Save Changes' : 'Create Product'}
          </Button>
          <Button 
            type="button" 
            variant="secondary"
            onClick={onClose} 
            className="mt-3 w-full sm:col-start-1 sm:mt-0"
          >
            Cancel
          </Button>
        </div>
      </form>
    </Modal>
  );
};
