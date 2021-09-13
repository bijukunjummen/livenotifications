## Run locally

### Start Redis

```shell
redis-server
```

### Start Cloud Firestore emulator

```shell
gcloud components install cloud-firestore-emulator
gcloud beta emulators firestore start --host-port=:8662
```

# Create chat rooms

```sh
curl -v -X POST  \
  -H "Content-type: application/json" \
  -H "Accept: application/json" \
   http://localhost:8080/chatrooms \
   -d '{
   "id": "some-room",
   "name": "some-room"
}'
```

# Get Chat Room

```sh
curl -v http://localhost:8080/chatrooms/some-room
```

# Stream Messages from room 

```sh
curl -v http://localhost:8080/messages/some-room
```

# Add a messsage to a room
```sh
curl -v -X POST \
  -H "Content-type: application/json" \
  -H "Accept: application/json" \
   http://localhost:8080/messages/some-room \
   -d '{
   "payload": "hello world"
}'
```
It should show up on the previous GET endpoint

