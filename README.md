# ubinote
Have you ever read a blog post so good that you just want to take it home, save somewhere you could access later even when the author loses the domain?
And also because it's so great you want to take note on it too!
Ubinote will solve this for you! You could gives ubinote a link, and it'll save it for you, as long you keep ubinote, you could still access that blog post you love.

Features:
- [ ] Archive static site ( with highlighter ) with proper format - Use single-file to download
- [ ] Archive pdf file    ( with highlighter )
- [ ] Listing page
- [ ] Download youtube video with youtube-dl
- [ ] Upload images,videos ?

Reserve url pattern:
https://ubinote.com => list archives
https://ubinote.com/a/id-title => view the archive
https://ubinote.com/archive/id-title => view the archive

TODO:
- Make adding comment works
- polishing the highlight classes
- Adds a header for every view page
- Login page + Authentication
- Listing page

### Install

First you need to install single-cli
`npm install -g "single-file-cli"`


### Development

Backend

```
clj -M:dev
```

Frontend

```
npm run dev
```
