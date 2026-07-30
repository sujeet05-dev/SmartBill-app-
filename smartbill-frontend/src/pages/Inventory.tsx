import React, { useEffect, useState } from 'react';
import { productService, Product } from '@/services/products';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Search, Plus, Edit2, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { ProductFormModal } from './ProductFormModal';

export const Inventory: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | undefined>();

  useEffect(() => {
    loadProducts();
  }, [search]);

  const loadProducts = async () => {
    try {
      setIsLoading(true);
      const data = await productService.getAllProducts(search);
      setProducts(data);
    } catch (error) {
      console.error('Failed to load products', error);
      toast.error('Failed to load products');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      try {
        await productService.deleteProduct(id);
        toast.success('Product deleted successfully');
        loadProducts();
      } catch (error) {
        console.error('Failed to delete product', error);
        toast.error('Failed to delete product');
      }
    }
  };

  const openAddModal = () => {
    setEditingProduct(undefined);
    setIsModalOpen(true);
  };

  const openEditModal = (product: Product) => {
    setEditingProduct(product);
    setIsModalOpen(true);
  };

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <div className="flex flex-col sm:flex-row justify-between items-center mb-6 space-y-4 sm:space-y-0">
        <h2 className="text-2xl font-bold text-slate-900">Product Inventory</h2>
        
        <div className="flex w-full sm:w-auto items-center space-x-4">
          <div className="relative flex-1 sm:w-64">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
              <Search className="h-5 w-5 text-slate-400" />
            </div>
            <input
              type="text"
              placeholder="Search products..."
              className="block w-full rounded-md border-0 py-1.5 pl-10 text-slate-900 ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          
          <Button onClick={openAddModal} className="flex-shrink-0">
            <Plus className="h-5 w-5 mr-2" />
            Add Product
          </Button>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-300">
          <thead>
            <tr>
              <th className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-slate-900 sm:pl-0">Name</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Brand</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Category</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Price</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Stock</th>
              <th className="relative py-3.5 pl-3 pr-4 sm:pr-0">
                <span className="sr-only">Actions</span>
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {isLoading ? (
              <tr>
                <td colSpan={6} className="text-center py-4 text-slate-500">Loading products...</td>
              </tr>
            ) : products.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center py-4 text-slate-500">No products found.</td>
              </tr>
            ) : (
              products.map((product) => (
                <tr key={product.id}>
                  <td className="whitespace-nowrap py-4 pl-4 pr-3 text-sm font-medium text-slate-900 sm:pl-0">
                    {product.name}
                    {product.sku && <div className="text-xs text-slate-500">SKU: {product.sku}</div>}
                  </td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">{product.brand}</td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">{product.category}</td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">₹{product.price.toFixed(2)}</td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">
                    <span className={`inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset ${
                      product.stock > 10 ? 'bg-green-50 text-green-700 ring-green-600/20' : 
                      product.stock > 0 ? 'bg-yellow-50 text-yellow-800 ring-yellow-600/20' : 
                      'bg-red-50 text-red-700 ring-red-600/10'
                    }`}>
                      {product.stock > 0 ? product.stock : 'Out of Stock'}
                    </span>
                  </td>
                  <td className="relative whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-0">
                    <button 
                      onClick={() => openEditModal(product)}
                      className="text-blue-600 hover:text-blue-900 mr-4"
                    >
                      <Edit2 className="h-4 w-4" />
                      <span className="sr-only">Edit</span>
                    </button>
                    <button 
                      onClick={() => product.id && handleDelete(product.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      <Trash2 className="h-4 w-4" />
                      <span className="sr-only">Delete</span>
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <ProductFormModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        product={editingProduct}
        onSaved={loadProducts}
      />
    </div>
  );
};
