<script>
  import { onMount } from 'svelte';
  import * as api from "frontend/api.js";
  import {
    Form,
    Button,
    Content,
    DataTable,
    Modal,
    TextInput,
    InlineLoading,
    PasswordInput,
    Toolbar,
    ToolbarContent,
    ToolbarSearch,
  } from "carbon-components-svelte";

  let firstnameInput, lastnameInput, emailInput, passwordInput;
  let users = [];

  function normalizeUser(user) {
    user["common_name"] = user["first_name"] + " " + user["last_name"];
    return user;
  }

  function newUser() {
    const payload = {
      first_name: firstnameInput,
      last_name: lastnameInput,
      email: emailInput,
      password: passwordInput};
    api.createUser(payload).then((resp) => {users = [...users, normalizeUser(resp.data)]}).catch((err) => console.log("Error while creating user", err));
  };

  onMount(() => {
    api.listUsers().then(resp => users = resp.data.map(normalizeUser));
  });

</script>

<Content>

  <Form>
    <TextInput labelText= "First name" placehodler="Your first name" bind:value={firstnameInput}/>
    <TextInput labelText= "Last name" placehodler="Your last name" bind:value={lastnameInput}/>
    <TextInput labelText= "Email" placehodler="Your email please" bind:value={emailInput}/>
    <PasswordInput labelText="Password" placeholder="Enter password..." bind:value={passwordInput}/>
    <Button type="submit" on:click={(e) => {
            e.preventDefault();
            newUser();
            }}>Create</Button>
  </Form>


  <DataTable
    sortable
    headers={[
    {key: "id", value: "ID"},
    {key: "email", value: "Email"},
    {key: "common_name", value: "Name"},
    {key: "created_at", value: "Joined date"},
    //{key: "delete", value: "Delete", display: (_) => "Delete", sort: false},
    ]}
    rows={users}>
    <Toolbar>
      <ToolbarContent>
        <ToolbarSearch
          persistent
          shouldFilterRows={(row, value) => {
          const valueLowered = value.toLowerCase();
          return (row.email.toLowerCase().includes(valueLowered) || row.common_name.toLowerCase().includes(valueLowered));
          }}>
        </ToolbarSearch>
      </ToolbarContent>
    </Toolbar>
  </DataTable>

</Content>
