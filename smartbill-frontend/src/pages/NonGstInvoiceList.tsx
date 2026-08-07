import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { invoiceService, type InvoiceResponse } from '@/services/invoices';
import { Button } from '@/components/common/Button';
import { Plus, Eye, Search, Trash2 } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import { ViewInvoiceModal } from './ViewInvoiceModal';

export const NonGstInvoiceList: React.FC = () => {
  const navigate = useNavigate();
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [search, setSearch] = useState('');
  
  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceResponse | null>(null);

  useEffect(() => {
    loadInvoices();
  }, [search]);

  const loadInvoices = async () => {
    try {
      setIsLoading(true);
      const data = await invoiceService.getAllInvoices(search);
      setInvoices(data);
    } catch (error) {
      console.error('Failed to load invoices', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownloadPdf = async (id: number, invoiceNumber: string) => {
    try {
      const blob = await invoiceService.downloadPdf(id);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `invoice_${invoiceNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Failed to download PDF', error);
      toast.error('Failed to download invoice PDF.');
    }
  };

  const handlePrintPdf = async (id: number) => {
    try {
      const blob = await invoiceService.downloadPdf(id);
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
      setTimeout(() => window.URL.revokeObjectURL(url), 10000);
    } catch (error) {
      console.error('Failed to open PDF', error);
      toast.error('Failed to open invoice PDF.');
    }
  };

  const handleDeleteInvoice = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this invoice? The products will be returned to inventory stock.')) {
      try {
        await invoiceService.deleteInvoice(id);
        toast.success('Invoice deleted successfully');
        loadInvoices();
      } catch (error) {
        console.error('Failed to delete invoice', error);
        toast.error('Failed to delete invoice.');
      }
    }
  };

  const openViewModal = (invoice: InvoiceResponse) => {
    setSelectedInvoice(invoice);
    setIsModalOpen(true);
  };

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <div className="flex flex-col sm:flex-row justify-between items-center mb-6 space-y-4 sm:space-y-0">
        <h2 className="text-2xl font-bold text-slate-900">Non-GST Bills</h2>
        
        <div className="flex w-full sm:w-auto items-center space-x-4">
          <div className="relative flex-1 sm:w-64">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
              <Search className="h-5 w-5 text-slate-400" />
            </div>
            <input
              type="text"
              placeholder="Search by Invoice # or Customer..."
              className="block w-full rounded-md border-0 py-1.5 pl-10 text-slate-900 ring-1 ring-inset ring-slate-300 placeholder:text-slate-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <Button onClick={() => navigate('/non-gst-invoices/new')} className="flex-shrink-0">
            <Plus className="h-5 w-5 mr-2" />
            Create Invoice
          </Button>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-slate-300">
          <thead>
            <tr>
              <th className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-slate-900 sm:pl-0">Invoice #</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Date</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Customer</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Payment</th>
              <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Amount</th>
              <th className="relative py-3.5 pl-3 pr-4 sm:pr-0">
                <span className="sr-only">Actions</span>
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-200">
            {isLoading ? (
              <tr>
                <td colSpan={6} className="text-center py-4 text-slate-500">Loading invoices...</td>
              </tr>
            ) : invoices.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center py-4 text-slate-500">No invoices found.</td>
              </tr>
            ) : (
              invoices.map((invoice) => (
                <tr key={invoice.id}>
                  <td className="whitespace-nowrap py-4 pl-4 pr-3 text-sm font-medium text-slate-900 sm:pl-0">
                    {invoice.invoiceNumber}
                  </td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">
                    {format(new Date(invoice.date), 'dd MMM yyyy, h:mm a')}
                  </td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">
                    {invoice.customerName || '-'}
                    {invoice.customerMobile && <div className="text-xs">{invoice.customerMobile}</div>}
                  </td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">
                    <span className="inline-flex items-center rounded-md bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
                      {invoice.paymentMethod}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-3 py-4 text-sm font-medium text-slate-900">
                    ₹{invoice.grandTotal.toFixed(2)}
                  </td>
                  <td className="relative whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-0">
                    <button 
                      onClick={() => openViewModal(invoice)}
                      className="text-blue-600 hover:text-blue-900 mr-4"
                      title="View Details"
                    >
                      <Eye className="h-5 w-5" />
                    </button>
                    <button 
                      onClick={() => handleDownloadPdf(invoice.id, invoice.invoiceNumber)}
                      className="text-green-600 hover:text-green-900"
                      title="Download PDF"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                    </button>
                    <button 
                      onClick={() => handleDeleteInvoice(invoice.id)}
                      className="text-red-600 hover:text-red-900 ml-4"
                      title="Delete Invoice"
                    >
                      <Trash2 className="h-5 w-5" />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <ViewInvoiceModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        invoice={selectedInvoice}
        onPrint={handlePrintPdf}
        onDownload={handleDownloadPdf}
      />
    </div>
  );
};
