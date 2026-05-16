import { EdgeDescriptor, GraphPayload, NodeDescriptor } from "../api/agentClient";

type BoundaryRole = "input" | "output" | "io";

export interface GroupNodeDescriptor extends NodeDescriptor {
  syntheticGroup?: true;
  groupLabel?: string;
  groupMembers?: string[];
  groupSize?: number;
  boundary?: boolean;
  boundaryRole?: BoundaryRole;
}

export interface GroupInfo {
  id: string;
  label: string;
  members: string[];
}

export interface GroupedGraphResult {
  graph: GraphPayload | null;
  groups: Record<string, GroupInfo>;
  memberToGroup: Record<string, string>;
  edgeToRelations: Record<string, string[]>;
}

const MIN_GROUP_SIZE = 8;
const MAX_GROUP_SIZE = 25;

function isBoundaryNode(node: NodeDescriptor): boolean {
  return Boolean((node as GroupNodeDescriptor).boundary);
}

function isCompositeNode(node: NodeDescriptor): boolean {
  const cls = (node.className ?? "").toLowerCase();
  return cls.includes("typedcompositeactor") || cls.endsWith(".compositeactor");
}

function safeToken(raw: string): string {
  return raw.toLowerCase().replace(/[^a-z0-9_]/g, "_").replace(/_+/g, "_");
}

function normalizePrefix(name: string): string {
  const token = safeToken(name);
  const trimmed = token.replace(/_?\d+$/, "");
  if (trimmed.length >= 3) {
    return trimmed;
  }
  return token.length >= 3 ? token : "actor";
}

function classBucket(className: string): string {
  const shortName = className.split(".").pop() ?? className;
  const raw = safeToken(shortName).replace(/_?\d+$/, "");
  return raw || "actor";
}

function chunkMembers(ids: string[]): string[][] {
  if (ids.length < MIN_GROUP_SIZE) {
    return [];
  }
  const chunks: string[][] = [];
  for (let i = 0; i < ids.length; i += MAX_GROUP_SIZE) {
    chunks.push(ids.slice(i, i + MAX_GROUP_SIZE));
  }
  if (chunks.length > 1) {
    const tail = chunks[chunks.length - 1];
    if (tail.length < MIN_GROUP_SIZE) {
      chunks[chunks.length - 2] = chunks[chunks.length - 2].concat(tail);
      chunks.pop();
    }
  }
  return chunks.filter((chunk) => chunk.length >= MIN_GROUP_SIZE);
}

function centroid(nodes: NodeDescriptor[]): { x: number; y: number } {
  if (nodes.length === 0) {
    return { x: 100, y: 100 };
  }
  let x = 0;
  let y = 0;
  nodes.forEach((node) => {
    x += Number.isFinite(node.position?.x) ? node.position.x : 100;
    y += Number.isFinite(node.position?.y) ? node.position.y : 100;
  });
  return {
    x: Math.round(x / nodes.length),
    y: Math.round(y / nodes.length),
  };
}

function groupEntanglement(
  members: Set<string>,
  edges: EdgeDescriptor[],
): { internal: number; external: number } {
  let internal = 0;
  let external = 0;
  edges.forEach((edge) => {
    const src = members.has(edge.source);
    const dst = members.has(edge.target);
    if (src && dst) {
      internal += 1;
    } else if (src || dst) {
      external += 1;
    }
  });
  return { internal, external };
}

export function buildGroupedGraph(
  graph: GraphPayload | null,
  expandedGroupIds: Set<string>,
): GroupedGraphResult {
  if (!graph) {
    return {
      graph: null,
      groups: {},
      memberToGroup: {},
      edgeToRelations: {},
    };
  }

  const nodes = graph.nodes ?? [];
  const edges = graph.edges ?? [];
  const byId = new Map<string, NodeDescriptor>();
  nodes.forEach((node) => byId.set(node.id, node));

  const buckets = new Map<string, string[]>();
  nodes.forEach((node) => {
    if (isBoundaryNode(node) || isCompositeNode(node)) {
      return;
    }
    const prefix = normalizePrefix(node.displayName || node.id);
    const cls = classBucket(node.className || "");
    const key = `${cls}__${prefix}`;
    const list = buckets.get(key) ?? [];
    list.push(node.id);
    buckets.set(key, list);
  });

  const groups: Record<string, GroupInfo> = {};
  const memberToGroup: Record<string, string> = {};
  const groupedMembers = new Set<string>();
  const sortedBucketKeys = Array.from(buckets.keys()).sort((a, b) =>
    a.localeCompare(b),
  );
  sortedBucketKeys.forEach((bucketKey) => {
    const members = (buckets.get(bucketKey) ?? []).slice().sort((a, b) => {
      const an = byId.get(a);
      const bn = byId.get(b);
      const ax = an?.position?.x ?? 0;
      const bx = bn?.position?.x ?? 0;
      if (ax !== bx) {
        return ax - bx;
      }
      const ay = an?.position?.y ?? 0;
      const by = bn?.position?.y ?? 0;
      if (ay !== by) {
        return ay - by;
      }
      return a.localeCompare(b);
    });
    const chunks = chunkMembers(members);
    chunks.forEach((chunk, idx) => {
      const memberSet = new Set(chunk);
      const entanglement = groupEntanglement(memberSet, edges);
      if (entanglement.external > entanglement.internal * 2 + 6) {
        return;
      }
      const groupId = `__group__${bucketKey}__${idx + 1}`;
      groups[groupId] = {
        id: groupId,
        label: bucketKey.replace(/__/g, " "),
        members: chunk,
      };
      if (!expandedGroupIds.has(groupId)) {
        chunk.forEach((id) => {
          memberToGroup[id] = groupId;
          groupedMembers.add(id);
        });
      }
    });
  });

  const visibleNodes: GroupNodeDescriptor[] = [];
  nodes.forEach((node) => {
    if (!groupedMembers.has(node.id)) {
      visibleNodes.push(node as GroupNodeDescriptor);
    }
  });

  Object.values(groups).forEach((group) => {
    if (expandedGroupIds.has(group.id)) {
      return;
    }
    const memberNodes = group.members
      .map((id) => byId.get(id))
      .filter((row): row is NodeDescriptor => Boolean(row));
    const mid = centroid(memberNodes);
    visibleNodes.push({
      id: group.id,
      className: "ptolemy.actor.TypedCompositeActor",
      displayName: `${group.label} (${group.members.length})`,
      position: mid,
      inputs: [{ id: "in", name: "in" } as NodeDescriptor["inputs"][number]],
      outputs: [{ id: "out", name: "out" } as NodeDescriptor["outputs"][number]],
      parameters: [],
      syntheticGroup: true,
      groupLabel: group.label,
      groupMembers: group.members.slice(),
      groupSize: group.members.length,
    });
  });

  const edgeToRelations: Record<string, string[]> = {};
  const mergedEdges = new Map<string, EdgeDescriptor>();
  edges.forEach((edge) => {
    const mappedSource = memberToGroup[edge.source] ?? edge.source;
    const mappedTarget = memberToGroup[edge.target] ?? edge.target;
    if (mappedSource === mappedTarget) {
      return;
    }
    const grouped = mappedSource !== edge.source || mappedTarget !== edge.target;
    const sourceHandle = mappedSource.startsWith("__group__")
      ? "out"
      : edge.sourceHandle;
    const targetHandle = mappedTarget.startsWith("__group__")
      ? "in"
      : edge.targetHandle;
    const dedupeKey = grouped
      ? `${mappedSource}=>${mappedTarget}`
      : edge.id;
    if (!mergedEdges.has(dedupeKey)) {
      mergedEdges.set(dedupeKey, {
        id: grouped ? `__groupedge__${safeToken(dedupeKey)}` : edge.id,
        source: mappedSource,
        sourceHandle,
        target: mappedTarget,
        targetHandle,
      });
    }
    const visibleId = mergedEdges.get(dedupeKey)?.id ?? edge.id;
    const relationIds = edgeToRelations[visibleId] ?? [];
    relationIds.push(edge.id);
    edgeToRelations[visibleId] = relationIds;
  });

  const groupedGraph: GraphPayload = {
    ...graph,
    nodes: visibleNodes as NodeDescriptor[],
    edges: Array.from(mergedEdges.values()),
  };
  return {
    graph: groupedGraph,
    groups,
    memberToGroup,
    edgeToRelations,
  };
}

export function visibleSelectionId(
  selectedNodeId: string | null,
  memberToGroup: Record<string, string>,
): string | null {
  if (!selectedNodeId) {
    return null;
  }
  return memberToGroup[selectedNodeId] ?? selectedNodeId;
}
