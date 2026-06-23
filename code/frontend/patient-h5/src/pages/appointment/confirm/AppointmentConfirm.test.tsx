import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { AppointmentConfirm } from "./AppointmentConfirm";

describe("AppointmentConfirm", () => {
  it("shows doctor schedule and fee before submit", () => {
    render(
      <MemoryRouter>
        <AppointmentConfirm
          doctor={{
            id: 1,
            name: "李医生",
            title: "主任医师",
            department: "儿科",
            specialty: "儿童常见病",
            status: "在线",
            consultStatus: "ONLINE",
            consultFee: "50.00",
            schedule: "上午"
          }}
          patient={{
            id: 1,
            patientName: "王小雨",
            maskedPhone: "138****0000",
            gender: "女"
          }}
          schedules={[{
            id: 1,
            doctorId: 1,
            doctorName: "李医生",
            slot: "上午",
            scheduleDate: "2026-06-23",
            timeSlot: "上午",
            totalNumber: 20,
            remain: 8
          }]}
          numberSources={[]}
          lockedSource={null}
          onLockNumberSource={async () => undefined}
        />
      </MemoryRouter>
    );

    expect(screen.getByText("李医生")).toBeInTheDocument();
    expect(screen.getByText("2026-06-23 上午｜余号 8")).toBeInTheDocument();
    expect(
      screen.getByText((_, node) => node?.classList.contains("detail-copy") === true && node.textContent === "挂号费 50.00 元")
    ).toBeInTheDocument();
  });
});
