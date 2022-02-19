import React from "react";
import {
  BrowserRouter,
  Routes,
  Route,
} from "react-router-dom";
import Home from "./pages/Home";
import ArchiveID from "./pages/archive/[id]";

const Router = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home/>}></Route>
        <Route path="archive/:id" element={<ArchiveID/>}></Route>
      </Routes>
    </BrowserRouter>
  );
}

export default Router;
