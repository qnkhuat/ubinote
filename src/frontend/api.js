import axios from "axios";

const api = axios.create({
  baseURL: process.env.UN_API_URL,
})

// ---------------------------- User ---------------------------- //

export function createUser (body) {
  return api.post("api/user", body)
};

export function getCurrentUser () {
  return api.get("api/user/current")
};

// ---------------------------- Session ---------------------------- //

export function createSession (body) {
  return api.post("api/session", body)
};

export function deleteSession () {
  return api.delete("api/session/")
};

// ---------------------------- Page ---------------------------- //
export function getPage (id) {
  return api.get(`/api/page/${id}`)
};

export function getPageContent (id) {
  return api.get(`/api/page/${id}/content`)
};

//
export function listPages () {
  return api.get("/api/page")
};

//

export function createPage (body) {
  return api.post("/api/page", body)
};

// ---------------------------- Annotation ---------------------------- //

export function createAnnotation (body) {
  return api.post(`/api/annotation`, body)
};

export function deleteAnnotation(id) {
  return api.delete(`/api/annotation/${id}`)
}
