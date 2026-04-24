export const getToken = () =>
  typeof window !== 'undefined' ? localStorage.getItem('jwt_token') : null;

export const setToken = (token: string) =>
  localStorage.setItem('jwt_token', token);

export const removeToken = () =>
  localStorage.removeItem('jwt_token');

export const isLoggedIn = () => !!getToken();
