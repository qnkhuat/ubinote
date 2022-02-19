import axios from "axios";
import { TArchive } from "./types";

const api = axios.create({
  baseURL: process.env.REACT_APP_BASE_URL,
})

// ---------------------------- Archive ---------------------------- //
// return the static url for a given path
const getStaticArchive = (path: string) => `${process.env.REACT_APP_BASE_URL}/static/${path}`

interface GetArchiveParams {};
const getArchive = (id: number | string, params: GetArchiveParams = {}) =>
  api.get<TArchive>(`/api/archive/${id}`, params);

//
interface ListArchivesParams {};
const listArchives = (params: ListArchivesParams = {}) =>
  api.get<TArchive[]>("/api/archive", params);

//
interface AddArchiveBody {
  url: string;
}
const addArchive = (body: AddArchiveBody) =>
  api.post<TArchive>("/api/archive", body);

export {
  api,
  addArchive,
  getArchive,
  listArchives,
  getStaticArchive
}
