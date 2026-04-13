import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import MySubmissions from '../components/Contributor/MySubmissions.vue'
import ResourceForm from '../components/Contributor/ResourceForm.vue'
import SubmissionDetail from '../components/Contributor/SubmissionDetail.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/contributor/my-submissions',
    name: 'MySubmissions',
    component: MySubmissions
  },
  {
    path: '/contributor/create',
    name: 'CreateResource',
    component: ResourceForm
  },
  {
    path: '/contributor/edit/:id',
    name: 'EditResource',
    component: ResourceForm
  },
  {
    path: '/contributor/detail/:id',
    name: 'SubmissionDetail',
    component: SubmissionDetail
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
