import React, { useEffect, useState } from 'react';
import { invoiceService, type InvoiceResponse, type MonthlySummaryResponse } from '@/services/invoices';
import { ViewInvoiceModal } from './ViewInvoiceModal';
import { Calendar, Eye, Trash2, Download, FileSpreadsheet, TrendingUp, DollarSign, Receipt, CreditCard } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';

export const MonthlyInvoices: React.FC = () => {
  const [summaries, setSummaries] = useState<MonthlySummaryResponse[]>([]);
  const [selectedMonth, setSelectedMonth] = useState<MonthlySummaryResponse | null>(null);
  const [monthInvoices, setMonthInvoices] = useState<InvoiceResponse[]>([]);
  const [isLoadingSummary, setIsLoadingSummary] = useState(true);
  const [isLoadingInvoices, setIsLoadingInvoices] = useState(false);

  // View modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceResponse | null>(null);

  useEffect(() => {
    loadMonthlySummary();
  }, []);

  useEffect(() => {
    if (selectedMonth) {
      loadInvoicesForMonth(selectedMonth.year, selectedMonth.month);
    } else {
      setMonthInvoices([]);
    }
  }, [selectedMonth]);

  const loadMonthlySummary = async () => {
    try {
      setIsLoadingSummary(true);
      const data = await invoiceService.getMonthlySummary();
      setSummaries(data);
      if (data.length > 0) {
        setSelectedMonth(data[0]); // Select latest month by default
      }
    } catch (error) {
      console.error('Failed to load monthly summary', error);
      toast.error('Failed to load monthly reports');
    } finally {
      setIsLoadingSummary(false);
    }
  };

  const loadInvoicesForMonth = async (year: number, month: number) => {
    try {
      setIsLoadingInvoices(true);
      const data = await invoiceService.getInvoicesByMonth(year, month);
      setMonthInvoices(data);
    } catch (error) {
      console.error('Failed to load month invoices', error);
      toast.error('Failed to load invoices for selected month');
    } finally {
      setIsLoadingInvoices(false);
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
    if (window.confirm('Are you sure you want to delete this invoice? Stock will be restored.')) {
      try {
        await invoiceService.deleteInvoice(id);
        toast.success('Invoice deleted successfully');
        loadMonthlySummary();
        if (selectedMonth) {
          loadInvoicesForMonth(selectedMonth.year, selectedMonth.month);
        }
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
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center bg-white shadow rounded-lg p-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 flex items-center">
            <Calendar className="h-7 w-7 mr-3 text-blue-600" />
            Monthly Invoice History
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            View which invoices were generated in each month and track monthly revenue breakdown.
          </p>
        </div>

        {/* Month Selector Dropdown */}
        {summaries.length > 0 && (
          <div className="mt-4 sm:mt-0">
            <select
              value={selectedMonth ? `${selectedMonth.year}-${selectedMonth.month}` : ''}
              onChange={(e) => {
                const [yr, mo] = e.target.value.split('-').map(Number);
                const found = summaries.find(s => s.year === yr && s.month === mo);
                if (found) setSelectedMonth(found);
              }}
              className="block w-full rounded-md border-0 py-2 pl-3 pr-10 text-slate-900 ring-1 ring-inset ring-slate-300 focus:ring-2 focus:ring-blue-600 sm:text-sm font-semibold bg-slate-50"
            >
              {summaries.map((s) => (
                <option key={`${s.year}-${s.month}`} value={`${s.year}-${s.month}`}>
                  📅 {s.monthYear} ({s.totalInvoices} invoices)
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {isLoadingSummary ? (
        <div className="bg-white shadow rounded-lg p-8 text-center text-slate-500">
          Loading monthly reports...
        </div>
      ) : summaries.length === 0 ? (
        <div className="bg-white shadow rounded-lg p-8 text-center text-slate-500">
          No invoices created yet. Generate an invoice to see monthly breakdown.
        </div>
      ) : (
        <>
          {/* Monthly Summary Cards Grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-md p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-blue-100 text-sm font-medium">Invoices Created</p>
                  <h3 className="text-3xl font-bold mt-1">{selectedMonth?.totalInvoices || 0}</h3>
                </div>
                <div className="p-3 bg-white/20 rounded-lg">
                  <FileSpreadsheet className="h-6 w-6 text-white" />
                </div>
              </div>
              <p className="text-xs text-blue-100 mt-4">In {selectedMonth?.monthYear}</p>
            </div>

            <div className="bg-gradient-to-br from-emerald-500 to-emerald-600 rounded-xl shadow-md p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-emerald-100 text-sm font-medium">Monthly Revenue</p>
                  <h3 className="text-3xl font-bold mt-1">₹{(selectedMonth?.totalAmount || 0).toFixed(2)}</h3>
                </div>
                <div className="p-3 bg-white/20 rounded-lg">
                  <DollarSign className="h-6 w-6 text-white" />
                </div>
              </div>
              <p className="text-xs text-emerald-100 mt-4">Total billed in {selectedMonth?.monthYear}</p>
            </div>

            <div className="bg-gradient-to-br from-indigo-500 to-indigo-600 rounded-xl shadow-md p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-indigo-100 text-sm font-medium">Total GST Collected</p>
                  <h3 className="text-3xl font-bold mt-1">₹{(selectedMonth?.totalGst || 0).toFixed(2)}</h3>
                </div>
                <div className="p-3 bg-white/20 rounded-lg">
                  <Receipt className="h-6 w-6 text-white" />
                </div>
              </div>
              <p className="text-xs text-indigo-100 mt-4">GST amount for {selectedMonth?.monthYear}</p>
            </div>
          </div>

          {/* Month Quick Tabs / Cards Overview */}
          <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-md font-semibold text-slate-800 mb-4 flex items-center">
              <TrendingUp className="h-5 w-5 mr-2 text-blue-600" />
              All Months History
            </h3>
            <div className="flex flex-wrap gap-3">
              {summaries.map((s) => {
                const isSelected = selectedMonth?.year === s.year && selectedMonth?.month === s.month;
                return (
                  <button
                    key={`${s.year}-${s.month}`}
                    onClick={() => setSelectedMonth(s)}
                    className={`px-4 py-3 rounded-lg text-left transition-all border ${
                      isSelected
                        ? 'border-blue-600 bg-blue-50 ring-2 ring-blue-600 text-blue-900 font-semibold'
                        : 'border-slate-200 bg-slate-50 hover:bg-slate-100 text-slate-700'
                    }`}
                  >
                    <div className="text-sm">{s.monthYear}</div>
                    <div className="text-xs text-slate-500 mt-1">
                      {s.totalInvoices} invoices • ₹{s.totalAmount.toFixed(0)}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Detailed Invoices Table for Selected Month */}
          <div className="bg-white shadow rounded-lg p-6">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-lg font-bold text-slate-900">
                Invoices Created in {selectedMonth?.monthYear}
              </h3>
              <span className="text-xs font-semibold px-3 py-1 bg-blue-100 text-blue-800 rounded-full">
                {monthInvoices.length} Invoices Found
              </span>
            </div>

            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-slate-300">
                <thead>
                  <tr>
                    <th className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-slate-900 sm:pl-0">Invoice #</th>
                    <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Date & Time</th>
                    <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Customer</th>
                    <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Payment</th>
                    <th className="px-3 py-3.5 text-left text-sm font-semibold text-slate-900">Amount</th>
                    <th className="relative py-3.5 pl-3 pr-4 sm:pr-0">
                      <span className="sr-only">Actions</span>
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {isLoadingInvoices ? (
                    <tr>
                      <td colSpan={6} className="text-center py-6 text-slate-500">
                        Loading invoices for {selectedMonth?.monthYear}...
                      </td>
                    </tr>
                  ) : monthInvoices.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="text-center py-6 text-slate-500">
                        No invoices found for this month.
                      </td>
                    </tr>
                  ) : (
                    monthInvoices.map((invoice) => (
                      <tr key={invoice.id} className="hover:bg-slate-50">
                        <td className="whitespace-nowrap py-4 pl-4 pr-3 text-sm font-semibold text-blue-600 sm:pl-0">
                          {invoice.invoiceNumber}
                        </td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">
                          {format(new Date(invoice.date), 'dd MMM yyyy, h:mm a')}
                        </td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">
                          {invoice.customerName || '-'}
                          {invoice.customerMobile && <div className="text-xs text-slate-400">{invoice.customerMobile}</div>}
                        </td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-slate-500">
                          <span className="inline-flex items-center rounded-md bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
                            <CreditCard className="h-3 w-3 mr-1" />
                            {invoice.paymentMethod}
                          </span>
                        </td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm font-bold text-slate-900">
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
                            className="text-green-600 hover:text-green-900 mr-4"
                            title="Download PDF"
                          >
                            <Download className="h-5 w-5" />
                          </button>
                          <button
                            onClick={() => handleDeleteInvoice(invoice.id)}
                            className="text-red-600 hover:text-red-900"
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
          </div>
        </>
      )}

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
