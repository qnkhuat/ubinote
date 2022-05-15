import axios from "axios";
import { TPage, TAnnotation } from "./types";

const api = axios.create({
  baseURL: process.env.REACT_APP_BASE_URL,
})

// ---------------------------- Page ---------------------------- //
// return the static url for a given path
const getStaticPage = (path: string) => `${process.env.REACT_APP_BASE_URL}/static/${path}`

const getPage = (id: number | string) => api.get<TPage>(`/api/page/${id}`);

//
const listPages = () => api.get<TPage[]>("/api/page");

//
interface AddPageBody {
  url: string;
}
const addPage = (body: AddPageBody) => api.post<TPage>("/api/page", body);

// ---------------------------- Annotation ---------------------------- //
// result of dom torange
type Coordinate = {
  start: number;
  end: number;
}

interface AddAnnotationParams {
  coordinate: Coordinate;
  color?: string;
};

const addAnnotation = (page_id: number, params: AddAnnotationParams) => api.post<TAnnotation>(`/api/page/${page_id}/annotation`, params);

export {
  api,
  addPage,
  getPage,
  listPages,
  getStaticPage,
  addAnnotation
}
