import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ConsultChat } from "./ConsultChat";

describe("ConsultChat", () => {
  it("renders remaining time and messages", () => {
    render(
      <ConsultChat
        remainingSeconds={300}
        messages={[{ id: 1, content: "哪里不舒服", contentType: "TEXT" }]}
        textMessage=""
        imageUrl=""
        canSend
        onTextChange={() => undefined}
        onImageUrlChange={() => undefined}
        onSendText={() => undefined}
        onSendImage={() => undefined}
      />
    );

    expect(screen.getByText((_, node) => node?.textContent === "剩余时间 05:00")).toBeInTheDocument();
    expect(screen.getByText("哪里不舒服")).toBeInTheDocument();
  });
});
