import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
const api = axios.create({ baseURL: '/api', timeout: 65000 })
let refreshPromises = { admin: null, counselor: null }
function roleInfo(config) { const admin = config.url?.includes('/admin/') || window.location.pathname.startsWith('/admin'); return { key: admin ? 'admin' : 'counselor', role: admin ? 'ADMIN' : 'COUNSELOR', login: admin ? '/admin/login' : '/login' } }
api.interceptors.request.use(config => { const info=roleInfo(config);const token=localStorage.getItem(info.key+'_accessToken');if(token)config.headers.Authorization='Bearer '+token;return config })
async function refreshAccess(info){if(refreshPromises[info.key])return refreshPromises[info.key];const refreshToken=localStorage.getItem(info.key+'_refreshToken');if(!refreshToken)throw new Error('没有刷新令牌');refreshPromises[info.key]=axios.post('/api/auth/refresh',{refreshToken,role:info.role}).then(({data})=>{localStorage.setItem(info.key+'_accessToken',data.accessToken);localStorage.setItem(info.key+'_refreshToken',data.refreshToken);return data.accessToken}).finally(()=>{refreshPromises[info.key]=null});return refreshPromises[info.key]}
api.interceptors.response.use(response=>response,async error=>{const config=error.config||{},status=error.response?.status,info=roleInfo(config);if((status===401||status===403)&&!config._retried&&!config.url?.includes('/auth/')){config._retried=true;try{const token=await refreshAccess(info);config.headers.Authorization='Bearer '+token;return api(config)}catch(_){[info.key+'_accessToken',info.key+'_refreshToken',info.key+'_role'].forEach(k=>localStorage.removeItem(k));ElMessage.error('登录已过期，请重新登录');router.push(info.login)}}else if(status!==401&&status!==403){ElMessage.error(error.response?.data?.message||'请求失败')}return Promise.reject(error)})
export default api
