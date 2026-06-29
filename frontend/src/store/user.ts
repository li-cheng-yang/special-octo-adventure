import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const user = ref<User | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setRefreshToken(newToken: string) {
    refreshToken.value = newToken
    localStorage.setItem('refreshToken', newToken)
  }

  function setUser(newUser: User) {
    user.value = newUser
  }

  function logout() {
    token.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    // 强制刷新页面，确保所有状态重置
    window.location.href = '/login'
  }


  function handleTokenExpired() {
    token.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')

    if (window.location.pathname !== '/login') {
      window.location.href = '/login'
    }
  }

  function getToken(): string | null {
    return token.value
  }

  function getRefreshToken(): string | null {
    return refreshToken.value
  }

  return {
    token,
    refreshToken,
    user,
    isLoggedIn,
    setToken,
    setRefreshToken,
    setUser,
    logout,
    handleTokenExpired,
    getToken,
    getRefreshToken,
  }
})
