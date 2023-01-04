<script>
	import { createSession } from "frontend/api/index.js";
	import {
		Form,
		TextInput,
		Button,
		PasswordInput,
	} from "carbon-components-svelte";

	let email = null;
	let password = null;

	function submit() {
		createSession({email, password}).then(resp => {
			document.cookie.set
		});
		console.log("COOKIE after loggedin ", document.cookie);
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
	<Button on:click={(e) => {
		console.log(document.cookie);
	}}>
	check cookie
	</Button>
