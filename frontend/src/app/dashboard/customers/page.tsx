"use client";

import { useState } from "react";
import { Plus, Search, ChevronLeft, ChevronRight } from "lucide-react";
import { useCustomers } from "@/hooks/use-customers";
import { CustomerTable } from "@/features/customers/customer-table";
import { CustomerFormDrawer } from "@/features/customers/customer-form-drawer";
import type { Customer, CustomerFilterParams } from "@/types/customer.types";
import { PAGINATION_DEFAULTS } from "@/constants";
import { usePermissions } from "@/hooks/use-current-user";

export default function CustomersPage() {
  const [filters, setFilters] = useState<CustomerFilterParams>({
    page: PAGINATION_DEFAULTS.PAGE,
    size: PAGINATION_DEFAULTS.SIZE,
    sort: PAGINATION_DEFAULTS.SORT,
  });
  const [search, setSearch] = useState("");
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);

  const { data, isLoading, isError } = useCustomers(filters);
  const { canManageCustomers } = usePermissions();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setFilters((prev) => ({ ...prev, nombre: search || undefined, page: 0 }));
  };

  const handleEdit = (customer: Customer) => {
    setSelectedCustomer(customer);
    setDrawerOpen(true);
  };

  const handleCreate = () => {
    setSelectedCustomer(null);
    setDrawerOpen(true);
  };

  const handleCloseDrawer = () => {
    setDrawerOpen(false);
    setSelectedCustomer(null);
  };

  const goToPage = (page: number) => setFilters((prev) => ({ ...prev, page }));

  return (
    <div className="flex flex-col gap-6 p-8">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-white">Clientes</h1>
          <p className="mt-0.5 text-sm text-slate-500">
            {data ? `${data.totalElements} clientes registrados` : "Cargando..."}
          </p>
        </div>
        {canManageCustomers && (
          <button
            onClick={handleCreate}
            className="flex items-center gap-2 rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-blue-700"
          >
            <Plus className="h-4 w-4" />
            Nuevo cliente
          </button>
        )}
      </div>

      {/* Filters */}
      <form onSubmit={handleSearch} className="flex gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nombre, apellido..."
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm text-slate-900 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
          />
        </div>
        <button
          type="submit"
          className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
        >
          Buscar
        </button>
      </form>

      {/* Table */}
      {isError ? (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-600">
          Error al cargar los clientes. Intentá de nuevo.
        </div>
      ) : isLoading ? (
        <CustomerTableSkeleton />
      ) : (
        <CustomerTable customers={data?.content ?? []} onEdit={canManageCustomers ? handleEdit : undefined} />
      )}

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between">
          <p className="text-xs text-slate-500">
            Página {(filters.page ?? 0) + 1} de {data.totalPages}
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => goToPage((filters.page ?? 0) - 1)}
              disabled={data.first}
              className="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-600 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 dark:border-slate-700 dark:bg-slate-800"
            >
              <ChevronLeft className="h-3.5 w-3.5" />
              Anterior
            </button>
            <button
              onClick={() => goToPage((filters.page ?? 0) + 1)}
              disabled={data.last}
              className="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-600 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 dark:border-slate-700 dark:bg-slate-800"
            >
              Siguiente
              <ChevronRight className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      )}

      {/* Drawer */}
      <CustomerFormDrawer
        open={drawerOpen}
        onClose={handleCloseDrawer}
        customer={selectedCustomer}
      />
    </div>
  );
}

function CustomerTableSkeleton() {
  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 dark:border-slate-800">
      <div className="border-b border-slate-200 bg-slate-50 px-4 py-3 dark:border-slate-800 dark:bg-slate-900/60">
        <div className="grid grid-cols-6 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-3 rounded-full bg-slate-200 dark:bg-slate-700" />
          ))}
        </div>
      </div>
      {Array.from({ length: 6 }).map((_, i) => (
        <div
          key={i}
          className="animate-pulse border-b border-slate-100 bg-white px-4 py-4 last:border-0 dark:border-slate-800 dark:bg-slate-900"
        >
          <div className="grid grid-cols-6 gap-4">
            <div className="h-3 rounded-full bg-slate-100 dark:bg-slate-800" />
            <div className="h-3 rounded-full bg-slate-100 dark:bg-slate-800" />
            <div className="h-3 w-3/4 rounded-full bg-slate-100 dark:bg-slate-800" />
            <div className="h-3 w-1/2 rounded-full bg-slate-100 dark:bg-slate-800" />
            <div className="h-3 w-2/3 rounded-full bg-slate-100 dark:bg-slate-800" />
            <div className="h-3 w-1/4 rounded-full bg-slate-100 dark:bg-slate-800" />
          </div>
        </div>
      ))}
    </div>
  );
}
