### Create session

http POST localhost:8000/api/session email=admin@ubinote.com password=Ubinote@123

### Call with session
export SESSION={the_session}
http localhost:8000/api/page Cookie:ubinote.SESSION=${SESSION}

### Create page
http POST localhost:8000/api/page Cookie:ubinote.SESSION=${SESSION} url=http://www.paulgraham.com/own.html
