cd /cachinghome
tar -czf /tmp/gradle-cache.tar.gz gradle/ &&
gsutil cp /tmp/gradle-cache.tar.gz gs://${_GCS_CACHE_BUCKET}/${_GCS_CACHE_FILE}.tar.gz