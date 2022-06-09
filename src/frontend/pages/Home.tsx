import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";

import { PageList } from "components/Page";

import { useState, useEffect } from "react";
import { TPage } from "api/types";
import { listPages } from "api";

const Home = () => {
  const [pages, setPages] = useState<TPage[]>([]);

  useEffect(() => {
    listPages().then((resp) => setPages(resp.data))
      .catch(e => console.error("Failed to get Pages: ", e));
  }, [])

  return (<TableContainer component={Paper}>
    <PageList pages={pages} />
    <Table sx={{ minWidth: 650 }} aria-label="simple table">
      <TableHead>
        <TableRow>
          <TableCell align="right">Title</TableCell>
          <TableCell align="right">Domain</TableCell>
          <TableCell align="right">URL</TableCell>
          <TableCell align="right">Path</TableCell>
          <TableCell align="right">Created At</TableCell>
          <TableCell align="right">Updated At</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {pages.map((page: TPage) => (
          <TableRow key={page.id} sx={{ "&:last-child td, &:last-child th": { border: 0 } }}>
            <TableCell component="th" scope="row"><a rel="noreferrer" href={`/page/${page.id}`}>{page.title}</a></TableCell>
            <TableCell component="th" scope="row"><a rel="noreferrer" href={`/page/${page.id}`}>{page.domain}</a></TableCell>
            <TableCell component="th" scope="row"><a rel="noreferrer" href={`/page/${page.id}`}>{page.url}</a></TableCell>
            <TableCell align="right"><a target="_blank" rel="noreferrer" href={`http://localhost:8000/static/${page.path}`}>{page.path}</a></TableCell>
            <TableCell align="right">{page.created_at}</TableCell>
            <TableCell align="right">{page.updated_at}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  </TableContainer>)
}

export default Home;
