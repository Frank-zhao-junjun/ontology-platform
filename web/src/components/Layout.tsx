import { Link, useLocation } from 'wouter';
import { useState, type ReactNode } from 'react';

function Layout({ children }: { children: ReactNode }) {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [location] = useLocation();

  const isActive = (path: string) => location === path || location.startsWith(path + '/');

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <aside
        className={`bg-slate-800 text-white transition-all duration-300 ${
          sidebarOpen ? 'w-64' : 'w-16'
        }`}
      >
        <div className="p-4 flex items-center justify-between">
          {sidebarOpen && (
            <h1 className="text-lg font-bold truncate">Ontology Platform</h1>
          )}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-1 rounded hover:bg-slate-700 transition-colors"
            title={sidebarOpen ? '收起侧边栏' : '展开侧边栏'}
          >
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              {sidebarOpen ? (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M11 19l-7-7 7-7m8 14l-7-7 7-7"
                />
              ) : (
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 5l7 7-7 7M5 5l7 7-7 7"
                />
              )}
            </svg>
          </button>
        </div>

        <nav className="mt-4 space-y-1">
          <Link
            href="/ontologies"
            className={`flex items-center px-4 py-3 hover:bg-slate-700 transition-colors ${
              isActive('/ontologies')
                ? 'bg-slate-700 border-l-4 border-blue-400'
                : 'border-l-4 border-transparent'
            }`}
          >
            <svg className="w-5 h-5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
            </svg>
            {sidebarOpen && <span className="ml-3">本体管理</span>}
          </Link>
        </nav>

        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-slate-700">
          {sidebarOpen && (
            <div className="text-xs text-slate-400">
              <p>Ontology Platform</p>
              <p className="mt-1">v1.0.0</p>
            </div>
          )}
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto">
        <div className="p-6">
          {children}
        </div>
      </main>
    </div>
  );
}

export default Layout;
