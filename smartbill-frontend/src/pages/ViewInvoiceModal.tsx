import React from 'react';
import { type InvoiceResponse } from '@/services/invoices';
import { Modal } from '@/components/common/Modal';
import { Button } from '@/components/common/Button';
import { format } from 'date-fns';
import { Printer, Download } from 'lucide-react';

interface ViewInvoiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  invoice: InvoiceResponse | null;
  onPrint: (id: number) => void;
  onDownload: (id: number, invoiceNumber: string) => void;
}

export const ViewInvoiceModal: React.FC<ViewInvoiceModalProps> = ({
  isOpen,
  onClose,
  invoice,
  onPrint,
  onDownload
}) => {
  if (!invoice) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Invoice Details - ${invoice.invoiceNumber}`}>
      <div className="space-y-6">
        {/* Header Info */}
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-slate-500">Bill To:</p>
            <p className="font-medium text-slate-900">{invoice.customerName || 'Cash Customer'}</p>
            {invoice.customerMobile && <p className="text-slate-600">{invoice.customerMobile}</p>}
          </div>
          <div className="text-right">
            <p className="text-slate-500">Date:</p>
            <p className="font-medium text-slate-900">{format(new Date(invoice.date), 'dd MMM yyyy, h:mm a')}</p>
            <p className="text-slate-500 mt-2">Payment Method:</p>
            <p className="font-medium text-slate-900">{invoice.paymentMethod}</p>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto border border-slate-200 rounded-lg">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">Product</th>
                <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">Qty</th>
                <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">Rate</th>
                <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">GST</th>
                <th className="px-4 py-3 text-right text-xs font-medium text-slate-500 uppercase tracking-wider">Total</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-200">
              {invoice.items.map((item, idx) => (
                <tr key={idx}>
                  <td className="px-4 py-3 text-sm text-slate-900 whitespace-nowrap">{item.productName || item.product?.name || '-'}</td>
                  <td className="px-4 py-3 text-sm text-slate-500 text-right whitespace-nowrap">{item.quantity}</td>
                  <td className="px-4 py-3 text-sm text-slate-500 text-right whitespace-nowrap">₹{item.unitPrice.toFixed(2)}</td>
                  <td className="px-4 py-3 text-sm text-slate-500 text-right whitespace-nowrap">
                    {item.gstPercentage}% (₹{item.gstAmount.toFixed(2)})
                  </td>
                  <td className="px-4 py-3 text-sm font-medium text-slate-900 text-right whitespace-nowrap">
                    ₹{item.totalAmount.toFixed(2)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Summary */}
        <div className="flex justify-end text-sm">
          <div className="w-64 space-y-3">
            <div className="flex justify-between text-slate-500">
              <span>Subtotal:</span>
              <span>₹{invoice.subTotal.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-slate-500">
              <span>Total GST:</span>
              <span>₹{invoice.totalGst.toFixed(2)}</span>
            </div>
            <div className="flex justify-between font-bold text-slate-900 text-base pt-3 border-t border-slate-200">
              <span>Grand Total:</span>
              <span>₹{invoice.grandTotal.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-3 pt-4 border-t border-slate-200">
          <Button variant="secondary" onClick={onClose}>Close</Button>
          <Button variant="secondary" onClick={() => onDownload(invoice.id, invoice.invoiceNumber)}>
            <Download className="w-4 h-4 mr-2" />
            Download PDF
          </Button>
          <Button onClick={() => onPrint(invoice.id)}>
            <Printer className="w-4 h-4 mr-2" />
            Print
          </Button>
        </div>
      </div>
    </Modal>
  );
};
