<template>
  <div id="app">
    <header class="app-header">
      <div class="header-content">
        <h1>CPT202</h1>
        <nav class="nav-links">
          <router-link to="/">Home</router-link>
          <router-link to="/contributor/my-submissions">My Submissions</router-link>
          <router-link to="/contributor/create">Create Resource</router-link>
        </nav>
      </div>
    </header>
    
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
  background-color: #fafafa;
  color: #333;
  line-height: 1.5;
}

#app {
  min-height: 100vh;
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h1 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  letter-spacing: -0.5px;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-links a {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #000;
}

.app-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}
</style>
