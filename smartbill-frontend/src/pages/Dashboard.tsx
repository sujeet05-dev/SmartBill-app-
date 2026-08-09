import React, { useEffect, useState } from 'react';
import { invoiceService } from '@/services/invoices';
import { IndianRupee, Package } from 'lucide-react';

export const Dashboard: React.FC = () => {
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [totalItemsSold, setTotalItemsSold] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setIsLoading(true);
        // Fetch both GST and Non-GST invoices
        const [gstInvoices, nonGstInvoices] = await Promise.all([
          invoiceService.getAllInvoices('', true),
          invoiceService.getAllInvoices('', false)
        ]);

        const allInvoices = [...gstInvoices, ...nonGstInvoices];

        let revenue = 0;
        let itemsCount = 0;

        allInvoices.forEach(invoice => {
          revenue += invoice.grandTotal;
          invoice.items.forEach((item: any) => {
            itemsCount += item.quantity;
          });
        });

        setTotalRevenue(revenue);
        setTotalItemsSold(itemsCount);
      } catch (error) {
        console.error('Failed to fetch dashboard stats', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64 text-slate-500">
        Loading dashboard...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-slate-900">Dashboard Overview</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Total Sales Card */}
        <div className="bg-white rounded-lg shadow p-6 flex items-center space-x-4 border-l-4 border-blue-500">
          <div className="p-3 bg-blue-100 rounded-full text-blue-600">
            <IndianRupee className="w-8 h-8" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Total Sales Amount</p>
            <p className="text-2xl font-bold text-slate-900">₹{totalRevenue.toFixed(2)}</p>
          </div>
        </div>

        {/* Total Items Sold Card */}
        <div className="bg-white rounded-lg shadow p-6 flex items-center space-x-4 border-l-4 border-green-500">
          <div className="p-3 bg-green-100 rounded-full text-green-600">
            <Package className="w-8 h-8" />
          </div>
          <div>
            <p className="text-sm font-medium text-slate-500">Total Items Sold</p>
            <p className="text-2xl font-bold text-slate-900">{totalItemsSold}</p>
          </div>
        </div>
      </div>
    </div>
  );
};
