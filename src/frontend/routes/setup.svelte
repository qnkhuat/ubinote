<script>
	import { navigateTo } from 'svelte-router-spa';
	import * as api from "frontend/api.js";
	import { getCurrentUser } from "frontend/stores/user.js";
	import { getSessionProperties } from "frontend/stores/sessionProperties.js";
	import {
		Content,
		Form,
		TextInput,
		Button,
		PasswordInput,
	} from "carbon-components-svelte";

	let email = null;
	let password = null;
	let first_name = null;
	let last_name = null;

	function submit() {
		api.setup({first_name, last_name, email, password}).then(() => {
			getSessionProperties().then(() => {
				getCurrentUser().finally(() => {
					navigateTo("/");
				})
			});
		});
	}
</script>

<Content>
	<div>
		<h3>Hey, welcome to Ubinote.</h3>
		<p>Let's create your user first!</p>
		<Form>
			<TextInput labelText= "First name" placehodler="Your first name" bind:value={first_name}/>
			<TextInput labelText= "Last name" placehodler="Your last name" bind:value={last_name}/>
			<TextInput labelText= "Email" placehodler="Your email please" bind:value={email}/>
			<PasswordInput labelText="Password" placeholder="Enter password..." bind:value={password}/>
			<Button type="submit" on:click={(e) => {
				e.preventDefault();
				submit();
			}}>Create</Button>
		</Form>
	</div>
</Content>
