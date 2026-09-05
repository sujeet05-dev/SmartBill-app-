import React, { useEffect, useState, useMemo } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { productService, type Product } from '@/services/products';
import { invoiceService, type InvoiceCreate } from '@/services/invoices';
import { Plus, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

export const CreateNonGstInvoice: React.FC = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const { register, control, handleSubmit, watch, setValue, formState: { errors } } = useForm<InvoiceCreate>({
    defaultValues: {
      paymentMethod: 'CASH',
      items: [{ productId: 0, productName: '', unitPrice: 0, quantity: 1 }]
    }
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items'
  });

  const watchItems = watch('items');

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      const data = await productService.getAllProducts();
      // Only show products in stock
      setProducts(data.filter((p: Product) => p.stock > 0));
    } catch (error) {
      console.error('Failed to load products', error);
    }
  };

  const totals = useMemo(() => {
    let subTotal = 0;

    watchItems.forEach((item) => {
      if (item.quantity > 0) {
        subTotal += (Number(item.unitPrice) || 0) * item.quantity;
      }
    });

    return {
      subTotal,
      totalGst: 0,
      grandTotal: subTotal
    };
  }, [watchItems]);

  const onSubmit = async (data: InvoiceCreate) => {
    try {
      setIsLoading(true);

      const items = data.items.map(item => {
        const prodId = Number(item.productId);
        return {
          productId: prodId > 0 ? prodId : undefined,
          productName: item.productName || undefined,
          unitPrice: Number(item.unitPrice),
          quantity: Number(item.quantity),
          selectedImeis: item.selectedImeis || []
        };
      });

      // Find any items that don't have enough stock (only for inventory products)
      const invalidItems = items.filter(item => {
        if (!item.productId) return false;
        const product = products.find(p => p.id === item.productId);
        return !product || product.stock < item.quantity;
      });

      if (invalidItems.length > 0) {
        toast.error('One or more selected products have insufficient stock.');
        setIsLoading(false);
        return;
      }

      const invalidImeis = items.filter(item => {
        if (!item.productId) return false;
        const product = products.find(p => p.id === item.productId);
        
        if (product && product.availableImeis && product.availableImeis.length > 0) {
          return !item.selectedImeis || item.selectedImeis.length !== item.quantity;
        }
        
        if (item.selectedImeis && item.selectedImeis.length > 0) {
          return item.selectedImeis.length !== item.quantity;
        }
        
        return false;
      });

      if (invalidImeis.length > 0) {
        toast.error('The number of selected IMEIs must match the quantity for the product.');
        setIsLoading(false);
        return;
      }

      await invoiceService.createInvoice({
        ...data,
        isGst: false,
        items: items
      });

      productService.clearCache();
      sessionStorage.removeItem('smartbill_dashboard_stats');

      toast.success('Non-GST Bill generated successfully!');
      navigate('/non-gst-invoices');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to generate bill.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-slate-900">Create Non-GST Bill / Estimate</h2>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-slate-900 mb-4">Customer Details</h3>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Input
              label="Customer Name *"
              {...register('customerName', { required: 'Customer name is required' })}
              error={errors.customerName?.message as string}
            />
            <Input
              label="Mobile Number *"
              {...register('customerMobile', { required: 'Mobile number is required' })}
              error={errors.customerMobile?.message as string}
            />
            <Input
              label="Customer Address *"
              {...register('customerAddress', { required: 'Address is required' })}
              error={errors.customerAddress?.message as string}
            />
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Payment Method
              </label>
              <select
                className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3 bg-white h-9"
                {...register('paymentMethod')}
              >
                <option value="CASH">Cash</option>
                <option value="CARD">Card</option>
                <option value="UPI">UPI</option>
                <option value="BANK_TRANSFER">Bank Transfer</option>
              </select>
            </div>
            <Input
              label="Received Amount (₹)"
              type="number"
              step="0.01"
              min="0"
              {...register('receivedAmount', { valueAsNumber: true })}
            />
          </div>
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-slate-900">Products</h3>
            <Button 
              type="button" 
              variant="secondary"
              onClick={() => append({ productId: 0, productName: '', unitPrice: 0, quantity: 1 })}
              className="text-xs py-1.5"
            >
              <Plus className="h-4 w-4 mr-1" />
              Add Row
            </Button>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead>
                <tr>
                  <th className="px-3 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider w-2/5">Product</th>
                  <th className="px-3 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider w-1/6">Qty</th>
                  <th className="px-3 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider w-1/6">Custom Rate (₹)</th>
                  <th className="px-3 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider w-1/6">Total</th>
                  <th className="px-3 py-3 w-10"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {fields.map((field, index) => {
                  const selectedProductId = watchItems[index]?.productId;
                  const selectedProduct = products.find(p => p.id === Number(selectedProductId));
                  const isManual = !selectedProductId || Number(selectedProductId) === 0;
                  const qty = Number(watchItems[index]?.quantity || 0);
                  const rate = Number(watchItems[index]?.unitPrice || 0);
                  const itemTotal = rate * qty;

                  return (
                    <React.Fragment key={field.id}>
                      <tr>
                        <td className="py-3 px-3">
                          <div className="space-y-2">
                            <select
                              className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3 bg-white"
                              {...register(`items.${index}.productId` as const)}
                              onChange={(e) => {
                                const prodId = Number(e.target.value);
                                setValue(`items.${index}.productId`, prodId);
                                if (prodId > 0) {
                                  const p = products.find(prod => prod.id === prodId);
                                  if (p) {
                                    setValue(`items.${index}.unitPrice`, p.price);
                                    setValue(`items.${index}.productName`, p.name);
                                  }
                                } else {
                                  setValue(`items.${index}.unitPrice`, 0);
                                  setValue(`items.${index}.productName`, '');
                                }
                              }}
                            >
                              <option value={0}>-- Manual Entry --</option>
                              {products.map(p => (
                                <option key={p.id} value={p.id}>
                                  {p.name} ({p.stock} in stock)
                                </option>
                              ))}
                            </select>
                            {isManual && (
                              <input
                                type="text"
                                placeholder="Enter Product Name"
                                className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3"
                                {...register(`items.${index}.productName` as const)}
                              />
                            )}
                          </div>
                        </td>
                        <td className="py-3 px-3 align-top">
                          <input
                            type="number"
                            min="1"
                            max={isManual ? undefined : selectedProduct?.stock}
                            className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3"
                            {...register(`items.${index}.quantity` as const, { 
                              required: true, 
                              valueAsNumber: true,
                              min: 1,
                              max: isManual ? undefined : selectedProduct?.stock
                            })}
                          />
                        </td>
                        <td className="py-3 px-3 align-top">
                          <input
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Rate"
                            className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3 text-right"
                            {...register(`items.${index}.unitPrice` as const, { required: true, valueAsNumber: true })}
                          />
                        </td>
                        <td className="py-3 px-3 text-right text-sm font-medium text-slate-900 align-top">
                          ₹{itemTotal.toFixed(2)}
                        </td>
                        <td className="py-3 px-3 text-right align-top">
                          {fields.length > 1 && (
                            <button
                              type="button"
                              onClick={() => remove(index)}
                              className="text-red-500 hover:text-red-700"
                            >
                              <Trash2 className="h-4 w-4" />
                            </button>
                          )}
                        </td>
                      </tr>
                      {selectedProduct?.availableImeis && selectedProduct.availableImeis.length > 0 && (
                        <tr>
                          <td colSpan={5} className="px-3 pb-4 pt-1 bg-slate-50 border-b border-slate-200">
                            <div className="flex flex-col gap-1 pl-4 border-l-2 border-blue-500">
                              <label className="text-xs font-medium text-slate-700">
                                Select IMEIs ({watchItems[index]?.selectedImeis?.length || 0}/{qty})
                              </label>
                              <select
                                multiple
                                className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 sm:text-xs px-2 bg-white"
                                {...register(`items.${index}.selectedImeis` as const)}
                                size={Math.min(3, selectedProduct.availableImeis.length)}
                              >
                                {selectedProduct.availableImeis.map(imei => (
                                  <option key={imei} value={imei}>{imei}</option>
                                ))}
                              </select>
                              <p className="text-[10px] text-slate-500 mt-1">Hold Ctrl/Cmd to select multiple</p>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
          {fields.length === 0 && (
            <div className="text-center py-4 text-sm text-slate-500">
              No products added yet. Click "Add Row" to start.
            </div>
          )}
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex flex-col md:flex-row justify-end space-y-4 md:space-y-0 md:space-x-12">
            <div className="space-y-3 text-right">
              <div className="text-lg font-bold text-slate-900 flex justify-between w-56 pt-3 mt-3">
                <span>Total Amount:</span>
                <span>₹{totals.grandTotal.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-end space-x-4">
          <Button type="button" variant="secondary" onClick={() => navigate('/non-gst-invoices')}>
            Cancel
          </Button>
          <Button type="submit" isLoading={isLoading}>
            Generate Bill
          </Button>
        </div>
      </form>
    </div>
  );
};
