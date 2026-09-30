import express from "express";
import path from "path";
import fs from "fs";
import { createServer as createViteServer } from "vite";

async function startServer() {
  const app = express();
  const PORT = 3000;

  app.use(express.json({ limit: "10mb" }));

  // API proxy endpoint for Google Apps Script to bypass CORS limitations in browser
  app.post("/api/sheets/proxy", async (req, res) => {
    try {
      const { scriptUrl, payload, method } = req.body;
      if (!scriptUrl) {
        return res.status(400).json({ error: "scriptUrl is required" });
      }

      const options: RequestInit = {
        method: method || "POST",
        headers: {
          "Content-Type": "application/json",
        },
        redirect: "follow",
      };

      if (payload && method !== "GET") {
        options.body = JSON.stringify(payload);
      }

      let fetchUrl = scriptUrl;
      if (method === "GET" && payload) {
        const params = new URLSearchParams(payload).toString();
        fetchUrl += (scriptUrl.includes("?") ? "&" : "?") + params;
      }

      const response = await fetch(fetchUrl, options);
      const text = await response.text();

      let data;
      try {
        data = JSON.parse(text);
      } catch {
        data = { responseText: text };
      }

      return res.json({ success: true, status: response.status, data });
    } catch (error: any) {
      console.error("Error in sheets proxy:", error);
      return res.status(500).json({ error: error.message || "Proxy request failed" });
    }
  });

  const distPath = path.join(process.cwd(), "dist");
  const isProd = process.env.NODE_ENV === "production" || fs.existsSync(path.join(distPath, "index.html"));

  if (!isProd) {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://0.0.0.0:${PORT}`);
  });
}

startServer().catch((err) => {
  console.error("Failed to start server:", err);
});
