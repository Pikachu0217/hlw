import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { AppointmentConfirm } from "./AppointmentConfirm";

describe("AppointmentConfirm", () => {
  it("shows doctor schedule and fee before submit", () => {
    render(
      <MemoryRouter>
        <AppointmentConfirm doctorName="李医生" timeSlot="上午" fee="50.00" />
      </MemoryRouter>
    );

    expect(screen.getByText("李医生")).toBeInTheDocument();
    expect(screen.getByText("上午")).toBeInTheDocument();
    expect(
      screen.getByText((_, node) => node?.classList.contains("detail-copy") === true && node.textContent === "挂号费 50.00")
    ).toBeInTheDocument();
  });
});
