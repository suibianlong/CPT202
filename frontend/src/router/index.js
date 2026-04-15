import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import MySubmissionsPage from '../pages/contributor/MySubmissionsPage.vue'
import MyDraftsPage from '../pages/contributor/MyDraftsPage.vue'
import ResourceFormPage from '../pages/contributor/ResourceFormPage.vue'
import SubmissionDetailPage from '../pages/contributor/SubmissionDetailPage.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage
  },
  {
  path: '/contributor/drafts',
  name: 'MyDrafts',
  component: () => import('../pages/contributor/MyDraftsPage.vue')
},
  {
    path: '/contributor/my-submissions',
    name: 'MySubmissions',
    component: MySubmissionsPage
  },
  {
    path: '/contributor/my-drafts',
    name: 'MyDrafts',
    component: MyDraftsPage
  },
  {
    path: '/contributor/create',
    name: 'CreateResource',
    component: ResourceFormPage
  },
  {
    path: '/contributor/edit/:id',
    name: 'EditResource',
    component: ResourceFormPage
  },
  {
    path: '/contributor/detail/:id',
    name: 'SubmissionDetail',
    component: SubmissionDetailPage
  },
  {
    path: '/contributor/resource/:id/versions',
    name: 'VersionHistory',
    component: () => import('../pages/contributor/VersionHistoryPage.vue')
  },
  {
    path: '/contributor/resource/:id/compare',
    name: 'VersionCompare',
    component: () => import('../pages/contributor/VersionComparePage.vue')
  },
  {
    path: '/contributor/resource/:id/rollback/:versionNo',
    name: 'RollbackVersion',
    component: () => import('../pages/contributor/RollbackVersionPage.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router