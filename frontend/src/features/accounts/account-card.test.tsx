import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { AccountCard } from "./account-card";
import type { Account } from "@/types/account.types";

const baseAccount: Account = {
  id: "acc-1",
  numeroCuenta: "CBL-AHO-001234",
  customerId: "cust-1",
  nombreCliente: "Juan Pérez",
  tipo: "AHORROS",
  estado: "ACTIVA",
  saldo: 1500,
  moneda: "USD",
  fechaApertura: "2024-01-01",
  createdAt: "2024-01-01T00:00:00",
  version: 1,
};

describe("AccountCard", () => {
  it("renders account number and client name", () => {
    render(<AccountCard account={baseAccount} />);
    expect(screen.getByText("CBL-AHO-001234")).toBeInTheDocument();
    expect(screen.getByText(/Juan Pérez/)).toBeInTheDocument();
  });

  it("renders balance formatted", () => {
    render(<AccountCard account={baseAccount} />);
    expect(screen.getByText(/1.500/)).toBeInTheDocument();
  });

  it("renders ACTIVA status badge", () => {
    render(<AccountCard account={baseAccount} />);
    expect(screen.getByText("Activa")).toBeInTheDocument();
  });

  it("renders CONGELADA status badge with snowflake icon area", () => {
    const frozen = { ...baseAccount, estado: "CONGELADA" as const };
    render(<AccountCard account={frozen} />);
    expect(screen.getByText("Congelada")).toBeInTheDocument();
  });

  it("renders CERRADA status badge", () => {
    const closed = { ...baseAccount, estado: "CERRADA" as const };
    render(<AccountCard account={closed} />);
    expect(screen.getByText("Cerrada")).toBeInTheDocument();
  });

  it("calls onClick when clicked", () => {
    const onClick = vi.fn();
    render(<AccountCard account={baseAccount} onClick={onClick} />);
    fireEvent.click(screen.getByRole("button"));
    expect(onClick).toHaveBeenCalledOnce();
  });

  it("applies selected ring when selected prop is true", () => {
    const { container } = render(
      <AccountCard account={baseAccount} selected onClick={() => {}} />,
    );
    expect(container.querySelector(".ring-2")).toBeInTheDocument();
  });

  it("renders CTS account type label", () => {
    const cts = { ...baseAccount, tipo: "CTS" as const };
    render(<AccountCard account={cts} />);
    expect(screen.getByText("CTS")).toBeInTheDocument();
  });

  it("renders CORRIENTE account type label", () => {
    const corriente = { ...baseAccount, tipo: "CORRIENTE" as const };
    render(<AccountCard account={corriente} />);
    expect(screen.getByText("Cuenta Corriente")).toBeInTheDocument();
  });
});
