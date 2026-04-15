<template>
  <div class="home">
    <div class="hero">
      <h1>CPT202 Heritage Repository</h1>
      <p class="subtitle">Cultural Heritage Resource Contribution Platform</p>
    </div>

    <div class="section">
      <h2>Features</h2>
      <div class="feature-grid">
        <div class="feature-card">
          <h3>My Submissions</h3>
          <p>View and manage your submitted resources with status filtering</p>
          <ul>
            <li>Submission list display</li>
            <li>Status filter (Draft/Pending/Rejected/Approved)</li>
            <li>Quick edit/delete</li>
            <li>View review feedback</li>
          </ul>
          <router-link to="/contributor/my-submissions" class="link">Try it</router-link>
        </div>

        <div class="feature-card">
          <h3>Create / Edit Resource</h3>
          <p>Fill in resource details, upload files, save draft or submit for review</p>
          <ul>
            <li>Form validation</li>
            <li>Category and tag selection</li>
            <li>File upload (images/documents/videos)</li>
            <li>Auto-save draft</li>
          </ul>
          <router-link to="/contributor/create" class="link">Try it</router-link>
        </div>

        <div class="feature-card">
          <h3>Submission Detail</h3>
          <p>View resource details and review status</p>
          <ul>
            <li>Complete information display</li>
            <li>Review status notification</li>
            <li>Review feedback view</li>
            <li>Quick resubmit</li>
          </ul>
        </div>
      </div>
    </div>

    <div class="section">
      <h2>Workflow</h2>
      <div class="workflow">
        <div class="workflow-step">
          <span class="step-num">1</span>
          <span class="step-label">Create</span>
        </div>
        <div class="workflow-arrow">-</div>
        <div class="workflow-step">
          <span class="step-num">2</span>
          <span class="step-label">Fill Details</span>
        </div>
        <div class="workflow-arrow">-</div>
        <div class="workflow-step">
          <span class="step-num">3</span>
          <span class="step-label">Upload Files</span>
        </div>
        <div class="workflow-arrow">-</div>
        <div class="workflow-step">
          <span class="step-num">4</span>
          <span class="step-label">Save Draft</span>
        </div>
        <div class="workflow-arrow">-</div>
        <div class="workflow-step active">
          <span class="step-num">5</span>
          <span class="step-label">Submit</span>
        </div>
      </div>
      
      <div class="status-flow">
        <span class="status draft">Draft</span>
        <span class="flow-arrow">-</span>
        <span class="status pending">Pending Review</span>
        <span class="flow-arrow">-</span>
        <span class="status approved">Approved</span>
        <span class="or">or</span>
        <span class="status rejected">Rejected</span>
      </div>
    </div>

    <div class="section">
      <h2>Tech Stack</h2>
      <div class="tech-grid">
        <div class="tech-item">
          <span class="tech-label">Frontend</span>
          <span class="tech-value">Vue 3 + Vite</span>
        </div>
        <div class="tech-item">
          <span class="tech-label">State</span>
          <span class="tech-value">Pinia</span>
        </div>
        <div class="tech-item">
          <span class="tech-label">Router</span>
          <span class="tech-value">Vue Router</span>
        </div>
        <div class="tech-item">
          <span class="tech-label">HTTP</span>
          <span class="tech-value">Axios</span>
        </div>
        <div class="tech-item">
          <span class="tech-label">API</span>
          <span class="tech-value">RESTful</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style scoped>
.home {
  max-width: 900px;
  margin: 0 auto;
}

.hero {
  text-align: center;
  padding: 48px 0 40px;
  border-bottom: 1px solid #eee;
  margin-bottom: 40px;
}

.hero h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 15px;
  color: #666;
}

.section {
  margin-bottom: 40px;
}

.section h2 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.feature-card {
  background: #fff;
  padding: 24px;
  border: 1px solid #eee;
}

.feature-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  line-height: 1.5;
}

.feature-card ul {
  list-style: none;
  margin-bottom: 16px;
}

.feature-card ul li {
  font-size: 13px;
  color: #666;
  padding: 4px 0;
  padding-left: 12px;
  position: relative;
}

.feature-card ul li::before {
  content: "-";
  position: absolute;
  left: 0;
  color: #ccc;
}

.link {
  display: inline-block;
  font-size: 13px;
  color: #0066cc;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.workflow {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 24px;
  background: #fff;
  border: 1px solid #eee;
  margin-bottom: 16px;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 16px;
}

.workflow-step.active {
  background: #1a1a1a;
  color: #fff;
}

.step-num {
  width: 24px;
  height: 24px;
  background: #f0f0f0;
  color: #666;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 6px;
}

.workflow-step.active .step-num {
  background: #fff;
  color: #1a1a1a;
}

.step-label {
  font-size: 12px;
  font-weight: 500;
}

.workflow-arrow {
  color: #ccc;
  font-size: 18px;
}

.status-flow {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #eee;
}

.status {
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 2px;
}

.status.draft { background: #f0f0f0; color: #666; }
.status.pending { background: #e6f0ff; color: #0066cc; }
.status.approved { background: #e6f7e6; color: #22863a; }
.status.rejected { background: #ffe6e6; color: #cc0000; }

.flow-arrow {
  color: #ccc;
}

.or {
  color: #999;
  font-size: 12px;
  font-style: italic;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.tech-item {
  background: #fff;
  padding: 16px;
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.tech-label {
  font-size: 11px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.tech-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
</style>
