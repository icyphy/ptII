// Layered (Sugiyama-lite) layout that arranges actors left-to-right in
// the order they fire under SDF / DE / Continuous semantics: rank 0
// holds sources (no predecessors), rank N holds everything reachable
// only after N earlier stages, and actors that fire in parallel share
// a rank.
//
// Algorithm
// ─────────
//  1) Rank by longest forward path from any predecessor-free node.
//     Back-edges (cycles) are ignored during ranking but still drawn.
//  2) Force composite boundary inputs to rank 0 and boundary outputs
//     to the rightmost rank so the canvas reads "in → … → out".
//  3) Insert DUMMY NODES on every edge that spans more than one rank.
//     Dummies participate in barycenter ordering and Y assignment but
//     render as nothing — they keep long edges visually straight by
//     reserving a vertical slot at every intermediate rank, so the
//     source actor doesn't end up far above/below its eventual
//     consumer.
//  4) Barycenter sweep (forward + backward, several iterations) over
//     the augmented graph to reduce edge crossings.
//  5) Y assignment by PREDECESSOR-ALIGNED MEDIAN: each node tries to
//     sit at the median Y of its predecessors. In-rank overlaps are
//     resolved by pushing later nodes downward with a fixed gap.
//
// Inputs are React-Flow nodes/edges. Output: a new node array with
// fresh `position` values; edges are unchanged.

import type { Edge, Node } from "reactflow";

export interface LayoutOpts {
  colGap?:    number;  // horizontal gap between columns
  rowGap?:    number;  // vertical gap between stacked nodes in a column
  leftPad?:   number;
  topPad?:    number;
  iterations?: number; // barycenter sweep iterations
  /** Fast mode for very large graphs: fewer iterations, less crossing work. */
  lightweight?: boolean;
}

interface PortAware {
  isBoundary?:   boolean;
  boundaryRole?: string;
  isComposite?:  boolean;
  inputs?:       { id: string; name: string }[];
  outputs?:      { id: string; name: string }[];
}

// Visual constants — these MUST match what ActorNode renders or
// nodes will overlap.
const HEADER_H        = 58;
const PORT_ROW_H      = 22;
const PORT_PAD_TOTAL  = 12;
const ATOM_WIDTH      = 176;
const COMPOSITE_WIDTH = 196;
const BOUNDARY_WIDTH  = 110;
const BOUNDARY_HEIGHT = 60;
const LARGE_GRAPH_THRESHOLD = 220;
const LIGHTWEIGHT_ITERATIONS = 3;
const MAX_DUMMY_SPAN_LIGHTWEIGHT = 4;

function nodeHeight<D extends PortAware>(n: Node<D>): number {
  if (n.data?.isBoundary) return BOUNDARY_HEIGHT;
  const rows = Math.max(
    n.data?.inputs?.length ?? 0,
    n.data?.outputs?.length ?? 0,
    1,
  );
  return HEADER_H + PORT_PAD_TOTAL + rows * PORT_ROW_H;
}

function nodeWidth<D extends PortAware>(n: Node<D>): number {
  if (n.data?.isBoundary) return BOUNDARY_WIDTH;
  return n.data?.isComposite ? COMPOSITE_WIDTH : ATOM_WIDTH;
}

export function layoutNodes<D extends PortAware>(
  nodes: Node<D>[],
  edges: Edge[],
  opts: LayoutOpts = {},
): Node<D>[] {
  if (nodes.length === 0) return nodes;

  const COL_GAP    = opts.colGap     ?? 60;
  const ROW_GAP    = opts.rowGap     ?? 28;
  const LEFT       = opts.leftPad    ?? 40;
  const TOP        = opts.topPad     ?? 40;
  const lightweight = opts.lightweight === true
    || nodes.length >= LARGE_GRAPH_THRESHOLD;
  const ITERATIONS = opts.iterations
    ?? (lightweight ? LIGHTWEIGHT_ITERATIONS : 8);

  const idToNode = new Map<string, Node<D>>();
  nodes.forEach((n) => idToNode.set(n.id, n));

  // ── 1) Build adjacency on the REAL graph (dedup multi-edges) ────
  const rSucc: Record<string, Set<string>> = {};
  const rPred: Record<string, Set<string>> = {};
  nodes.forEach((n) => { rSucc[n.id] = new Set(); rPred[n.id] = new Set(); });
  edges.forEach((e) => {
    if (rSucc[e.source] && rPred[e.target] && e.source !== e.target) {
      rSucc[e.source].add(e.target);
      rPred[e.target].add(e.source);
    }
  });

  const isInputBoundary  = (n: Node<D>) =>
    n.data?.isBoundary === true && n.data?.boundaryRole === "input";
  const isOutputBoundary = (n: Node<D>) =>
    n.data?.isBoundary === true && n.data?.boundaryRole === "output";
  const inputBoundaryIds = nodes.filter(isInputBoundary).map((n) => n.id);
  const outputBoundaryIds = nodes.filter(isOutputBoundary).map((n) => n.id);
  const inputBoundarySet = new Set(inputBoundaryIds);
  const outputBoundarySet = new Set(outputBoundaryIds);
  const isBoundaryId = (id: string) =>
    inputBoundarySet.has(id) || outputBoundarySet.has(id);

  // ── 2) Rank by longest forward path ─────────────────────────────
  //
  // Important: physical / control models often contain feedback loops
  // (Integrator.output -> AddSubtract.minus, PID loops, recurrent
  // cells). A naive iterative relaxation keeps increasing ranks around
  // a cycle until an arbitrary safety cap, which pushes nodes thousands
  // of pixels to the right. Use DFS with a `visiting` guard instead:
  // when we see a back-edge, ignore that edge for ranking. The edge is
  // still drawn; it simply does not stretch the rank space.
  const rank: Record<string, number> = {};
  nodes.forEach((n) => { rank[n.id] = 0; });

  const visiting = new Set<string>();
  const visited  = new Set<string>();
  function rankOf(id: string): number {
    if (visited.has(id)) return rank[id];
    if (visiting.has(id)) return rank[id]; // back-edge/cycle: ignore
    visiting.add(id);
    let r = 0;
    for (const p of rPred[id] ?? []) {
      r = Math.max(r, rankOf(p) + 1);
    }
    rank[id] = r;
    visiting.delete(id);
    visited.add(id);
    return r;
  }
  nodes.forEach((n) => rankOf(n.id));

  // Pin boundaries to dedicated leading/trailing columns:
  //  - all input boundary ports in the first column
  //  - all output boundary ports in the last column
  // This guarantees "input rail → graph body → output rail".
  let maxRank = 0;
  Object.values(rank).forEach((r) => { if (r > maxRank) maxRank = r; });
  const hasInputBoundary = inputBoundaryIds.length > 0;
  const hasOutputBoundary = outputBoundaryIds.length > 0;
  if (hasInputBoundary) {
    nodes.forEach((n) => {
      if (!isBoundaryId(n.id)) rank[n.id] += 1;
    });
    maxRank += 1;
    inputBoundaryIds.forEach((id) => { rank[id] = 0; });
  }
  if (hasOutputBoundary) {
    const outRank = maxRank + 1;
    outputBoundaryIds.forEach((id) => { rank[id] = outRank; });
    maxRank = outRank;
  }
  const inputBoundaryRank = hasInputBoundary ? 0 : -1;
  const outputBoundaryRank = hasOutputBoundary ? maxRank : -1;
  const isFrozenRank = (r: number) =>
    r === inputBoundaryRank || r === outputBoundaryRank;

  // ── 3) Insert dummy nodes on long edges ────────────────────────
  //  Augmented graph:
  //    - vertices = real nodes + dummies
  //    - dummies have height 0, width 0, no rendered output
  //    - every original edge u→v with |rank(v) − rank(u)| > 1 is
  //      split into a chain u → d_1 → … → d_k → v passing through
  //      each intermediate rank.
  interface Vertex { id: string; rank: number; isDummy: boolean; }
  const vmap = new Map<string, Vertex>();
  nodes.forEach((n) => vmap.set(n.id, {
    id: n.id, rank: rank[n.id], isDummy: false,
  }));

  const aSucc: Record<string, Set<string>> = {};
  const aPred: Record<string, Set<string>> = {};
  function aLink(u: string, v: string) {
    (aSucc[u] ??= new Set()).add(v);
    (aPred[v] ??= new Set()).add(u);
  }
  nodes.forEach((n) => { aSucc[n.id] = new Set(); aPred[n.id] = new Set(); });

  let dummyCount = 0;
  edges.forEach((e) => {
    if (!vmap.has(e.source) || !vmap.has(e.target)) return;
    if (e.source === e.target) return;
    const ru = rank[e.source];
    const rv = rank[e.target];
    if (rv > ru + 1) {
      // forward long edge — chain dummies through ranks
      const span = rv - ru - 1;
      if (lightweight && span > MAX_DUMMY_SPAN_LIGHTWEIGHT) {
        aLink(e.source, e.target);
        return;
      }
      let prev = e.source;
      for (let r = ru + 1; r < rv; r++) {
        const did = `__dummy__${dummyCount++}`;
        vmap.set(did, { id: did, rank: r, isDummy: true });
        aSucc[did] = new Set();
        aPred[did] = new Set();
        aLink(prev, did);
        prev = did;
      }
      aLink(prev, e.target);
    } else if (ru === rv || ru > rv) {
      // same-rank or back-edge: keep as a single direct adjacency, no
      // dummies. Visual edge is still drawn; it just won't influence
      // barycenter through intermediate ranks.
      aLink(e.source, e.target);
    } else {
      // short edge (consecutive ranks): plain link.
      aLink(e.source, e.target);
    }
  });

  // Group vertex IDs by rank (real + dummy).
  const byRank: Record<number, string[]> = {};
  for (let r = 0; r <= maxRank; r++) byRank[r] = [];
  vmap.forEach((v) => byRank[v.rank].push(v.id));
  const ranks = Object.keys(byRank).map(Number).sort((a, b) => a - b);

  // Initial in-rank order: alphabetical (stable starting point).
  ranks.forEach((r) => { byRank[r].sort((a, b) => a.localeCompare(b)); });

  // ── 4) Barycenter sweeps over the AUGMENTED graph ──────────────
  const slot: Record<string, number> = {};
  function reslot() {
    ranks.forEach((r) => {
      byRank[r].forEach((id, i) => { slot[id] = i; });
    });
  }
  reslot();
  function meanSlot(ids: Iterable<string>): number | null {
    let s = 0, c = 0;
    for (const id of ids) {
      const sl = slot[id];
      if (typeof sl === "number") { s += sl; c++; }
    }
    return c === 0 ? null : s / c;
  }

  // Count crossings only on forward adjacent edges (r -> r+1),
  // which is what dummy-expanded layered layout optimizes.
  function crossingsBetween(upperRank: number, lowerRank: number): number {
    if (lowerRank !== upperRank + 1) return 0;
    const upper = byRank[upperRank] ?? [];
    const lower = byRank[lowerRank] ?? [];
    if (upper.length === 0 || lower.length === 0) return 0;
    const upPos: Record<string, number> = {};
    const loPos: Record<string, number> = {};
    upper.forEach((id, i) => { upPos[id] = i; });
    lower.forEach((id, i) => { loPos[id] = i; });
    const pairs: Array<{ u: number; v: number }> = [];
    for (const uId of upper) {
      const succ = aSucc[uId] ?? new Set<string>();
      for (const vId of succ) {
        const v = vmap.get(vId);
        if (!v || v.rank !== lowerRank) continue;
        const u = upPos[uId];
        const w = loPos[vId];
        if (typeof u === "number" && typeof w === "number") {
          pairs.push({ u, v: w });
        }
      }
    }
    let c = 0;
    for (let i = 0; i < pairs.length; i++) {
      for (let j = i + 1; j < pairs.length; j++) {
        const a = pairs[i], b = pairs[j];
        if ((a.u - b.u) * (a.v - b.v) < 0) c++;
      }
    }
    return c;
  }
  function crossingCostAtRank(r: number): number {
    let c = 0;
    if (r > 0) c += crossingsBetween(r - 1, r);
    if (r < maxRank) c += crossingsBetween(r, r + 1);
    return c;
  }
  function transposeRank(r: number) {
    if (isFrozenRank(r)) return;
    const ids = byRank[r] ?? [];
    if (ids.length < 2) return;
    let improved = true;
    while (improved) {
      improved = false;
      for (let i = 0; i < ids.length - 1; i++) {
        const a = ids[i], b = ids[i + 1];
        const before = crossingCostAtRank(r);
        ids[i] = b;
        ids[i + 1] = a;
        reslot();
        const after = crossingCostAtRank(r);
        if (after < before) {
          improved = true;
        } else {
          ids[i] = a;
          ids[i + 1] = b;
          reslot();
        }
      }
    }
  }

  for (let pass = 0; pass < ITERATIONS; pass++) {
    for (let i = 1; i < ranks.length; i++) {
      const r = ranks[i];
      if (isFrozenRank(r)) continue;
      byRank[r].sort((a, b) => {
        const ma = meanSlot(aPred[a] ?? []);
        const mb = meanSlot(aPred[b] ?? []);
        if (ma == null && mb == null) return a < b ? -1 : a > b ? 1 : 0;
        if (ma == null) return  1;
        if (mb == null) return -1;
        if (ma !== mb)  return ma - mb;
        return a < b ? -1 : a > b ? 1 : 0;
      });
      reslot();
      if (!lightweight) {
        transposeRank(r);
      }
    }
    reslot();
    for (let i = ranks.length - 2; i >= 0; i--) {
      const r = ranks[i];
      if (isFrozenRank(r)) continue;
      byRank[r].sort((a, b) => {
        const ma = meanSlot(aSucc[a] ?? []);
        const mb = meanSlot(aSucc[b] ?? []);
        if (ma == null && mb == null) return a < b ? -1 : a > b ? 1 : 0;
        if (ma == null) return  1;
        if (mb == null) return -1;
        if (ma !== mb)  return ma - mb;
        return a < b ? -1 : a > b ? 1 : 0;
      });
      reslot();
      if (!lightweight) {
        transposeRank(r);
      }
    }
    reslot();
  }

  // ── 5) Geometry ────────────────────────────────────────────────
  // Column width (real nodes only — dummies have width 0).
  const colWidth: Record<number, number> = {};
  ranks.forEach((r) => {
    let w = 0;
    for (const id of byRank[r]) {
      const v = vmap.get(id);
      if (!v || v.isDummy) continue;
      const n = idToNode.get(id);
      if (!n) continue;
      const nw = nodeWidth(n);
      if (nw > w) w = nw;
    }
    if (w === 0) w = ATOM_WIDTH; // pure-dummy column
    colWidth[r] = w;
  });
  const colX: Record<number, number> = {};
  let xCursor = LEFT;
  ranks.forEach((r) => {
    colX[r] = xCursor;
    xCursor += colWidth[r] + COL_GAP;
  });

  // Y assignment — predecessor-median with in-column overlap fix.
  const y: Record<string, number> = {};
  function vheight(id: string): number {
    const v = vmap.get(id);
    if (!v) return 0;
    if (v.isDummy) return 0;
    const n = idToNode.get(id);
    return n ? nodeHeight(n) : 0;
  }
  function median(values: number[]): number {
    if (values.length === 0) return 0;
    const s = values.slice().sort((a, b) => a - b);
    const m = s.length / 2;
    return s.length % 2 === 0 ? (s[m - 1] + s[m]) / 2 : s[Math.floor(m)];
  }

  // First pass for non-boundary ranks.
  for (let r = 0; r <= maxRank; r++) {
    if (isFrozenRank(r)) continue;
    const ids = byRank[r];
    if (ids.length === 0) continue;
    const target: Record<string, number> = {};
    for (const id of ids) {
      const preds = aPred[id] ?? new Set<string>();
      const ys: number[] = [];
      for (const p of preds) {
        if (typeof y[p] === "number") ys.push(y[p]);
      }
      target[id] = ys.length > 0 ? median(ys) : 0;
    }
    // Sort within rank by target Y (already close to barycenter order).
    ids.sort((a, b) => target[a] - target[b]);

    // Place top-down with overlap resolution.
    let cursor = -Infinity;
    for (const id of ids) {
      const h  = vheight(id);
      const tg = target[id];
      let ny: number;
      if (cursor === -Infinity) {
        ny = tg;
      } else {
        ny = Math.max(tg, cursor + ROW_GAP);
      }
      y[id] = ny;
      cursor = ny + h;
    }
  }

  // Any not-yet-placed vertices (mostly dummies tied to frozen columns).
  for (let r = 0; r <= maxRank; r++) {
    const ids = byRank[r];
    let cursor = -Infinity;
    for (const id of ids) {
      if (typeof y[id] === "number") {
        cursor = y[id] + vheight(id);
        continue;
      }
      const preds = aPred[id] ?? new Set<string>();
      const ys: number[] = [];
      for (const p of preds) {
        if (typeof y[p] === "number") ys.push(y[p]);
      }
      const tg = ys.length > 0 ? median(ys) : 0;
      const ny = cursor === -Infinity ? tg : Math.max(tg, cursor + ROW_GAP);
      y[id] = ny;
      cursor = ny + vheight(id);
    }
  }

  // Reorder and stack boundary rails by neighboring actor Y:
  // inputs by successor median, outputs by predecessor median.
  function placeBoundaryRail(ids: string[], useSuccessors: boolean) {
    if (ids.length === 0) return;
    const scored = ids.map((id) => {
      const neigh = useSuccessors ? rSucc[id] : rPred[id];
      const ys: number[] = [];
      for (const nb of neigh ?? []) {
        if (typeof y[nb] === "number") ys.push(y[nb]);
      }
      const score = ys.length > 0 ? median(ys) : 0;
      return { id, score };
    });
    scored.sort((a, b) => {
      if (a.score !== b.score) return a.score - b.score;
      return a.id.localeCompare(b.id);
    });
    let cursor = -Infinity;
    for (const row of scored) {
      const h = vheight(row.id);
      const ny = cursor === -Infinity
        ? row.score
        : Math.max(row.score, cursor + ROW_GAP);
      y[row.id] = ny;
      cursor = ny + h;
    }
  }
  placeBoundaryRail(inputBoundaryIds, true);
  placeBoundaryRail(outputBoundaryIds, false);

  // Normalize so the highest real node sits at TOP.
  let minY = Infinity;
  for (const v of vmap.values()) {
    if (v.isDummy) continue;
    if (y[v.id] < minY) minY = y[v.id];
  }
  if (minY === Infinity) minY = 0;
  const yShift = TOP - minY;

  // Build final position map for REAL nodes only.
  const out = new Map<string, { x: number; y: number }>();
  ranks.forEach((r) => {
    for (const id of byRank[r]) {
      const v = vmap.get(id);
      if (!v || v.isDummy) continue;
      const n = idToNode.get(id);
      if (!n) continue;
      const w = nodeWidth(n);
      const x = colX[r] + (colWidth[r] - w) / 2;
      out.set(id, { x, y: y[id] + yShift });
    }
  });

  return nodes.map((n) => {
    const p = out.get(n.id);
    return p ? { ...n, position: p } : n;
  });
}

/** Heuristic for "should we auto-relayout this freshly-loaded graph?".
 *  True iff (a) many nodes still sit at the agent's default (100,100)
 *  spawn point, or (b) any two nodes' axis-aligned rectangles overlap. */
export function isLayoutCluttered<D extends PortAware>(
  nodes: Node<D>[],
  opts: {
    skipOverlapWhenLarge?: boolean;
    largeNodeThreshold?: number;
    maxPairChecks?: number;
  } = {},
): boolean {
  if (nodes.length < 3) return false;

  let defaultLike = 0;
  for (const n of nodes) {
    const x = Math.round(n.position?.x ?? 0);
    const y = Math.round(n.position?.y ?? 0);
    if (Math.abs(x - 100) <= 2 && Math.abs(y - 100) <= 2) defaultLike++;
  }
  if (defaultLike >= 3) return true;

  const skipOverlapWhenLarge = opts.skipOverlapWhenLarge ?? false;
  const largeNodeThreshold = opts.largeNodeThreshold ?? LARGE_GRAPH_THRESHOLD;
  if (skipOverlapWhenLarge && nodes.length >= largeNodeThreshold) {
    return false;
  }

  type Rect = { x: number; y: number; w: number; h: number };
  const rects: Rect[] = nodes.map((n) => ({
    x: n.position?.x ?? 0,
    y: n.position?.y ?? 0,
    w: nodeWidth(n),
    h: nodeHeight(n),
  }));
  const maxPairChecks = opts.maxPairChecks ?? Number.POSITIVE_INFINITY;
  let pairChecks = 0;
  for (let i = 0; i < rects.length; i++) {
    for (let j = i + 1; j < rects.length; j++) {
      pairChecks += 1;
      if (pairChecks > maxPairChecks) {
        return false;
      }
      const a = rects[i], b = rects[j];
      if (a.x < b.x + b.w && a.x + a.w > b.x &&
          a.y < b.y + b.h && a.y + a.h > b.y) {
        return true;
      }
    }
  }
  return false;
}
