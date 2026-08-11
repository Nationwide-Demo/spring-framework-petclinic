import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// In production the SPA is served from S3/CloudFront and /api is routed to the ALB;
// locally the same paths are proxied to the api-gateway.
export default defineConfig(({ mode }) => ({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: loadEnv(mode, '.', 'VITE_').VITE_GATEWAY_URL || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
  },
}));
