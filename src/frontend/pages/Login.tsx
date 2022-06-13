import { useState, MouseEvent} from "react";
import { Link } from "react-router-dom";

import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";

import { createSession } from "api";

const Login = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = (_e: MouseEvent<HTMLButtonElement>) => {
    createSession({email, password})
      .then(_result => console.log("Login succcessfully"))
      .catch(err => console.error("Failed to login: ", err));
  }
  return (
    <div>
      <form>
        <TextField
          id="email"
          label="Email"
          value={email}
          onChange={e => setEmail(e.target.value)}/>
        <TextField
          id="password"
          label="Password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          type="password"/>
        <Button id="submit" onClick={handleLogin}>Login</Button>
      </form>
      <Link to="/">GO</Link>
    </div>
  )
}

export default Login;
