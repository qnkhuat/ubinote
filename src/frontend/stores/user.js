import * as api from "frontend/api.js";
import { writable } from 'svelte/store';

export const currentUser = writable(null);

export function getCurrentUser() {
	return api.getCurrentUser().then((resp) => {
		currentUser.set(resp.data);
	}).catch((err) => {
		currentUser.set(null);
		throw err;
	});
}

export function logout() {
	return api.deleteSession().then(() => {
		currentUser.set(null);
	});
}
