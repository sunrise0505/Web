import { cp, mkdir, rm, access } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
const source = fileURLToPath(new URL('../frontend/dist/', import.meta.url))
const target = fileURLToPath(new URL('../backend/src/main/resources/public/', import.meta.url))
await access(new URL('../frontend/dist/index.html', import.meta.url))
await rm(target, { recursive: true, force: true })
await mkdir(target, { recursive: true })
await cp(source, target, { recursive: true })
console.log('前端构建产物已复制到 backend/src/main/resources/public')
