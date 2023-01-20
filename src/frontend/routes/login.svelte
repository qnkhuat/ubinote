<script>
	import { navigateTo } from 'svelte-router-spa';
	import * as api from "frontend/api.js";
	import { getCurrentUser } from "frontend/stores/user.js";
	import {
		Form,
		TextInput,
		Button,
		PasswordInput,
	} from "carbon-components-svelte";

	let email = null;
	let password = null;

	function submit() {
		api.createSession({email, password}).then(resp => {
			getCurrentUser().then(() => {
				// is there a way to refer to the last page the user was on?
				navigateTo("/");
			});
		});
	}
</script>

<Form>
	<TextInput labelText= "Email" placeholder="Your email please" bind:value={email}/>
	<PasswordInput labelText="Password" placeholder="Enter password..." bind:value={password}/>
	<Button type="submit" on:click={(e) => {
				 e.preventDefault();
				 submit();
				 }}>Login</Button>
</Form>
