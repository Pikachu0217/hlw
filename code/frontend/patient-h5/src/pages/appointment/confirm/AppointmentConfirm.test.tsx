import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AppointmentConfirm } from "./AppointmentConfirm";

describe("AppointmentConfirm", () => {
  it("shows doctor schedule and fee before submit", () => {
    render(<AppointmentConfirm doctorName="李医生" timeSlot="上午" fee="50.00" />);

    expect(screen.getByText("李医生")).toBeInTheDocument();
    expect(screen.getByText("上午")).toBeInTheDocument();
    expect(screen.getByText("50.00")).toBeInTheDocument();
  });
});
