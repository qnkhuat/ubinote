import Home from "./routes/index.svelte";
import Login from "./routes/login.svelte"

const SESSION_KEY = "ubinote.SESSION";

function isLogedin() {
	// if a session exists, it means users is logedin
	// if not, redirect to login page
	//return typeof Cookies.get(SESSION_KEY) !== "undefined";
}
isLogedin()

const routes = [
	{
		name: "/",
		component: Home,
	},
	{
		name: "/login",
		component: Login,
	}
]

export { routes }
