set -x
export CACHING_HOME="/cachinghome"
USER_HOME="/root"
M2_HOME="$${USER_HOME}/.m2"
M2_CACHE="$${CACHING_HOME}/maven"
GRADLE_HOME="$${USER_HOME}/.gradle"
GRADLE_CACHE="$${CACHING_HOME}/gradle"

echo "Generating symbolic links for caches"
mkdir -p $${M2_CACHE}
mkdir -p $${GRADLE_CACHE}

[[ -d "$${M2_CACHE}" && ! -d "$${M2_HOME}" ]] && ln -s "$${M2_CACHE}" "$${M2_HOME}"
[[ -d "$${GRADLE_CACHE}" && ! -d "$${GRADLE_HOME}" ]] && ln -s "$${GRADLE_CACHE}" "$${GRADLE_HOME}"
./gradlew jib --image=gcr.io/$PROJECT_ID/livenotifications:$SHORT_SHA