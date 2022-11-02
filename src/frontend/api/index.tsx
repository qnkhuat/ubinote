import axios from "axios";
import { TPage, TAnnotation, TUser, TSession } from "./types";

const api = axios.create({
  baseURL: process.env.REACT_APP_BASE_URL,
  // this should be in dev mode only
  // we do this because we want to use the hot-loading endpoint for dev
  // instead of visiting the actual host
  withCredentials: true,
})


// ---------------------------- User ---------------------------- //
interface createUserBody {
  email: string;
  first_name: string;
  last_name: string;
  password: string

}
const createUser = (body: createUserBody) => api.post<TUser>("api/user", body);

// ---------------------------- Session ---------------------------- //
interface createSessionBody {
  email: string;
  password: string;
}
const createSession = (body: createSessionBody) => api.post<TSession>("api/session", body);

// ---------------------------- Page ---------------------------- //
// return the static url for a given path
const getStaticPage = (path: string) => `${process.env.REACT_APP_BASE_URL}/static/${path}`

const getPage = (id: number | string) => api.get<TPage>(`/api/page/${id}`);

//
const listPages = () => api.get<TPage[]>("/api/page");

//
interface createPageBody {
  url: string;
}
const createPage = (body: createPageBody) => api.post<TPage>("/api/page", body);

// ---------------------------- Annotation ---------------------------- //
// result of dom torange
type Coordinate = {
  start: number;
  end: number;
}

interface createAnnotationBody {
  page_id: number;
  coordinate: Coordinate;
  color?: string;
};

const createAnnotation = (body: createAnnotationBody) => api.post<TAnnotation>(`/api/annotation`, body);

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
