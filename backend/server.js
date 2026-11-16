import express from "express";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { z } from "zod";

const app = express();
app.use(express.json({ limit: "1mb" }));
const server = new McpServer({ name: "hypergpt", version: "0.1.0" });

server.registerTool("workspace_status", {
  title: "Workspace status",
  description: "Return HyperGPT backend status. Does not expose local tablet files.",
  inputSchema: {}
}, async () => ({
  content: [{ type: "text", text: JSON.stringify({ status: "online", localDevice: "not-paired", version: "0.1.0" }) }]
}));

server.registerTool("draft_ability", {
  title: "Draft an ability",
  description: "Create a non-executable ability specification for owner review.",
  inputSchema: {
    name: z.string().min(2).max(80),
    description: z.string().min(5).max(500),
    permissionScope: z.enum(["read_only", "workspace_write", "network", "shell_approval"])
  }
}, async ({ name, description, permissionScope }) => ({
  content: [{ type: "text", text: JSON.stringify({
    id: `draft-${Date.now()}`, name, description, permissionScope,
    status: "draft_requires_owner_review", executable: false
  }) }]
}));

app.get("/health", (_req, res) => res.json({ ok: true, service: "hypergpt-api", version: "0.1.0" }));
app.get("/openapi.json", (_req, res) => res.sendFile(new URL("../integrations/openapi.json", import.meta.url).pathname));

app.post("/mcp", async (req, res) => {
  // Production deployment must add authentication, per-device authorization,
  // request limits, audit logging and an origin policy before public exposure.
  const transport = new StreamableHTTPServerTransport({ sessionIdGenerator: undefined });
  res.on("close", () => transport.close());
  await server.connect(transport);
  await transport.handleRequest(req, res, req.body);
});

const port = Number(process.env.PORT || 8787);
app.listen(port, () => console.log(`HyperGPT backend listening on ${port}`));
