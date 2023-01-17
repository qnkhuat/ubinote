import * as api from "frontend/api.js";
import { writable } from 'svelte/store';

export const sessionProperties = writable({});

export function getSessionProperties() {
	return api.sessionProperties().then((response) => {
		sessionProperties.set(response.data);
	});
}
