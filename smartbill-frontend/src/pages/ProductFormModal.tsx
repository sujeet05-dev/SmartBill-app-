import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import toast from 'react-hot-toast';
import { Modal } from '@/components/common/Modal';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { productService, type Product } from '@/services/products';

const productSchema = z.object({
  name: z.string().min(1, 'Product name is required'),
  description: z.string().optional(),
  brand: z.string().min(1, 'Brand is required'),
  category: z.string().min(1, 'Category is required'),
  sku: z.string().optional(),
  hsnCodesText: z.string().min(1, 'At least one HSN Code is required'),
  imeisText: z.string().optional(),
  unit: z.string().optional(),
  price: z.preprocess((val) => Number(val), z.number().min(0, 'Price must be positive')),
  gstPercentage: z.preprocess((val) => Number(val), z.number().min(0).max(100, 'GST must be between 0 and 100')),
  stock: z.preprocess((val) => Number(val), z.number().int().min(0, 'Stock cannot be negative')),
});

// Form will be handled with any to avoid Zod preprocessing type mismatches

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
  const [isLoading, setIsLoading] = React.useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<any>({
    resolver: zodResolver(productSchema),
  });

  useEffect(() => {
    if (isOpen) {
      if (product) {
        reset({
          ...product,
          imeisText: product.availableImeis ? product.availableImeis.join('\n') : '',
          hsnCodesText: product.availableHsnCodes ? product.availableHsnCodes.join('\n') : ''
        });
      } else {
        reset({
          name: '',
          description: '',
          brand: '',
          category: '',
          price: 0,
          gstPercentage: 18,
          stock: 0,
          sku: '',
          hsnCodesText: '',
          imeisText: '',
          unit: 'PCS'
        });
      }
    }
  }, [isOpen, product, reset]);

  const onSubmit = async (data: any) => {
    try {
      setIsLoading(true);

      const parsedImeis = data.imeisText 
        ? data.imeisText.split(/[\n,]+/).map((i: string) => i.trim()).filter((i: string) => i.length > 0)
        : [];
        
      const parsedHsnCodes = data.hsnCodesText 
        ? data.hsnCodesText.split(/[\n,]+/).map((i: string) => i.trim()).filter((i: string) => i.length > 0)
        : [];
        
      if (parsedImeis.length > data.stock) {
        toast.error(`You entered ${parsedImeis.length} IMEIs, but stock is only ${data.stock}.`);
        setIsLoading(false);
        return;
      }

      const payload: Product = {
        id: product?.id || 0,
        name: data.name,
        description: data.description,
        brand: data.brand,
        category: data.category,
        sku: data.sku || '',
        availableHsnCodes: parsedHsnCodes,
        availableImeis: parsedImeis,
        unit: data.unit || 'PCS',
        price: data.price,
        gstPercentage: data.gstPercentage,
        stock: data.stock,
      };

      if (product?.id) {
        await productService.updateProduct(product.id, payload);
        toast.success('Product updated successfully!');
      } else {
        await productService.createProduct(payload);
        toast.success('Product added successfully!');
      }
      onSaved();
      onClose();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'An error occurred while saving the product.');
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
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="sm:col-span-2">
            <Input
              label="Product Name"
              {...register('name')}
              error={errors.name?.message as string}
            />
          </div>
          
          <div className="sm:col-span-2">
            <Input
              label="Description (Optional)"
              {...register('description')}
              error={errors.description?.message as string}
            />
          </div>
          
          <Input
            label="Brand"
            {...register('brand')}
            error={errors.brand?.message as string}
          />
          
          <Input
            label="Category"
            {...register('category')}
            error={errors.category?.message as string}
          />
          
          <Input
            label="Selling Price (₹)"
            type="number"
            step="0.01"
            min="0"
            {...register('price')}
            error={errors.price?.message as string}
          />
          
          <Input
            label="GST Percentage (%)"
            type="number"
            step="0.1"
            min="0"
            {...register('gstPercentage')}
            error={errors.gstPercentage?.message as string}
          />
          
          <Input
            label="Stock Quantity"
            type="number"
            min="0"
            {...register('stock')}
            error={errors.stock?.message as string}
          />
          
          <Input
            label="SKU (Optional)"
            {...register('sku')}
            error={errors.sku?.message as string}
          />
          
          <div className="sm:col-span-2">
            <label className="block text-sm font-medium leading-6 text-slate-900 mb-2">
              HSN Codes
            </label>
            <p className="text-xs text-slate-500 mb-2">Enter multiple HSN codes separated by commas or new lines.</p>
            <textarea
              {...register('hsnCodesText')}
              rows={2}
              className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
              placeholder="1234, 5678&#10;..."
            />
            {errors.hsnCodesText?.message && (
              <p className="mt-2 text-sm text-red-600">{errors.hsnCodesText.message as string}</p>
            )}
          </div>

          <div className="sm:col-span-2">
            <label className="block text-sm font-medium leading-6 text-slate-900 mb-2">
              IMEIs / Serial Numbers (Optional)
            </label>
            <p className="text-xs text-slate-500 mb-2">Enter IMEIs separated by commas or new lines. Count must not exceed stock.</p>
            <textarea
              {...register('imeisText')}
              rows={3}
              className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
              placeholder="123456789012345, 987654321098765&#10;..."
            />
            {errors.imeisText?.message && (
              <p className="mt-2 text-sm text-red-600">{errors.imeisText.message as string}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium leading-6 text-slate-900 mb-2">
              Unit
            </label>
            <select
              {...register('unit')}
              className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3 bg-white h-9"
            >
              <option value="PCS">Pieces (PCS)</option>
              <option value="KG">Kilograms (KG)</option>
              <option value="GM">Grams (GM)</option>
              <option value="LTR">Liters (LTR)</option>
              <option value="BOX">Box (BOX)</option>
              <option value="SET">Set (SET)</option>
              <option value="MTR">Meters (MTR)</option>
            </select>
          </div>
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
