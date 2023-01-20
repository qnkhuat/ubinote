import * as api from "frontend/api.js";
import { writable } from 'svelte/store';

export const currentUser = writable(null);

export function getCurrentUser() {
	return api.getCurrentUser().then((resp) => {
		currentUser.set(resp.data);
	}).catch((_err) => {
		currentUser.set(null);
	});
}


export function logout() {
	return api.deleteSession().then(() => {
		currentUser.set(null);
	});
}
