import Home from "./routes/index.svelte";
import Login from "./routes/login.svelte"

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
