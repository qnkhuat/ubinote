<script>
	console.log("SUP", process.env.UN_API_URL);
	import { createSession } from "../api/index.js";
	import {
		Form,
		TextInput,
		Button,
		PasswordInput,
	} from "carbon-components-svelte";

	let email = null;
	let password = null;

	function submit() {
		console.log("Username: ", email);
		console.log("Password: ", password);
		createSession({email, password});
	}

</script>

<Form>
	<TextInput labelText= "Email" placehodler="Your email please" bind:value={email}/>
	<PasswordInput labelText="Password" placeholder="Enter password..." bind:value={password}/>
	<Button type="submit" on:click={(e) => {
		e.preventDefault();
		submit();
	}}>Submit</Button>
</Form>
