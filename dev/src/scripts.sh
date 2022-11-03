### Create user at setup

http POST localhost:8000/api/setup email=qn.khuat@gmail.com password=Qnkhuat.123 first_name=Ngoc last_name=Khuat

### Create session
http POST localhost:8000/api/session email=qn.khuat@gmail.com password=Qnkhuat.123

### Call with session
export SESSION='ubinote.SESSION={the_id}'
http localhost:8000/api/page Cookie:${SESSION}

### Create page
http POST localhost:8000/api/page Cookie:${SESSION} url=http://www.paulgraham.com/own.html
