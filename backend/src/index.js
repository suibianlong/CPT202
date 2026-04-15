const express = require('express')
const cors = require('cors')
const path = require('path')
const fs = require('fs')

const app = express()

app.use(cors())
app.use(express.json())
app.use(express.urlencoded({ extended: true }))

// 健康检查 - 放在最前面
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() })
})

// 简单测试
app.get('/ping', (req, res) => {
  res.json({ message: 'pong' })
})

// 挂载资源路由 - 注意路径是 /api/resources
const resourceRoutes = require('./routes/resourceRoutes')
app.use('/api/resources', resourceRoutes)

// 404 处理
app.use((req, res) => {
  console.log('404:', req.method, req.url)
  res.status(404).json({ error: `Route ${req.method} ${req.url} not found` })
})
// 在 app.use('/api/resources', resourceRoutes) 之前添加
app.get('/api/direct-test', (req, res) => {
  res.json({ message: 'Direct route works!' })
})

app.get('/api/resources-direct/my', (req, res) => {
  res.json({ message: 'Direct resources route works!', resources: [] })
})
const PORT = 3000
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`)
  console.log(`Test: http://localhost:${PORT}/api/health`)
  console.log(`Resources: http://localhost:${PORT}/api/resources/test`)
})