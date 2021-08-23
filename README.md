## Run locally

### Start Redis

```shell
redis-server
```

### Start Cloud Datastore emulator

```shell
gcloud components install cloud-datastore-emulator
gcloud beta emulators datastore start
```

# Get Live Notifications

```sh
curl -v http://localhost:8080/notifications/some-channel
```

# Create a live notification
```sh
curl -v -X POST \
  -H "Content-type: application/json" \
  -H "Accept: application/json" \
   http://localhost:8080/notifications/some-channel \
   -d '{
   "id": "test",
   "payload": {"name": "test", "more": "attributes"}
}'
```
It should show up on the previous GET endpoint

