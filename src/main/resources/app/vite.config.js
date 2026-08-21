import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  base: "",
  root: "src",
  server: {
    port: 3001,
    strictPort: true
  },
  build: {
    cssCodeSplit: true,
    manifest: true,
    minify: false,
    rollupOptions: {
      input: {
        app: "/app.main.js"
      },
      output: {
        dir: "./dist",
        assetFileNames: "assets/[name][extname]",
        chunkFileNames: "js/[name].js",
        entryFileNames: "js/[name].js"
      }
    },
    outDir: "../dist"
  }
})
