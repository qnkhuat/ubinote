import Home from "frontend/routes/index.svelte";
import Login from "frontend/routes/login.svelte"
import PageId from "frontend/routes/page/[id].svelte";
import { currentUser } from "frontend/stores/user.js";

let currentUserValue;
currentUser.subscribe(value => {currentUserValue = value});

function logedIn() {
	return currentUserValue !== null;
}

function requireAuth(route) {
	return {
		...route,
		...{onlyIf: { guard: logedIn, redirect: '/login' }}}
}

const privateRoutes = [
	{
		name: "/",
		component: Home,
	},
	{
		name: "/page",
		component: "",
		nestedRoutes: [
			{
				name: ":id",
				component: PageId
			}
		]
	}
]

const publicRoutes = [
	{
		name: "/login",
		component: Login,
	},

]

const routes = [
	...privateRoutes.map((route) => requireAuth(route)),
	...publicRoutes
]

export default routes;
