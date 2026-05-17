import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { Users } from "lucide-react";
import { KpiCard } from "./kpi-card";

describe("KpiCard", () => {
  it("renders label and value", () => {
    render(<KpiCard label="Clientes activos" value={42} icon={Users} />);
    expect(screen.getByText("Clientes activos")).toBeInTheDocument();
    expect(screen.getByText("42")).toBeInTheDocument();
  });

  it("renders sub text when provided", () => {
    render(
      <KpiCard label="KPI" value={10} icon={Users} sub="de 15 totales" />,
    );
    expect(screen.getByText("de 15 totales")).toBeInTheDocument();
  });

  it("does not render sub text when not provided", () => {
    render(<KpiCard label="KPI" value={10} icon={Users} />);
    expect(screen.queryByText("de")).not.toBeInTheDocument();
  });

  it("renders skeleton when isLoading is true", () => {
    const { container } = render(
      <KpiCard label="KPI" value={0} icon={Users} isLoading />,
    );
    expect(container.querySelector(".animate-pulse")).toBeInTheDocument();
  });

  it("does not render animate-pulse when not loading", () => {
    const { container } = render(
      <KpiCard label="KPI" value={5} icon={Users} isLoading={false} />,
    );
    expect(container.querySelector(".animate-pulse")).not.toBeInTheDocument();
  });

  it("renders string value correctly", () => {
    render(<KpiCard label="Saldo" value="S/ 1,000.00" icon={Users} />);
    expect(screen.getByText("S/ 1,000.00")).toBeInTheDocument();
  });
});
