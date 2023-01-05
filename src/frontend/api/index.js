import axios from "axios";
console.log("FORM API", process.env.NODE_ENV);
console.log("FORM API", process.env.UN_API_URL);

const api = axios.create({
  baseURL: process.env.UN_API_URL,
  // this should be in dev mode only
  // we do this because we want to use the hot-loading endpoint for dev
  // instead of visiting the actual host
  withCredentials: process.env.NODE_ENV=="development",
})

// ---------------------------- User ---------------------------- //

export const createUser = (body) => api.post("api/user", body);

// ---------------------------- Session ---------------------------- //

export const createSession = (body) => api.post("api/session", body);
export const deleteSession = () => api.delete("api/session/");

// ---------------------------- Page ---------------------------- //
// return the static url for a given path
export const getStaticPage = (path) => `${process.env.UN_API_URL}/static/${path}`

export const getPage = (id) => api.get(`/api/page/${id}`);

//
export const listPages = () => api.get("/api/page");

//

export const createPage = (body) => api.post("/api/page", body);

// ---------------------------- Annotation ---------------------------- //

export const createAnnotation = (body) => api.post(`/api/annotation`, body);
