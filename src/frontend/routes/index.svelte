<script>
	import { onMount } from 'svelte';
	import Cookies from "js-cookie";

	import "carbon-components-svelte/css/white.css";
	import { Button } from "carbon-components-svelte";

	import { listPages } from "../api/index.js";

	onMount(async () => {
		console.log(document);
		console.log("pages:", listPages().then(resp => console.log(resp)));
		console.log("COOKIES from svelte", Cookies.get("ubinote.SESSION"))
	});

</script>

<Button/>
<h1>Hello world!</h1>
<p>Visit the <a href="https://svelte.dev/tutorial">Svelte tutorial</a> to learn how to build Svelte apps.</p>
<p>
	This template is pre-configured with svlete-spa-router for routing.<br/>
	Visit the <a href="https://github.com/ItalyPaleAle/svelte-spa-router">documentation for the router</a> to learn more.
</p>
<p>
	<a href="/login">Login now</a>
</p>

<style>
	h1 {
		color: #ff3e00;
		text-transform: uppercase;
		font-size: 4em;
		font-weight: 100;
	}
</style>
