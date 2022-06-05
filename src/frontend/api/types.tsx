export interface TPage {
  id: number;
  url: string;
  path: string;
  title: string;
  description: string | null;
  domain: string;
  status: string;
  created_at: string;
  updated_at: string;
}

export interface TAnnotation {
  creator_id: number;
  page_id: number;
  color: string;
  coordinate: string;
}
