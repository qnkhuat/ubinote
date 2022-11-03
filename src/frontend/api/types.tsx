interface WithDate {
  created_at?: string;
  updated_at?: string;
}

export interface TPage extends WithDate {
  id: number;
  url: string;
  path: string;
  title: string;
  description: string | null;
  domain: string;
  status: string;

}

export interface TAnnotation extends WithDate {
  id: number;
  creator_id: number;
  page_id: number;
  color: string;
  coordinate: string;
}

export interface TUser extends WithDate {
  id: number;
  email: string;
  first_name: string;
  last_name: string;
  password?: string
}

export interface TSession {
  id: string;
  creator_id: number;
  created_at: string;
}
