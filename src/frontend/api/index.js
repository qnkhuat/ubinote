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

const createUser = (body) => api.post("api/user", body);

// ---------------------------- Session ---------------------------- //

const createSession = (body) => api.post("api/session", body);

// ---------------------------- Page ---------------------------- //
// return the static url for a given path
const getStaticPage = (path) => `${process.env.UN_API_URL}/static/${path}`

const getPage = (id) => api.get(`/api/page/${id}`);

//
const listPages = () => api.get("/api/page");

//

const createPage = (body) => api.post("/api/page", body);

// ---------------------------- Annotation ---------------------------- //

const createAnnotation = (body) => api.post(`/api/annotation`, body);

export {
  api,
  // page
  createPage,
  getPage,
  listPages,
  getStaticPage,

  // annotation
  createAnnotation,


  // session
  createSession,
}
