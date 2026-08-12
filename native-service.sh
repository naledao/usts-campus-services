#!/usr/bin/env bash
set -euo pipefail

APP_NAME="usts-campus-services-native"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

BINARY="${BINARY:-$SCRIPT_DIR/target/usts-campus-services}"
RUN_DIR="${RUN_DIR:-$SCRIPT_DIR/run}"
LOG_DIR="${LOG_DIR:-$SCRIPT_DIR/logs}"
PID_FILE="${PID_FILE:-$RUN_DIR/$APP_NAME.pid}"
CONSOLE_LOG_FILE="${CONSOLE_LOG_FILE:-$LOG_DIR/$APP_NAME-console.log}"
APP_LOG_FILE="${APP_LOG_FILE:-$LOG_DIR/$APP_NAME.log}"
ENV_FILE="${ENV_FILE:-$RUN_DIR/$APP_NAME.env}"
REDISSON_FILE="${REDISSON_FILE:-$RUN_DIR/redisson-agents.yml}"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

SPRING_PROFILE="${SPRING_PROFILE:-prod}"
SERVER_HOST="${SERVER_HOST:-127.0.0.1}"
SERVER_PORT="${SERVER_PORT:-9880}"
START_TIMEOUT="${START_TIMEOUT:-30}"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-8088}"
MYSQL_DATABASE="${MYSQL_DATABASE:-usts_campus_services}"
MYSQL_USERNAME="${MYSQL_USERNAME:-kangnasi}"

RABBITMQ_HOST="${RABBITMQ_HOST:-127.0.0.1}"
RABBITMQ_PORT="${RABBITMQ_PORT:-8090}"
RABBITMQ_USERNAME="${RABBITMQ_USERNAME:-kangnasi}"
RABBITMQ_VHOST="${RABBITMQ_VHOST:-/}"

GRPC_HOST="${GRPC_HOST:-127.0.0.1}"
GRPC_PORT="${GRPC_PORT:-55051}"
GRPC_TIMEOUT_MS="${GRPC_TIMEOUT_MS:-5000}"
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://127.0.0.1:8084}"
MINIO_REGION="${MINIO_REGION:-us-east-1}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-$MYSQL_USERNAME}"

usage() {
  cat <<EOF
Usage: $0 {start|stop|restart}
       $0 {启动|关闭|重启}

Optional configuration file:
  $ENV_FILE

Important environment variables:
  BINARY, SERVER_PORT, MYSQL_PASSWORD, RABBITMQ_PASSWORD,
  REDISSON_FILE, GRPC_HOST, GRPC_PORT, GRPC_TIMEOUT_MS,
  MINIO_ENDPOINT, MINIO_REGION
EOF
}

read_pid() {
  [[ -f "$PID_FILE" ]] || return 1
  local pid
  pid="$(tr -d '[:space:]' < "$PID_FILE")"
  [[ "$pid" =~ ^[0-9]+$ ]] || return 1
  printf '%s\n' "$pid"
}

pid_matches_app() {
  local pid="$1"
  [[ -r "/proc/$pid/cmdline" ]] || return 1
  local executable
  executable="$(tr '\0' '\n' < "/proc/$pid/cmdline" | head -n 1)"
  [[ -n "$executable" ]] || return 1
  [[ "$(readlink -f "$executable")" == "$(readlink -f "$BINARY")" ]]
}

process_alive() {
  local pid="$1"
  kill -0 "$pid" 2>/dev/null || return 1
  [[ -r "/proc/$pid/stat" ]] || return 1
  [[ "$(awk '{print $3}' "/proc/$pid/stat")" != "Z" ]]
}

is_running() {
  local pid
  pid="$(read_pid)" || return 1
  process_alive "$pid" || return 1
  pid_matches_app "$pid"
}

redisson_password() {
  [[ -r "$REDISSON_FILE" ]] || return 1
  sed -nE 's/^[[:space:]]*password:[[:space:]]*"?([^"[:space:]]+)"?[[:space:]]*$/\1/p' \
    "$REDISSON_FILE" | head -n 1
}

load_passwords() {
  local shared_password="${LOCAL_SERVICE_PASSWORD:-}"
  if [[ -z "$shared_password" ]]; then
    shared_password="$(redisson_password || true)"
  fi

  MYSQL_PASSWORD="${MYSQL_PASSWORD:-$shared_password}"
  RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD:-$shared_password}"
  MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-$shared_password}"

  if [[ -z "$MYSQL_PASSWORD" || -z "$RABBITMQ_PASSWORD" || -z "$MINIO_SECRET_KEY" ]]; then
    echo "Missing service password." >&2
    echo "Set MYSQL_PASSWORD and RABBITMQ_PASSWORD in the environment or $ENV_FILE." >&2
    return 1
  fi
}

health_url() {
  printf 'http://%s:%s/usts-campus-services/actuator/health\n' "$SERVER_HOST" "$SERVER_PORT"
}

start() {
  mkdir -p "$RUN_DIR" "$LOG_DIR"

  if [[ ! -x "$BINARY" ]]; then
    echo "Native executable not found or not executable: $BINARY" >&2
    return 1
  fi
  if [[ ! -r "$REDISSON_FILE" ]]; then
    echo "Redisson configuration not found: $REDISSON_FILE" >&2
    return 1
  fi

  if is_running; then
    echo "$APP_NAME is already running, pid=$(read_pid)"
    return 0
  fi

  local old_pid
  if old_pid="$(read_pid 2>/dev/null)" && process_alive "$old_pid"; then
    echo "PID file points to another live process; refusing to overwrite it: pid=$old_pid" >&2
    return 1
  fi
  rm -f "$PID_FILE"

  load_passwords

  local datasource_url
  datasource_url="jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"

  local -a app_env=(
    "SPRING_PROFILES_ACTIVE=$SPRING_PROFILE"
    "SERVER_PORT=$SERVER_PORT"
    "SPRING_REDIS_REDISSON_FILE=file:$REDISSON_FILE"
    "SPRING_DATASOURCE_URL=$datasource_url"
    "SPRING_DATASOURCE_USERNAME=$MYSQL_USERNAME"
    "SPRING_DATASOURCE_PASSWORD=$MYSQL_PASSWORD"
    "SPRING_RABBITMQ_HOST=$RABBITMQ_HOST"
    "SPRING_RABBITMQ_PORT=$RABBITMQ_PORT"
    "SPRING_RABBITMQ_USERNAME=$RABBITMQ_USERNAME"
    "SPRING_RABBITMQ_PASSWORD=$RABBITMQ_PASSWORD"
    "SPRING_RABBITMQ_VIRTUAL_HOST=$RABBITMQ_VHOST"
    "APP_DIANFEI_HOST=$GRPC_HOST"
    "APP_DIANFEI_PORT=$GRPC_PORT"
    "APP_DIANFEI_TIMEOUT_MS=$GRPC_TIMEOUT_MS"
    "APP_UPDATE_ANDROID_ENDPOINT=$MINIO_ENDPOINT"
    "APP_UPDATE_ANDROID_REGION=$MINIO_REGION"
    "APP_UPDATE_ANDROID_ACCESS_KEY=$MINIO_ACCESS_KEY"
    "APP_UPDATE_ANDROID_SECRET_KEY=$MINIO_SECRET_KEY"
    "LOGGING_FILE_NAME=$APP_LOG_FILE"
  )

  cd "$SCRIPT_DIR"
  umask 027
  nohup setsid env "${app_env[@]}" "$BINARY" >> "$CONSOLE_LOG_FILE" 2>&1 < /dev/null &
  local pid="$!"
  printf '%s\n' "$pid" > "$PID_FILE"

  local second
  for ((second = 1; second <= START_TIMEOUT; second++)); do
    if ! process_alive "$pid"; then
      echo "$APP_NAME failed to start. Check: $CONSOLE_LOG_FILE" >&2
      rm -f "$PID_FILE"
      return 1
    fi
    if curl --noproxy '*' --fail --silent --max-time 2 "$(health_url)" >/dev/null 2>&1; then
      echo "$APP_NAME started, pid=$pid"
      echo "Health: $(health_url)"
      echo "Application log: $APP_LOG_FILE"
      echo "Console log: $CONSOLE_LOG_FILE"
      return 0
    fi
    sleep 1
  done

  echo "$APP_NAME did not become healthy within ${START_TIMEOUT}s." >&2
  echo "Check: $CONSOLE_LOG_FILE" >&2
  stop
  return 1
}

stop() {
  local pid
  if ! pid="$(read_pid)"; then
    echo "$APP_NAME is not running: PID file not found"
    return 0
  fi

  if ! process_alive "$pid"; then
    rm -f "$PID_FILE"
    echo "$APP_NAME is not running; removed stale PID file"
    return 0
  fi
  if ! pid_matches_app "$pid"; then
    echo "PID file points to another process; refusing to stop it: pid=$pid" >&2
    return 1
  fi

  echo "Stopping $APP_NAME, pid=$pid"
  kill "$pid"

  local second
  for ((second = 1; second <= 30; second++)); do
    if ! process_alive "$pid"; then
      rm -f "$PID_FILE"
      echo "$APP_NAME stopped"
      return 0
    fi
    sleep 1
  done

  echo "$APP_NAME did not stop within 30s; sending SIGKILL, pid=$pid" >&2
  kill -9 "$pid" 2>/dev/null || true
  rm -f "$PID_FILE"
  echo "$APP_NAME stopped"
}

restart() {
  stop
  start
}

case "${1:-}" in
  start|启动)
    start
    ;;
  stop|关闭|停止)
    stop
    ;;
  restart|重启)
    restart
    ;;
  *)
    usage
    exit 2
    ;;
esac
