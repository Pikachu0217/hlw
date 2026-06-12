import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { DoctorList } from "./DoctorList";

describe("DoctorList", () => {
  it("renders doctor management table", () => {
    render(<DoctorList doctors={[{ id: 1, name: "李医生", title: "主任医师", consultStatus: "ONLINE" }]} />);

    expect(screen.getByText("李医生")).toBeInTheDocument();
    expect(screen.getByText("主任医师")).toBeInTheDocument();
    expect(screen.getByText("ONLINE")).toBeInTheDocument();
  });
});
