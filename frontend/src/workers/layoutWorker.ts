import { layoutNodes, LayoutOpts } from "../utils/autoLayout";

interface LayoutWorkerRequest {
  id: number;
  nodes: Array<Record<string, unknown>>;
  edges: Array<Record<string, unknown>>;
  options?: LayoutOpts;
}

interface LayoutWorkerResponse {
  id: number;
  ok: boolean;
  nodes?: Array<Record<string, unknown>>;
  error?: string;
}

self.onmessage = (event: MessageEvent<LayoutWorkerRequest>) => {
  const req = event.data;
  const res: LayoutWorkerResponse = { id: req.id, ok: false };
  try {
    const laidOut = layoutNodes(
      req.nodes as never[],
      req.edges as never[],
      req.options ?? {},
    );
    res.ok = true;
    res.nodes = laidOut as Array<Record<string, unknown>>;
  } catch (error) {
    res.error = error instanceof Error ? error.message : "layout worker failed";
  }
  self.postMessage(res);
};

export {};
