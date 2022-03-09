import axios from "axios";
import { TPage } from "./types";

const api = axios.create({
  baseURL: process.env.REACT_APP_BASE_URL,
})

// ---------------------------- Page ---------------------------- //
// return the static url for a given path
const getStaticPage = (path: string) => `${process.env.REACT_APP_BASE_URL}/static/${path}`

interface GetPageParams {};
const getPage = (id: number | string, params: GetPageParams = {}) => api.get<TPage>(`/api/page/${id}`, params);

//
interface ListPagesParams {};
const listPages = (params: ListPagesParams = {}) => api.get<TPage[]>("/api/page", params);

//
interface AddPageBody {
  url: string;
}
const addPage = (body: AddPageBody) => api.post<TPage>("/api/page", body);

export {
  api,
  addPage,
  getPage,
  listPages,
  getStaticPage
}
