import { useState, useEffect, useCallback } from 'react';
import { useParams, useLocation, Link } from 'wouter';
import type { Ontology, ObjectType, ValidationResult } from '../types';
import {
  getOntology,
  updateOntology,
  validateOntology,
  listObjectTypes,
} from '../api/client';

const statusLabels: Record<string, { text: string; class: string }> = {
  DRAFT: { text: '草稿', class: 'bg-yellow-100 text-yellow-800' },
  PUBLISHED: { text: '已发布', class: 'bg-green-100 text-green-800' },
  ARCHIVED: { text: '已归档', class: 'bg-gray-100 text-gray-800' },
};

function OntologyDetail() {
  const params = useParams();
  const id = params.id;
  const [, setLocation] = useLocation();
  const [ontology, setOntology] = useState<Ontology | null>(null);
  const [objectTypes, setObjectTypes] = useState<ObjectType[]>([]);
  const [loading, setLoading] = useState(false);
  const [, setError] = useState('');
  const [editMode, setEditMode] = useState(false);
  const [editForm, setEditForm] = useState({ displayName: '', description: '' });
  const [validationResult, setValidationResult] = useState<ValidationResult | null>(null);

  const loadData = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError('');
    try {
      const [ontologyData, objectTypesData] = await Promise.all([
        getOntology(id),
        listObjectTypes(id),
      ]);
      setOntology(ontologyData);
      setObjectTypes(objectTypesData);
      setEditForm({
        displayName: ontologyData.displayName,
        description: ontologyData.description,
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleUpdate = async () => {
    if (!id) return;
    try {
      const updated = await updateOntology(id, editForm);
      setOntology(updated);
      setEditMode(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : '更新失败');
    }
  };

  const handleValidate = async () => {
    if (!id) return;
    try {
      const result = await validateOntology(id);
      setValidationResult(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '验证失败');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!ontology) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">本体不存在或已被删除</p>
        <button
          onClick={() => setLocation('/ontologies')}
          className="mt-4 text-blue-600 hover:text-blue-700"
        >
          返回列表
        </button>
      </div>
    );
  }

  const status = statusLabels[ontology.status] || { text: ontology.status, class: 'bg-gray-100 text-gray-800' };

  return (
    <div>
      {/* Breadcrumb */}
      <div className="mb-4">
        <button
          onClick={() => setLocation('/ontologies')}
          className="text-blue-600 hover:text-blue-700 flex items-center gap-1"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          返回列表
        </button>
      </div>

      {/* Header */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <div className="flex justify-between items-start">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <h2 className="text-2xl font-bold text-gray-900">{ontology.displayName}</h2>
              <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${status.class}`}>
                {status.text}
              </span>
            </div>
            <p className="text-gray-500">{ontology.name}</p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setEditMode(!editMode)}
              className="px-4 py-2 text-blue-600 border border-blue-600 rounded-lg hover:bg-blue-50"
            >
              {editMode ? '取消' : '编辑'}
            </button>
            <button
              onClick={handleValidate}
              className="px-4 py-2 text-green-600 border border-green-600 rounded-lg hover:bg-green-50"
            >
              验证
            </button>
          </div>
        </div>

        {editMode ? (
          <div className="mt-4 space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">显示名称</label>
              <input
                type="text"
                value={editForm.displayName}
                onChange={(e) => setEditForm({ ...editForm, displayName: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">描述</label>
              <textarea
                value={editForm.description}
                onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                rows={3}
              />
            </div>
            <div className="flex gap-2">
              <button
                onClick={handleUpdate}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              >
                保存
              </button>
            </div>
          </div>
        ) : (
          <div className="mt-4">
            <p className="text-gray-700">{ontology.description || '暂无描述'}</p>
          </div>
        )}

        {/* Meta Info */}
        <div className="mt-6 grid grid-cols-4 gap-4 text-sm">
          <div>
            <span className="text-gray-500">版本</span>
            <p className="font-medium text-gray-900">{ontology.version}</p>
          </div>
          <div>
            <span className="text-gray-500">对象类型数</span>
            <p className="font-medium text-gray-900">{ontology.objectTypeCount}</p>
          </div>
          <div>
            <span className="text-gray-500">创建时间</span>
            <p className="font-medium text-gray-900">
              {new Date(ontology.createdAt).toLocaleDateString('zh-CN')}
            </p>
          </div>
          <div>
            <span className="text-gray-500">更新时间</span>
            <p className="font-medium text-gray-900">
              {new Date(ontology.updatedAt).toLocaleDateString('zh-CN')}
            </p>
          </div>
        </div>

        {/* Validation Result */}
        {validationResult && (
          <div className="mt-4 p-4 rounded-lg border">
            <h4 className="font-medium mb-2">验证结果</h4>
            <div className={`text-sm ${validationResult.valid ? 'text-green-600' : 'text-red-600'}`}>
              {validationResult.valid ? '验证通过' : '验证失败'}
            </div>
            {validationResult.errors.length > 0 && (
              <div className="mt-2">
                <p className="text-sm font-medium text-red-600">错误：</p>
                <ul className="list-disc list-inside text-sm text-red-600">
                  {validationResult.errors.map((err, i) => (
                    <li key={i}>{err}</li>
                  ))}
                </ul>
              </div>
            )}
            {validationResult.warnings.length > 0 && (
              <div className="mt-2">
                <p className="text-sm font-medium text-orange-600">警告：</p>
                <ul className="list-disc list-inside text-sm text-orange-600">
                  {validationResult.warnings.map((warn, i) => (
                    <li key={i}>{warn}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Object Types Section */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-bold text-gray-900">对象类型</h3>
          <div className="flex gap-2">
            <Link
              href={`/ontologies/${id}/object-types`}
              className="px-4 py-2 text-blue-600 border border-blue-600 rounded-lg hover:bg-blue-50"
            >
              管理对象类型
            </Link>
            <Link
              href={`/ontologies/${id}/graph`}
              className="px-4 py-2 text-purple-600 border border-purple-600 rounded-lg hover:bg-purple-50"
            >
              图遍历查询
            </Link>
          </div>
        </div>

        {objectTypes.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <p>暂无对象类型</p>
            <Link
              href={`/ontologies/${id}/object-types`}
              className="mt-2 text-blue-600 hover:text-blue-700 inline-block"
            >
              创建对象类型
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {objectTypes.map((ot) => (
              <div key={ot.id} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="flex justify-between items-start">
                  <div>
                    <h4 className="font-medium text-gray-900">{ot.displayName}</h4>
                    <p className="text-sm text-gray-500">{ot.name}</p>
                  </div>
                  <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                    {ot.instanceCount} 实例
                  </span>
                </div>
                <p className="text-sm text-gray-600 mt-2 line-clamp-2">{ot.description || '暂无描述'}</p>
                <div className="mt-3 text-xs text-gray-400">
                  主键: {ot.primaryKey || '无'}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default OntologyDetail;
