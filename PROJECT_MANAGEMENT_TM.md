The so called "Project Management" of Ubinote

# Phase 2
- [ ] can add/edit/delete comments
- [ ] Finetune:
  - clean way to install single-file-cli, maybe provide an env to set chrome-bin
- [ ] switch to toucan 2

# Phase 3
- [ ] PDF

# To prioritize
- [ ] follow links
- [ ] full-text search
- [ ] public sharing

# Known bugs:
- [ ] CSS and font is not correctly recorded for https://philip.greenspun.com/materialism/money

Some note on design
- [ ] when users select text, automatically display a popup to whether highlight or add note
- [ ] Note display
  - It's best to display the note on the right of the whole text paragraph
  - but I guess since our app needs to be universal, then maybe only display note when user click on the highlight?
- [ ] starts with simple: add/delete no edit for now
- [ ] I think the hard stuff is how to have a component to wrap text that can trigger events? like the hyper-annotation from memex





Some thoughts on what todo :
- need to test if the new migration with password salt works backward
- Rework the wrap api exception middle so that it makes more sense, currently check-401 is not catched properly. and also let's normalize to always returns a map, {:message ...}
