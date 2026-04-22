import { useState } from 'react';
import { useParams, useLocation } from 'wouter';
import type { GraphTraversalRequest, GraphTraversalResponse } from '../types';
import { traverseGraph, findShortestPath, extractSubgraph } from '../api/client';

function GraphTraversal() {
  const params = useParams();
  const ontologyId = params.id;
  const [, setLocation] = useLocation();
  const [activeTab, setActiveTab] = useState<'traverse' | 'path' | 'subgraph'>('traverse');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState<GraphTraversalResponse | null>(null);

  // Traverse form
  const [traverseForm, setTraverseForm] = useState<GraphTraversalRequest>({
    startObjectType: '',
    startObjectId: '',
    maxDepth: 3,
    direction: 'OUTGOING',
    limit: 100,
    returnFormat: 'GRAPH',
  });

  // Path form
  const [pathForm, setPathForm] = useState({ from: '', to: '', maxDepth: 5 });

  // Subgraph form
  const [subgraphForm, setSubgraphForm] = useState({ root: '', depth: 3 });

  const handleTraverse = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ontologyId) return;
    setLoading(true);
    setError('');
    try {
      const data = await traverseGraph(ontologyId, traverseForm);
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '查询失败');
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const handleFindPath = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ontologyId) return;
    setLoading(true);
    setError('');
    try {
      const data = await findShortestPath(ontologyId, pathForm.from, pathForm.to, pathForm.maxDepth);
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '查询失败');
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const handleExtractSubgraph = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ontologyId) return;
    setLoading(true);
    setError('');
    try {
      const data = await extractSubgraph(ontologyId, subgraphForm.root, subgraphForm.depth);
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '查询失败');
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const renderResult = () => {
    if (!result) return null;

    if (!result.success) {
      return (
        <div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-lg">
          <h4 className="font-medium text-red-700">查询失败</h4>
          <p className="text-sm text-red-600 mt-1">{result.errorMessage || '未知错误'}</p>
        </div>
      );
    }

    return (
      <div className="mt-6 space-y-4">
        <div className="bg-white rounded-lg border border-gray-200 p-4">
          <h4 className="font-medium text-gray-900 mb-3">查询结果</h4>
          <div className="grid grid-cols-4 gap-4 text-sm">
            <div className="bg-blue-50 p-3 rounded-lg">
              <span className="text-gray-500">节点数</span>
              <p className="text-xl font-bold text-blue-600">{result.nodes.length}</p>
            </div>
            <div className="bg-green-50 p-3 rounded-lg">
              <span className="text-gray-500">边数</span>
              <p className="text-xl font-bold text-green-600">{result.edges.length}</p>
            </div>
            <div className="bg-purple-50 p-3 rounded-lg">
              <span className="text-gray-500">路径数</span>
              <p className="text-xl font-bold text-purple-600">{result.paths.length}</p>
            </div>
            <div className="bg-orange-50 p-3 rounded-lg">
              <span className="text-gray-500">耗时</span>
              <p className="text-xl font-bold text-orange-600">{result.executionTimeMs}ms</p>
            </div>
          </div>
        </div>

        {result.nodes.length > 0 && (
          <div className="bg-white rounded-lg border border-gray-200 p-4">
            <h4 className="font-medium text-gray-900 mb-3">节点列表</h4>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">ID</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">对象类型</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">对象ID</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">深度</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {result.nodes.map((node) => (
                    <tr key={node.id}>
                      <td className="px-3 py-2 font-mono text-xs">{node.id}</td>
                      <td className="px-3 py-2">{node.objectType}</td>
                      <td className="px-3 py-2 font-mono text-xs">{node.objectId}</td>
                      <td className="px-3 py-2">
                        <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded text-xs">
                          {node.depth}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {result.edges.length > 0 && (
          <div className="bg-white rounded-lg border border-gray-200 p-4">
            <h4 className="font-medium text-gray-900 mb-3">边列表</h4>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">关系类型</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">源节点</th>
                    <th className="px-3 py-2 text-left font-medium text-gray-500">目标节点</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {result.edges.map((edge) => (
                    <tr key={edge.id}>
                      <td className="px-3 py-2">
                        <span className="bg-purple-100 text-purple-800 px-2 py-0.5 rounded text-xs">
                          {edge.relationType}
                        </span>
                      </td>
                      <td className="px-3 py-2 font-mono text-xs">{edge.sourceId}</td>
                      <td className="px-3 py-2 font-mono text-xs">{edge.targetId}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {result.paths.length > 0 && (
          <div className="bg-white rounded-lg border border-gray-200 p-4">
            <h4 className="font-medium text-gray-900 mb-3">路径</h4>
            <div className="space-y-2">
              {result.paths.map((path) => (
                <div key={path.pathId} className="bg-gray-50 p-3 rounded-lg">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="text-xs text-gray-500">深度 {path.depth}:</span>
                    {path.nodeIds.map((nodeId, i) => (
                      <span key={i} className="flex items-center gap-2">
                        <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded text-xs font-mono">
                          {nodeId}
                        </span>
                        {i < path.nodeIds.length - 1 && (
                          <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                          </svg>
                        )}
                      </span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  };

  return (
    <div>
      <div className="mb-4">
        <button
          onClick={() => setLocation(`/ontologies/${ontologyId}`)}
          className="text-blue-600 hover:text-blue-700 flex items-center gap-1"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          返回本体详情
        </button>
      </div>

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">图遍历查询</h2>
        <p className="text-gray-500 mt-1">查询本体图谱中的节点关系和路径</p>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          {error}
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200">
        {/* Tabs */}
        <div className="border-b border-gray-200">
          <nav className="flex">
            <button
              onClick={() => { setActiveTab('traverse'); setResult(null); setError(''); }}
              className={`px-6 py-3 text-sm font-medium border-b-2 ${
                activeTab === 'traverse'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              图遍历
            </button>
            <button
              onClick={() => { setActiveTab('path'); setResult(null); setError(''); }}
              className={`px-6 py-3 text-sm font-medium border-b-2 ${
                activeTab === 'path'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              最短路径
            </button>
            <button
              onClick={() => { setActiveTab('subgraph'); setResult(null); setError(''); }}
              className={`px-6 py-3 text-sm font-medium border-b-2 ${
                activeTab === 'subgraph'
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              子图提取
            </button>
          </nav>
        </div>

        <div className="p-6">
          {activeTab === 'traverse' && (
            <form onSubmit={handleTraverse} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    起始对象类型 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={traverseForm.startObjectType}
                    onChange={(e) => setTraverseForm({ ...traverseForm, startObjectType: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    placeholder="如：Product"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    起始对象ID <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={traverseForm.startObjectId}
                    onChange={(e) => setTraverseForm({ ...traverseForm, startObjectId: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    placeholder="如：550e8400-e29b-41d4-a716-446655440000"
                    pattern="^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
                    title="请输入 UUID 格式的对象 ID"
                    required
                  />
                </div>
              </div>
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">最大深度</label>
                  <input
                    type="number"
                    value={traverseForm.maxDepth}
                    onChange={(e) => setTraverseForm({ ...traverseForm, maxDepth: parseInt(e.target.value) })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    min={1}
                    max={5}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">方向</label>
                  <select
                    value={traverseForm.direction}
                    onChange={(e) => setTraverseForm({ ...traverseForm, direction: e.target.value as 'OUTGOING' | 'INCOMING' | 'BOTH' })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="OUTGOING">出向</option>
                    <option value="INCOMING">入向</option>
                    <option value="BOTH">双向</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">返回数量限制</label>
                  <input
                    type="number"
                    value={traverseForm.limit}
                    onChange={(e) => setTraverseForm({ ...traverseForm, limit: parseInt(e.target.value) })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    min={1}
                    max={1000}
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">返回格式</label>
                <select
                  value={traverseForm.returnFormat}
                  onChange={(e) => setTraverseForm({ ...traverseForm, returnFormat: e.target.value as 'GRAPH' | 'TREE' | 'FLAT' })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                >
                  <option value="GRAPH">图结构</option>
                  <option value="TREE">树结构</option>
                  <option value="FLAT">扁平列表</option>
                </select>
              </div>
              <button
                type="submit"
                disabled={loading}
                className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
              >
                {loading && <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>}
                执行遍历
              </button>
            </form>
          )}

          {activeTab === 'path' && (
            <form onSubmit={handleFindPath} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    起始节点ID <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={pathForm.from}
                    onChange={(e) => setPathForm({ ...pathForm, from: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    placeholder="如：node-001"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    目标节点ID <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={pathForm.to}
                    onChange={(e) => setPathForm({ ...pathForm, to: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    placeholder="如：node-002"
                    required
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">最大深度</label>
                <input
                  type="number"
                  value={pathForm.maxDepth}
                  onChange={(e) => setPathForm({ ...pathForm, maxDepth: parseInt(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  min={1}
                  max={10}
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 disabled:opacity-50 flex items-center gap-2"
              >
                {loading && <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>}
                查找最短路径
              </button>
            </form>
          )}

          {activeTab === 'subgraph' && (
            <form onSubmit={handleExtractSubgraph} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  根节点ID <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={subgraphForm.root}
                  onChange={(e) => setSubgraphForm({ ...subgraphForm, root: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="如：root-node"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">提取深度</label>
                <input
                  type="number"
                  value={subgraphForm.depth}
                  onChange={(e) => setSubgraphForm({ ...subgraphForm, depth: parseInt(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  min={1}
                  max={10}
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="bg-purple-600 text-white px-6 py-2 rounded-lg hover:bg-purple-700 disabled:opacity-50 flex items-center gap-2"
              >
                {loading && <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>}
                提取子图
              </button>
            </form>
          )}

          {renderResult()}
        </div>
      </div>
    </div>
  );
}

export default GraphTraversal;
