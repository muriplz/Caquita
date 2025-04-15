import {defineConfig} from 'vite'
import {fileURLToPath, URL} from 'node:url'
import vue from '@vitejs/plugin-vue'
import {templateCompilerOptions} from '@tresjs/core'
import Components from 'unplugin-vue-components/vite'
import MotionResolver from 'motion-v/resolver'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
    plugins: [
        tailwindcss(),
        vue({
            ...templateCompilerOptions
        }),
        Components({
            dts: true,
            resolvers: [
                MotionResolver()
            ],
        }),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }
})