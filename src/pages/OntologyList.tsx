import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import type { Ontology, CreateOntologyRequest } from '../types';
import {
  listOntologies,
  createOntology,
  deleteOntology,
  publishOntology,
  archiveOntology,
} from '../api/client';

const statusLabels: Record<string, { text: string; class: string }> = {
  DRAFT: { text: '草稿', class: 'bg-yellow-100 text-yellow-800' },
  PUBLISHED: { text: '已发布', class: 'bg-green-100 text-green-800' },
  ARCHIVED: { text: '已归档', class: 'bg-gray-100 text-gray-800' },
};

function OntologyList() {
  const navigate = useNavigate();
  const [ontologies, setOntologies] = useState<Ontology[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState<CreateOntologyRequest>({
    name: '',
    displayName: '',
    description: '',
  });
  const [createError, setCreateError] = useState('');

  const loadOntologies = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await listOntologies();
      setOntologies(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadOntologies();
  }, [loadOntologies]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError('');
    try {
      await createOntology(createForm);
      setShowCreateModal(false);
      setCreateForm({ name: '', displayName: '', description: '' });
      loadOntologies();
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : '创建失败');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('确定要删除这个本体吗？此操作不可撤销。')) return;
    try {
      await deleteOntology(id);
      loadOntologies();
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除失败');
    }
  };

  const handlePublish = async (id: string) => {
    try {
      await publishOntology(id);
      loadOntologies();
    } catch (err) {
      setError(err instanceof Error ? err.message : '发布失败');
    }
  };

  const handleArchive = async (id: string) => {
    try {
      await archiveOntology(id);
      loadOntologies();
    } catch (err) {
      setError(err instanceof Error ? err.message : '归档失败');
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">本体管理</h2>
          <p className="text-gray-500 mt-1">管理所有本体定义，包括创建、发布、归档等操作</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          创建本体
        </button>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          {error}
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      ) : ontologies.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg border border-gray-200">
          <svg className="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          <p className="text-gray-500">暂无本体数据</p>
          <button
            onClick={() => setShowCreateModal(true)}
            className="mt-4 text-blue-600 hover:text-blue-700"
          >
            创建第一个本体
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">名称</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">对象类型数</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">版本</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">创建时间</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {ontologies.map((ontology) => {
                const status = statusLabels[ontology.status] || { text: ontology.status, class: 'bg-gray-100 text-gray-800' };
                return (
                  <tr key={ontology.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <div>
                          <div className="text-sm font-medium text-gray-900">{ontology.displayName}</div>
                          <div className="text-sm text-gray-500">{ontology.name}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${status.class}`}>
                        {status.text}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">{ontology.objectTypeCount}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{ontology.version}</td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {new Date(ontology.createdAt).toLocaleDateString('zh-CN')}
                    </td>
                    <td className="px-6 py-4 text-right text-sm font-medium">
                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => navigate(`/ontologies/${ontology.id}`)}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          详情
                        </button>
                        {ontology.status === 'DRAFT' && (
                          <button
                            onClick={() => handlePublish(ontology.id)}
                            className="text-green-600 hover:text-green-900"
                          >
                            发布
                          </button>
                        )}
                        {ontology.status === 'PUBLISHED' && (
                          <button
                            onClick={() => handleArchive(ontology.id)}
                            className="text-orange-600 hover:text-orange-900"
                          >
                            归档
                          </button>
                        )}
                        <button
                          onClick={() => handleDelete(ontology.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          删除
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-4">创建本体</h3>
            {createError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded text-red-700 text-sm">
                {createError}
              </div>
            )}
            <form onSubmit={handleCreate}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  本体名称 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={createForm.name}
                  onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="以小写字母开头，如：product_model"
                  pattern="^[a-z][a-z0-9_]*$"
                  required
                />
                <p className="text-xs text-gray-500 mt-1">只能包含小写字母、数字和下划线</p>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  显示名称 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={createForm.displayName}
                  onChange={(e) => setCreateForm({ ...createForm, displayName: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="如：产品模型"
                  required
                />
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-1">描述</label>
                <textarea
                  value={createForm.description}
                  onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  rows={3}
                  placeholder="本体的描述信息"
                />
              </div>
              <div className="flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  取消
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  创建
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default OntologyList;
