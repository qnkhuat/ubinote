### Create session

http POST localhost:8000/api/setup email=admin@ubinote.com password=Ubinote@123

### Call with session
export SESSION='ubinote.SESSION={the_id}'
http localhost:8000/api/page Cookie:${SESSION}

### Create page
http POST localhost:8000/api/page Cookie:${SESSION} url=http://www.paulgraham.com/own.html
