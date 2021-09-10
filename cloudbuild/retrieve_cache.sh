set -x
cd /cachinghome
(gsutil ls -b gs://${_GCS_CACHE_BUCKET} || gsutil mb -l gs://${_GCS_CACHE_BUCKET})

(
  gsutil cp gs://${_GCS_CACHE_BUCKET}/${_GCS_CACHE_FILE}.tar.gz /tmp/gradle-cache.tar.gz &&
  tar -xzf /tmp/gradle-cache.tar.gz
) || echo 'Cache not found'