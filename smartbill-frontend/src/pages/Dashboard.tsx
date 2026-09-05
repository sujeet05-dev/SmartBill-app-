import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { invoiceService, type DashboardStatsResponse } from '@/services/invoices';
import { IndianRupee, Package, FilePlus, ShoppingCart, ArrowRight } from 'lucide-react';

const CACHE_KEY = 'smartbill_dashboard_stats';

const getCachedStats = (): DashboardStatsResponse | null => {
  try {
    const raw = sessionStorage.getItem(CACHE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const Dashboard: React.FC = () => {
  const cached = getCachedStats();
  const [stats, setStats] = useState<DashboardStatsResponse>(
    cached || { totalRevenue: 0, totalItemsSold: 0 }
  );
  const [isInitialLoading, setIsInitialLoading] = useState(!cached);

  useEffect(() => {
    let isMounted = true;

    const fetchStats = async () => {
      try {
        const data = await invoiceService.getDashboardStats();
        if (isMounted) {
          setStats(data);
          sessionStorage.setItem(CACHE_KEY, JSON.stringify(data));
        }
      } catch (error) {
        console.error('Failed to fetch dashboard stats', error);
      } finally {
        if (isMounted) {
          setIsInitialLoading(false);
        }
      }
    };

    fetchStats();

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-900">Dashboard Overview</h2>
        <p className="text-sm text-slate-500 mt-1">Here is a quick snapshot of your shop's performance.</p>
      </div>
      
      {/* Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Total Sales Card */}
        <div className="bg-white rounded-xl shadow-sm p-6 flex items-center space-x-4 border border-slate-100 border-l-4 border-l-blue-600 transition-all hover:shadow-md">
          <div className="p-3.5 bg-blue-50 rounded-xl text-blue-600">
            <IndianRupee className="w-8 h-8" />
          </div>
          <div className="flex-1">
            <p className="text-sm font-medium text-slate-500">Total Sales Amount</p>
            {isInitialLoading ? (
              <div className="h-8 w-36 bg-slate-200 animate-pulse rounded mt-1"></div>
            ) : (
              <p className="text-2xl font-bold text-slate-900">
                ₹{Number(stats.totalRevenue || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 4 })}
              </p>
            )}
          </div>
        </div>

        {/* Total Items Sold Card */}
        <div className="bg-white rounded-xl shadow-sm p-6 flex items-center space-x-4 border border-slate-100 border-l-4 border-l-emerald-600 transition-all hover:shadow-md">
          <div className="p-3.5 bg-emerald-50 rounded-xl text-emerald-600">
            <Package className="w-8 h-8" />
          </div>
          <div className="flex-1">
            <p className="text-sm font-medium text-slate-500">Total Items Sold</p>
            {isInitialLoading ? (
              <div className="h-8 w-24 bg-slate-200 animate-pulse rounded mt-1"></div>
            ) : (
              <p className="text-2xl font-bold text-slate-900">{stats.totalItemsSold || 0}</p>
            )}
          </div>
        </div>
      </div>

      {/* Quick Action Shortcuts */}
      <div className="bg-white rounded-xl shadow-sm p-6 border border-slate-100">
        <h3 className="text-base font-semibold text-slate-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Link
            to="/invoices/new"
            className="group flex items-center justify-between p-4 rounded-lg bg-blue-50/60 border border-blue-100 hover:bg-blue-50 transition-colors"
          >
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-blue-600 text-white rounded-lg">
                <FilePlus className="w-5 h-5" />
              </div>
              <div className="text-left">
                <span className="block text-sm font-semibold text-slate-900">New GST Bill</span>
                <span className="block text-xs text-slate-500">Create tax invoice</span>
              </div>
            </div>
            <ArrowRight className="w-4 h-4 text-blue-600 transition-transform group-hover:translate-x-1" />
          </Link>

          <Link
            to="/non-gst-invoices/new"
            className="group flex items-center justify-between p-4 rounded-lg bg-amber-50/60 border border-amber-100 hover:bg-amber-50 transition-colors"
          >
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-amber-600 text-white rounded-lg">
                <ShoppingCart className="w-5 h-5" />
              </div>
              <div className="text-left">
                <span className="block text-sm font-semibold text-slate-900">Non-GST Bill</span>
                <span className="block text-xs text-slate-500">Create estimate/cash receipt</span>
              </div>
            </div>
            <ArrowRight className="w-4 h-4 text-amber-600 transition-transform group-hover:translate-x-1" />
          </Link>

          <Link
            to="/inventory"
            className="group flex items-center justify-between p-4 rounded-lg bg-slate-50 border border-slate-200 hover:bg-slate-100/80 transition-colors"
          >
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-slate-700 text-white rounded-lg">
                <Package className="w-5 h-5" />
              </div>
              <div className="text-left">
                <span className="block text-sm font-semibold text-slate-900">Manage Inventory</span>
                <span className="block text-xs text-slate-500">View & add products</span>
              </div>
            </div>
            <ArrowRight className="w-4 h-4 text-slate-600 transition-transform group-hover:translate-x-1" />
          </Link>
        </div>
      </div>
    </div>
  );
};
