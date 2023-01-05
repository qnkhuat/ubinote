import { writable } from 'svelte/store';
import * as api from 'frontend/api/index.js';

export const isLogedIn = writable(false);


export function login(email, password)  {
	return api.createSession(email, password).then((response) => {
		if (response.status === 200) {
			isLogedIn.set(true);
			return true;
		} else {
			isLogedIn.set(false);
			return false;
		}
	})
}

export function logout() {
	return api.deleteSession();
	//isLogedIn.set(false);
}
