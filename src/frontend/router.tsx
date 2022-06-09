import {
  BrowserRouter,
  Routes,
  Route,
} from "react-router-dom";
import Home from "pages/Home";
import PageID from "pages/page/[id]";
import Login from "pages/Login";

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home/>}></Route>
        <Route path="login" element={<Login/>}></Route>
        <Route path="page/:id" element={<PageID/>}></Route>
      </Routes>
    </BrowserRouter>
  );
}

export default Router;
