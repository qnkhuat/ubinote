# Create a tag

git tag -a 0.0.4 -m "Display pages using iframe"
git push origin 0.0.4

# Build the project

clj -T:build uberjar

# Go to github and draft a release

https://github.com/qnkhuat/ubinote/releases/new
Include the jar with the release

Done!
