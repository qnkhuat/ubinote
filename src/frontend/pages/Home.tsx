import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

import { useState, useEffect } from "react";
import { TArchive } from "../api/types";
import { listArchives } from "../api";

const Home = () => {
  const [archives, setArchives] = useState<TArchive[]>([]);

  useEffect(() => {
    listArchives().then((resp) => setArchives(resp.data))
      .catch(e => console.error("Failed to get Archives: ", e));
  }, [])

  return (<TableContainer component={Paper}>
    <Table sx={{ minWidth: 650 }} aria-label="simple table">
      <TableHead>
        <TableRow>
          <TableCell>Url</TableCell>
          <TableCell align="right">Path</TableCell>
          <TableCell align="right">Created At</TableCell>
          <TableCell align="right">Updated At</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {archives.map((archive: TArchive) => (
          <TableRow key={archive.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
            <TableCell component="th" scope="row"><a target="_blank" rel="noreferrer" href={archive.url}>{archive.url}</a></TableCell>
            <TableCell align="right"><a target="_blank" rel="noreferrer" href={`http://localhost:8000/static/${archive.path}`}>{archive.path}</a></TableCell>
            <TableCell align="right">{archive.created_at}</TableCell>
            <TableCell align="right">{archive.updated_at}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  </TableContainer>)
}

export default Home;
