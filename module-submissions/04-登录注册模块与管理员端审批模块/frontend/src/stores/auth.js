import { defineStore } from 'pinia'
export const useAuthStore = defineStore('auth', {
  state: () => ({ token: '', role: '', userId: '', userName: '' }),
  actions: {
    setAdmin(data){ this.token=data.accessToken; this.role=data.role; this.userId=data.userId; this.userName=data.name; localStorage.setItem('admin_accessToken',data.accessToken);localStorage.setItem('admin_refreshToken',data.refreshToken);localStorage.setItem('admin_role',data.role);localStorage.setItem('admin_userId',data.userId);localStorage.setItem('admin_userName',data.name) },
    setCounselor(data){ this.token=data.accessToken; this.role=data.role; this.userId=data.userId; this.userName=data.name; localStorage.setItem('counselor_accessToken',data.accessToken);localStorage.setItem('counselor_refreshToken',data.refreshToken);localStorage.setItem('counselor_mustChangePassword',data.mustChangePassword?'1':'0');localStorage.setItem('counselor_role',data.role);localStorage.setItem('counselor_userId',data.userId);localStorage.setItem('counselor_userName',data.name) },
    adminLogout(){ ['admin_accessToken','admin_refreshToken','admin_role','admin_userId','admin_userName'].forEach(function(k){localStorage.removeItem(k)}) },
    counselorLogout(){ ['counselor_accessToken','counselor_refreshToken','counselor_mustChangePassword','counselor_role','counselor_userId','counselor_userName'].forEach(function(k){localStorage.removeItem(k)}) }
  }
})
