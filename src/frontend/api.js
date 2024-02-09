import axios from "axios";

const api = axios.create({
  baseURL: "/",
})

// ---------------------------- User ---------------------------- //
//
export function listUsers() {
  return api.get("api/user");
};

export function createUser(body) {
  return api.post("api/user", body);
};

export function getCurrentUser() {
  return api.get("api/user/current");
};

// ---------------------------- Session ---------------------------- //

export function sessionProperties() {
  return api.get("api/session/properties");
};

export function createSession(body) {
  return api.post("api/session", body);
};

export function deleteSession() {
  return api.delete("api/session/");
};


// ---------------------------- Setup ---------------------------- //

export function setup(body) {
  return api.post("api/setup", body);
}

// ---------------------------- Page ---------------------------- //
export function getPage(id) {
  return api.get(`/api/page/${id}`);
};

export function deletePage(id) {
  return api.delete(`/api/page/${id}`);
};

export function getPageContent(id) {
  return api.get(`/api/page/${id}/content`);
};

export function listPages() {
  return api.get("/api/page");
};

export function listPagesHTML() {
  return api.get("/api/page/html");
};


export function createPage(body) {
  return api.post("/api/page", body);
};

export function createPublicPage(id) {
  return api.post(`/api/page/${id}/public`);
}

export function deletePublicPage(id) {
  return api.delete(`/api/page/${id}/public`);
}

// ---------------------------- Annotation ---------------------------- //

export function createAnnotation(body) {
  return api.post(`/api/annotation`, body);
};

export function updateAnnotation(id, body) {
  return api.put(`/api/annotation/${id}`, body);
};


export function deleteAnnotation(id) {
  return api.delete(`/api/annotation/${id}`);
}

// ---------------------------- Public ---------------------------- //

export function getPublicPage(uuid) {
  return api.get(`/api/public/page/${uuid}`);
}

export function getPublicPageContent(uuid) {
  return api.get(`/api/public/page/${uuid}/content`);
}
