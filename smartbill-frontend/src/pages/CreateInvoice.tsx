import React, { useEffect, useState } from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { productService, type Product } from '@/services/products';
import { invoiceService, type InvoiceCreate } from '@/services/invoices';
import { Plus, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

export const CreateInvoice: React.FC = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const { register, control, handleSubmit, watch, formState: { errors } } = useForm<InvoiceCreate>({
    defaultValues: {
      paymentMethod: 'CASH',
      items: [{ productId: 0, quantity: 1 }]
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

  const calculateTotals = () => {
    let subTotal = 0;
    let totalGst = 0;

    watchItems.forEach((item) => {
      const product = products.find(p => p.id === Number(item.productId));
      if (product && item.quantity > 0) {
        const itemSubTotal = product.price * item.quantity;
        const itemGst = itemSubTotal * (product.gstPercentage / 100);
        subTotal += itemSubTotal;
        totalGst += itemGst;
      }
    });

    return {
      subTotal,
      totalGst,
      grandTotal: subTotal + totalGst
    };
  };

  const totals = calculateTotals();

  const onSubmit = async (data: InvoiceCreate) => {
    try {
      setIsLoading(true);

      const items = data.items.map(item => ({
        productId: Number(item.productId),
        quantity: Number(item.quantity),
        selectedImeis: item.selectedImeis || []
      }));

      // Find any items that don't have enough stock
      const invalidItems = items.filter(item => {
        const product = products.find(p => p.id === item.productId);
        return !product || product.stock < item.quantity;
      });

      if (invalidItems.length > 0) {
        toast.error('One or more selected products have insufficient stock.');
        setIsLoading(false);
        return;
      }

      const invalidImeis = items.filter(item => {
        const product = products.find(p => p.id === item.productId);
        
        // If product has available IMEIs, they MUST select exactly 'quantity' IMEIs
        if (product && product.availableImeis && product.availableImeis.length > 0) {
          return !item.selectedImeis || item.selectedImeis.length !== item.quantity;
        }
        
        // If they selected IMEIs anyway, it must match quantity
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
        items: items
      });

      toast.success('Invoice generated successfully!');
      // Redirect to invoice history after successful creation
      navigate('/invoices');
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Failed to generate invoice.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-slate-900">Create New Invoice</h2>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Customer Details */}
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

        {/* Invoice Items */}
        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-slate-900">Products</h3>
            <Button 
              type="button" 
              variant="secondary"
              onClick={() => append({ productId: 0, quantity: 1 })}
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
                  <th className="px-3 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider w-1/6">Rate</th>
                  <th className="px-3 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider w-1/6">GST</th>
                  <th className="px-3 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider w-1/6">Total</th>
                  <th className="px-3 py-3 w-10"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {fields.map((field, index) => {
                  const selectedProductId = watchItems[index]?.productId;
                  const selectedProduct = products.find(p => p.id === Number(selectedProductId));
                  const qty = Number(watchItems[index]?.quantity || 0);
                  
                  const rate = selectedProduct?.price || 0;
                  const gstPct = selectedProduct?.gstPercentage || 0;
                  const itemSubTotal = rate * qty;
                  const itemGst = itemSubTotal * (gstPct / 100);
                  const itemTotal = itemSubTotal + itemGst;

                  return (
                    <tr key={field.id}>
                      <td className="py-3 px-3">
                        <select
                          className="block w-full rounded-md border-0 py-1.5 text-slate-900 shadow-sm ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3 bg-white"
                          {...register(`items.${index}.productId` as const, { required: true })}
                        >
                          <option value={0}>Select Product...</option>
                          {products.map(p => (
                            <option key={p.id} value={p.id}>
                              {p.name} ({p.stock} in stock)
                            </option>
                          ))}
                        </select>
                        {selectedProduct?.availableImeis && selectedProduct.availableImeis.length > 0 && (
                          <div className="mt-2">
                            <label className="block text-xs font-medium text-slate-700 mb-1">
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
                        )}
                      </td>
                      <td className="py-3 px-3">
                        <Input
                          type="number"
                          min="1"
                          max={selectedProduct?.stock}
                          {...register(`items.${index}.quantity` as const, { 
                            required: true, 
                            min: 1,
                            max: selectedProduct?.stock 
                          })}
                          className="h-9"
                        />
                      </td>
                      <td className="py-3 px-3 text-right text-sm text-slate-900">
                        ₹{rate.toFixed(2)}
                      </td>
                      <td className="py-3 px-3 text-right text-sm text-slate-900">
                        ₹{itemGst.toFixed(2)}
                        <br />
                        <span className="text-xs text-slate-500">({gstPct}%)</span>
                      </td>
                      <td className="py-3 px-3 text-right text-sm font-medium text-slate-900">
                        ₹{itemTotal.toFixed(2)}
                      </td>
                      <td className="py-3 px-3 text-right">
                        <button
                          type="button"
                          onClick={() => remove(index)}
                          className="text-red-500 hover:text-red-700"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </td>
                    </tr>
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

        {/* Totals Section */}
        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex flex-col md:flex-row justify-end space-y-4 md:space-y-0 md:space-x-12">
            <div className="space-y-3 text-right">
              <div className="text-sm text-slate-500 flex justify-between w-56">
                <span>Taxable Amount:</span>
                <span className="text-slate-900">₹{totals.subTotal.toFixed(2)}</span>
              </div>
              <div className="text-sm text-slate-500 flex justify-between w-56">
                <span>CGST:</span>
                <span className="text-slate-900">₹{(totals.totalGst / 2).toFixed(2)}</span>
              </div>
              <div className="text-sm text-slate-500 flex justify-between w-56">
                <span>SGST:</span>
                <span className="text-slate-900">₹{(totals.totalGst / 2).toFixed(2)}</span>
              </div>
              <div className="text-lg font-bold text-slate-900 flex justify-between w-56 border-t pt-3 mt-3">
                <span>Total Amount:</span>
                <span>₹{totals.grandTotal.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="flex justify-end space-x-4">
          <Button type="button" variant="secondary" onClick={() => navigate('/invoices')}>
            Cancel
          </Button>
          <Button type="submit" isLoading={isLoading}>
            Generate Invoice
          </Button>
        </div>
      </form>
    </div>
  );
};
