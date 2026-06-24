import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ConsultChat } from "./ConsultChat";

describe("ConsultChat", () => {
  it("renders remaining time and messages", () => {
    render(
      <ConsultChat
        doctorName="王医生"
        remainingSeconds={300}
        messages={[{ id: 1, content: "哪里不舒服", contentType: "TEXT" }]}
        textMessage=""
        canSend
        onTextChange={() => undefined}
        onSendText={() => undefined}
        onSendImage={() => undefined}
      />
    );

    expect(screen.getByText("05:00")).toBeInTheDocument();
    expect(screen.getByText("哪里不舒服")).toBeInTheDocument();
    expect(screen.getByText("问诊中")).toBeInTheDocument();
  });
});
