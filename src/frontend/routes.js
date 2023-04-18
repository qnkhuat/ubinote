import Home from "frontend/routes/index.svelte";
import Login from "frontend/routes/login.svelte"
import Setup from "frontend/routes/setup.svelte";
import PageId from "frontend/routes/page/[id].svelte";
import PublicPageId from "frontend/routes/public/page/[id].svelte";
import NotFound from "frontend/routes/404.svelte";
import { currentUser } from "frontend/stores/user.js";
import { sessionProperties } from "frontend/stores/sessionProperties.js";

let currentUserValue;
currentUser.subscribe(value => {currentUserValue = value});
let sessionPropertiesValue;
sessionProperties.subscribe(value => {sessionPropertiesValue = value});

function logedIn() {
	return currentUserValue !== null;
}

function setup() {
	return sessionPropertiesValue.has_user_setup === true;
}

function requireAuth(route) {
	let redirect = sessionPropertiesValue.has_user_setup ? "/login" : "/setup";
	return {
		...route,
		...{onlyIf: { guard: logedIn, redirect: redirect }}}
}

function requireSetup(route) {
	return {
		...route,
		...{onlyIf: { guard: setup, redirect: "/setup" }}}
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
	{
		name: "/public",
		component: "",
		nestedRoutes: [
			{
				name: "page",
				component: "",
				nestedRoutes: [
					{
						name: ":uuid",
						component: PublicPageId
					}
				]
			}
		]
	}
]

const routes = [
	...privateRoutes.map((route) => requireAuth(route)),
	...publicRoutes.map((route) => requireSetup(route)),
	{
		name: "/setup",
		component: Setup,
		onlyIf: {
			guard: () => !setup(),
			redirect: "/login"
		}
	},
	{
		name: "404",
		path: "404",
		component: NotFound,
	}
]

export default routes;
