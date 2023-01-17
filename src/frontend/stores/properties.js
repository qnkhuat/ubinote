import * as api from "frontend/api.js";
import { writable } from 'svelte/store';

export const currentUser = writable(null);

export function getCurrentUser() {
	return api.getCurrentUser().then((resp) => {
		if (resp.status == 200){
			currentUser.set(resp.data);
		} else {
			currentUser.set(null);
		}
	}).catch((_err) => {
		currentUser.set(null);
	});
}
