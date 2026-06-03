import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { mockServer } from './server/mock';

export default defineConfig({
  plugins: [react(), mockServer()],
  server: {
    port: 5000,
    host: '0.0.0.0',
  },
});
