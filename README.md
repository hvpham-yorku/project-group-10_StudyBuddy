# Study Buddy

## How to build and run with **Docker**
```
git clone "https://github.com/hvpham-yorku/project-group-10_StudyBuddy"
cd project-group-10_StudyBuddy
docker build -t app .
docker run -p 8080:8080 app
```
## How to build and run **manually**
This requires two terminals; one for backend and one for frontend

First terminal
```
# Clone the repo
git clone "https://github.com/hvpham-yorku/project-group-10_StudyBuddy"

# Setup and run the backend (NOTE: running maven is a little different
# for now these commands work for Linux)
cd StudyBuddy
./mvnw spring-boot:run
```
This will startup the Spring Boot middleware at [localhost:8080](localhost:8080)
(feel free to explore the backend APIs through this)

Second terminal
```
# You need to be back at the root of the Github Project (i.e project-group-10_StudyBuddy)
# Install frontend dependencies
cd ./project-group-10_StudyBuddy/Frontend/
npm install
npm run server
```
Now, go to [localhost:5173](localhost:5173) and you should be able to see the frontend.
