import { useState, useEffect, useCallback } from 'react';
import { useParams, useLocation } from 'wouter';
import type { ObjectType, Property, CreateObjectTypeRequest, CreatePropertyRequest } from '../types';
import {
  listObjectTypes,
  createObjectType,
  deleteObjectType,
  listProperties,
  createProperty,
  deleteProperty,
} from '../api/client';

function ObjectTypeList() {
  const params = useParams();
  const ontologyId = params.id;
  const [, setLocation] = useLocation();
  const [objectTypes, setObjectTypes] = useState<ObjectType[]>([]);
  const [selectedObjectType, setSelectedObjectType] = useState<ObjectType | null>(null);
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [showCreateOT, setShowCreateOT] = useState(false);
  const [createOTForm, setCreateOTForm] = useState<CreateObjectTypeRequest>({
    ontologyId: ontologyId || '',
    name: '',
    displayName: '',
    description: '',
    primaryKey: '',
    interfaceNames: [],
  });

  const [showCreateProp, setShowCreateProp] = useState(false);
  const [createPropForm, setCreatePropForm] = useState<CreatePropertyRequest>({
    objectTypeId: '',
    name: '',
    displayName: '',
    description: '',
    dataType: 'STRING',
    required: false,
  });

  const loadObjectTypes = useCallback(async () => {
    if (!ontologyId) return;
    setLoading(true);
    setError('');
    try {
      const data = await listObjectTypes(ontologyId);
      setObjectTypes(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载失败');
    } finally {
      setLoading(false);
    }
  }, [ontologyId]);

  useEffect(() => {
    loadObjectTypes();
  }, [loadObjectTypes]);

  const loadProperties = async (objectType: ObjectType) => {
    if (!ontologyId) return;
    setSelectedObjectType(objectType);
    try {
      const data = await listProperties(ontologyId, objectType.id);
      setProperties(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载属性失败');
    }
  };

  const handleCreateOT = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ontologyId) return;
    try {
      await createObjectType(ontologyId, { ...createOTForm, ontologyId });
      setShowCreateOT(false);
      setCreateOTForm({
        ontologyId: ontologyId,
        name: '',
        displayName: '',
        description: '',
        primaryKey: '',
        interfaceNames: [],
      });
      loadObjectTypes();
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建失败');
    }
  };

  const handleDeleteOT = async (otId: string) => {
    if (!ontologyId || !window.confirm('确定要删除这个对象类型吗？')) return;
    try {
      await deleteObjectType(ontologyId, otId);
      setSelectedObjectType(null);
      setProperties([]);
      loadObjectTypes();
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除失败');
    }
  };

  const handleCreateProp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ontologyId || !selectedObjectType) return;
    try {
      await createProperty(ontologyId, selectedObjectType.id, {
        ...createPropForm,
        objectTypeId: selectedObjectType.id,
      });
      setShowCreateProp(false);
      setCreatePropForm({
        objectTypeId: '',
        name: '',
        displayName: '',
        description: '',
        dataType: 'STRING',
        required: false,
      });
      loadProperties(selectedObjectType);
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建属性失败');
    }
  };

  const handleDeleteProp = async (propertyId: string) => {
    if (!ontologyId || !selectedObjectType || !window.confirm('确定要删除这个属性吗？')) return;
    try {
      await deleteProperty(ontologyId, selectedObjectType.id, propertyId);
      loadProperties(selectedObjectType);
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除属性失败');
    }
  };

  const dataTypes = ['STRING', 'INTEGER', 'LONG', 'DOUBLE', 'BOOLEAN', 'DATE', 'DATETIME', 'JSON', 'REFERENCE'];

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

      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">对象类型管理</h2>
          <p className="text-gray-500 mt-1">管理本体下的对象类型及其属性</p>
        </div>
        <button
          onClick={() => setShowCreateOT(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 flex items-center gap-2"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          创建对象类型
        </button>
      </div>

      {error && (
        <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Object Types List */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg border border-gray-200">
            <div className="p-4 border-b border-gray-200">
              <h3 className="font-medium text-gray-900">对象类型列表</h3>
            </div>
            {loading ? (
              <div className="p-8 text-center">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mx-auto"></div>
              </div>
            ) : objectTypes.length === 0 ? (
              <div className="p-8 text-center text-gray-500">
                <p>暂无对象类型</p>
              </div>
            ) : (
              <div className="divide-y divide-gray-200">
                {objectTypes.map((ot) => (
                  <div
                    key={ot.id}
                    onClick={() => loadProperties(ot)}
                    className={`p-4 cursor-pointer hover:bg-gray-50 transition-colors ${
                      selectedObjectType?.id === ot.id ? 'bg-blue-50 border-l-4 border-blue-500' : ''
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-medium text-gray-900">{ot.displayName}</p>
                        <p className="text-sm text-gray-500">{ot.name}</p>
                      </div>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteOT(ot.id);
                        }}
                        className="text-red-500 hover:text-red-700"
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Properties Panel */}
        <div className="lg:col-span-2">
          {selectedObjectType ? (
            <div className="bg-white rounded-lg border border-gray-200">
              <div className="p-4 border-b border-gray-200 flex justify-between items-center">
                <div>
                  <h3 className="font-medium text-gray-900">{selectedObjectType.displayName} - 属性</h3>
                  <p className="text-sm text-gray-500">主键: {selectedObjectType.primaryKey || '无'}</p>
                </div>
                <button
                  onClick={() => setShowCreateProp(true)}
                  className="text-blue-600 border border-blue-600 px-3 py-1 rounded-lg hover:bg-blue-50 text-sm"
                >
                  添加属性
                </button>
              </div>

              {properties.length === 0 ? (
                <div className="p-8 text-center text-gray-500">
                  <p>暂无属性</p>
                </div>
              ) : (
                <table className="w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">名称</th>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
                      <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">必填</th>
                      <th className="px-4 py-2 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {properties.map((prop) => (
                      <tr key={prop.id}>
                        <td className="px-4 py-3">
                          <div className="text-sm font-medium text-gray-900">{prop.displayName}</div>
                          <div className="text-xs text-gray-500">{prop.name}</div>
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-900">{prop.dataType}</td>
                        <td className="px-4 py-3">
                          {prop.required ? (
                            <span className="text-green-600 text-sm">是</span>
                          ) : (
                            <span className="text-gray-400 text-sm">否</span>
                          )}
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button
                            onClick={() => handleDeleteProp(prop.id)}
                            className="text-red-600 hover:text-red-900 text-sm"
                          >
                            删除
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          ) : (
            <div className="bg-gray-50 rounded-lg border border-gray-200 p-12 text-center">
              <svg className="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <p className="text-gray-500">选择一个对象类型查看属性</p>
            </div>
          )}
        </div>
      </div>

      {/* Create Object Type Modal */}
      {showCreateOT && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-4">创建对象类型</h3>
            <form onSubmit={handleCreateOT}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  名称 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={createOTForm.name}
                  onChange={(e) => setCreateOTForm({ ...createOTForm, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="如：product"
                  pattern="^[a-z][a-z0-9_]*$"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  显示名称 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={createOTForm.displayName}
                  onChange={(e) => setCreateOTForm({ ...createOTForm, displayName: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="如：产品"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">描述</label>
                <textarea
                  value={createOTForm.description}
                  onChange={(e) => setCreateOTForm({ ...createOTForm, description: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  rows={2}
                />
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-1">主键</label>
                <input
                  type="text"
                  value={createOTForm.primaryKey}
                  onChange={(e) => setCreateOTForm({ ...createOTForm, primaryKey: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  placeholder="如：id"
                />
              </div>
              <div className="flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateOT(false)}
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

      {/* Create Property Modal */}
      {showCreateProp && selectedObjectType && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-bold mb-4">添加属性</h3>
            <form onSubmit={handleCreateProp}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  名称 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={createPropForm.name}
                  onChange={(e) => setCreatePropForm({ ...createPropForm, name: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  显示名称 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={createPropForm.displayName}
                  onChange={(e) => setCreatePropForm({ ...createPropForm, displayName: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  数据类型 <span className="text-red-500">*</span>
                </label>
                <select
                  value={createPropForm.dataType}
                  onChange={(e) => setCreatePropForm({ ...createPropForm, dataType: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                >
                  {dataTypes.map((dt) => (
                    <option key={dt} value={dt}>{dt}</option>
                  ))}
                </select>
              </div>
              <div className="mb-4 flex items-center">
                <input
                  type="checkbox"
                  id="required"
                  checked={createPropForm.required}
                  onChange={(e) => setCreatePropForm({ ...createPropForm, required: e.target.checked })}
                  className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                <label htmlFor="required" className="ml-2 text-sm text-gray-700">必填</label>
              </div>
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-1">描述</label>
                <textarea
                  value={createPropForm.description}
                  onChange={(e) => setCreatePropForm({ ...createPropForm, description: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  rows={2}
                />
              </div>
              <div className="flex justify-end gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateProp(false)}
                  className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  取消
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                >
                  添加
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default ObjectTypeList;
