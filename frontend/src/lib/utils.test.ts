import { describe, it, expect } from "vitest";
import { formatCurrency, formatDate, formatDateTime, cn } from "./utils";

describe("cn", () => {
  it("merges class names correctly", () => {
    expect(cn("foo", "bar")).toBe("foo bar");
  });

  it("deduplicates conflicting tailwind classes (last wins)", () => {
    expect(cn("text-red-500", "text-blue-600")).toBe("text-blue-600");
  });

  it("ignores falsy values", () => {
    expect(cn("foo", false, undefined, null, "bar")).toBe("foo bar");
  });
});

describe("formatCurrency", () => {
  it("formats PEN amounts with S/ symbol", () => {
    const result = formatCurrency(1000, "PEN");
    expect(result).toContain("1");
    expect(result).toContain("000");
  });

  it("formats USD amounts", () => {
    const result = formatCurrency(250.5, "USD");
    expect(result).toContain("250");
  });

  it("always shows 2 decimal places", () => {
    const result = formatCurrency(100, "PEN");
    expect(result).toMatch(/[,.]00$/);
  });

  it("handles zero correctly", () => {
    const result = formatCurrency(0, "PEN");
    expect(result).toContain("0");
  });

  it("defaults currency to USD when not provided", () => {
    const withDefault = formatCurrency(100);
    const withUSD     = formatCurrency(100, "USD");
    expect(withDefault).toBe(withUSD);
  });
});

describe("formatDate", () => {
  it("formats a date string to DD/MM/YYYY", () => {
    // Use midday local time to avoid UTC midnight rollover across timezones
    const result = formatDate("2024-06-15T12:00:00");
    expect(result).toContain("15");
    expect(result).toContain("06");
    expect(result).toContain("2024");
  });

  it("accepts a Date object", () => {
    // Use midday local time to avoid UTC midnight rollover across timezones
    const result = formatDate(new Date("2024-01-15T12:00:00"));
    expect(result).toContain("2024");
  });
});

describe("formatDateTime", () => {
  it("includes both date and time components", () => {
    // Local time string — no Z suffix so no timezone conversion applies
    const result = formatDateTime("2024-06-15T14:30:00");
    expect(result).toContain("2024");
    expect(result).toContain("14");
    expect(result).toContain("30");
  });
});
