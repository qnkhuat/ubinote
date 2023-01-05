<script>
	import { createSession } from "frontend/api/index.js";
	import { isLogedIn, logout } from "frontend/stores/auth.js";
	import {
		Form,
		TextInput,
		Button,
		PasswordInput,
	} from "carbon-components-svelte";

	let email = null;
	let password = null;
	let logedInValue;
	isLogedIn.subscribe((value) => {
		logedInValue = value;
	});
	console.log("islogedin", logedInValue);

	function submit() {
		createSession({email, password}).then(resp => {
			if (resp.status_code == 200)
				isLogedIn.set(true);
		});
	}

</script>

<Form>
	<TextInput labelText= "Email" placehodler="Your email please" bind:value={email}/>
	<PasswordInput labelText="Password" placeholder="Enter password..." bind:value={password}/>
	<Button type="submit" on:click={(e) => {
		e.preventDefault();
		submit();
	}}>Login</Button>

	<Button type="submit" on:click={(e) => {
		e.preventDefault();
		logout();
	}}>Logout</Button>

</Form>
	<Button on:click={(e) => {
		console.log(document.cookie);
	}}>
	check cookie
	</Button>
