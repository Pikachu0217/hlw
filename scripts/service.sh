#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="${ROOT_DIR}/code/backend"
FRONTEND_DIR="${ROOT_DIR}/code/frontend"
RUNTIME_DIR="${ROOT_DIR}/.runtime"
PID_DIR="${RUNTIME_DIR}/pids"
LOG_DIR="${RUNTIME_DIR}/logs"

BACKEND_MODULES="${BACKEND_MODULES:-hospital-gateway hospital-auth hospital-system hospital-patient hospital-doctor hospital-consult hospital-appointment hospital-prescription hospital-drug hospital-order}"
MENU_BACKEND_MODULES="${MENU_BACKEND_MODULES:-hospital-appointment hospital-auth hospital-consult hospital-doctor hospital-drug hospital-gateway hospital-order hospital-patient hospital-prescription hospital-system}"
FRONTEND_APPS="${FRONTEND_APPS:-admin-web patient-h5}"
MENU_FRONTEND_APPS="${MENU_FRONTEND_APPS:-admin-web patient-h5}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"

# 输出脚本用法，帮助开发者快速确认启停命令。
print_usage() {
  cat <<EOF
互联网医院一键启停脚本

用法：
  ./scripts/service.sh            进入交互式菜单
  ./scripts/service.sh start      启动前端和后端
  ./scripts/service.sh stop       停止前端和后端
  ./scripts/service.sh restart    重启前端和后端
  ./scripts/service.sh status     查看进程状态
  ./scripts/service.sh logs       查看日志文件位置

可选环境变量：
  BACKEND_MODULES="hospital-gateway hospital-auth"  指定后端模块
  FRONTEND_APPS="admin-web patient-h5"              指定前端应用
  SPRING_PROFILES_ACTIVE=local                      指定后端 Spring Profile
  SKIP_BACKEND=1                                    跳过后端
  SKIP_FRONTEND=1                                   跳过前端
EOF
}

# 暂停交互式菜单，方便开发者阅读执行结果。
pause_menu() {
  printf "按回车键继续..."
  read -r _
}

# 安全清屏；在管道或缺少 TERM 的环境里不阻断脚本。
clear_screen() {
  if [ -t 1 ] && [ -n "${TERM:-}" ]; then
    clear || true
  fi
}

# 创建运行时目录，用于保存 pid 和日志。
ensure_runtime_dir() {
  mkdir -p "${PID_DIR}" "${LOG_DIR}"
}

# 判断指定 pid 文件中的进程是否仍在运行。
is_running() {
  local pid_file="$1"

  if [ ! -f "${pid_file}" ]; then
    return 1
  fi

  local pid
  pid="$(cat "${pid_file}")"
  if [ -z "${pid}" ]; then
    return 1
  fi

  kill -0 "${pid}" >/dev/null 2>&1
}

# 清理已经失效的 pid 文件，避免状态误判。
clean_stale_pid() {
  local pid_file="$1"

  if [ -f "${pid_file}" ] && ! is_running "${pid_file}"; then
    rm -f "${pid_file}"
  fi
}

# 启动一个后台进程，并记录 pid 与日志。
start_process() {
  local service_name="$1"
  local work_dir="$2"
  local command="$3"
  local pid_file="${PID_DIR}/${service_name}.pid"
  local log_file="${LOG_DIR}/${service_name}.log"

  clean_stale_pid "${pid_file}"
  if is_running "${pid_file}"; then
    echo "服务已运行：${service_name}，pid=$(cat "${pid_file}")"
    return 0
  fi

  echo "正在启动：${service_name}"
  (
    cd "${work_dir}"
    nohup bash -lc "exec ${command}" >>"${log_file}" 2>&1 &
    echo $! >"${pid_file}"
  )
  echo "启动完成：${service_name}，pid=$(cat "${pid_file}")，日志=${log_file}"
}

# 停止一个后台进程，先正常终止，超时后强制终止。
stop_process() {
  local service_name="$1"
  local pid_file="${PID_DIR}/${service_name}.pid"

  clean_stale_pid "${pid_file}"
  if [ ! -f "${pid_file}" ]; then
    echo "服务未运行：${service_name}"
    return 0
  fi

  local pid
  pid="$(cat "${pid_file}")"
  echo "正在停止：${service_name}，pid=${pid}"
  kill "${pid}" >/dev/null 2>&1 || true

  local wait_count=0
  while kill -0 "${pid}" >/dev/null 2>&1; do
    wait_count=$((wait_count + 1))
    if [ "${wait_count}" -ge 10 ]; then
      echo "正常停止超时，强制停止：${service_name}"
      kill -9 "${pid}" >/dev/null 2>&1 || true
      break
    fi
    sleep 1
  done

  rm -f "${pid_file}"
  echo "停止完成：${service_name}"
}

# 输出一个服务的当前运行状态。
print_process_status() {
  local service_name="$1"
  local pid_file="${PID_DIR}/${service_name}.pid"

  clean_stale_pid "${pid_file}"
  if is_running "${pid_file}"; then
    echo "运行中：${service_name}，pid=$(cat "${pid_file}")"
  else
    echo "未运行：${service_name}"
  fi
}

# 判断后端模块目录和启动类是否存在。
is_backend_module_runnable() {
  local module="$1"

  if [ ! -d "${BACKEND_DIR}/${module}" ]; then
    echo "跳过不存在的后端模块：${module}"
    return 1
  fi

  if ! find "${BACKEND_DIR}/${module}/src/main/java" -name '*Application.java' -print -quit | grep -q .; then
    echo "跳过缺少启动类的后端模块：${module}"
    return 1
  fi

  return 0
}

# 启动指定后端模块。
start_backend_module() {
  local module="$1"

  if is_backend_module_runnable "${module}"; then
    start_process "${module}" "${BACKEND_DIR}/${module}" "mvn spring-boot:run -Dspring-boot.run.profiles=${SPRING_PROFILES_ACTIVE}"
  fi
}

# 启动所有存在启动类的后端模块。
start_backend() {
  if [ "${SKIP_BACKEND:-0}" = "1" ]; then
    echo "已跳过后端启动"
    return 0
  fi

  for module in ${BACKEND_MODULES}; do
    start_backend_module "${module}"
  done
}

# 停止所有后端模块。
stop_backend() {
  if [ "${SKIP_BACKEND:-0}" = "1" ]; then
    echo "已跳过后端停止"
    return 0
  fi

  for module in ${BACKEND_MODULES}; do
    stop_process "${module}"
  done
}

# 输出所有后端模块状态。
status_backend() {
  if [ "${SKIP_BACKEND:-0}" = "1" ]; then
    echo "已跳过后端状态检查"
    return 0
  fi

  for module in ${BACKEND_MODULES}; do
    print_process_status "${module}"
  done
}

# 获取前端应用对应的 pnpm 启动命令。
frontend_command() {
  local app_name="$1"

  case "${app_name}" in
    admin-web)
      echo "pnpm dev:admin"
      ;;
    patient-h5)
      echo "pnpm dev:patient"
      ;;
    *)
      echo "pnpm --filter ${app_name} dev"
      ;;
  esac
}

# 启动所有前端应用。
start_frontend() {
  if [ "${SKIP_FRONTEND:-0}" = "1" ]; then
    echo "已跳过前端启动"
    return 0
  fi

  for app_name in ${FRONTEND_APPS}; do
    if [ ! -d "${FRONTEND_DIR}/${app_name}" ]; then
      echo "跳过不存在的前端应用：${app_name}"
      continue
    fi

    start_process "${app_name}" "${FRONTEND_DIR}" "$(frontend_command "${app_name}")"
  done
}

# 停止所有前端应用。
stop_frontend() {
  if [ "${SKIP_FRONTEND:-0}" = "1" ]; then
    echo "已跳过前端停止"
    return 0
  fi

  for app_name in ${FRONTEND_APPS}; do
    stop_process "${app_name}"
  done
}

# 输出所有前端应用状态。
status_frontend() {
  if [ "${SKIP_FRONTEND:-0}" = "1" ]; then
    echo "已跳过前端状态检查"
    return 0
  fi

  for app_name in ${FRONTEND_APPS}; do
    print_process_status "${app_name}"
  done
}

# 启动指定前端应用。
start_frontend_app() {
  local app_name="$1"

  if [ ! -d "${FRONTEND_DIR}/${app_name}" ]; then
    echo "跳过不存在的前端应用：${app_name}"
    return 0
  fi

  start_process "${app_name}" "${FRONTEND_DIR}" "$(frontend_command "${app_name}")"
}

# 停止指定前端应用。
stop_frontend_app() {
  local app_name="$1"

  stop_process "${app_name}"
}

# 启动前端和后端服务。
start_all() {
  ensure_runtime_dir
  start_backend
  start_frontend
}

# 停止前端和后端服务，前端先停，后端后停。
stop_all() {
  ensure_runtime_dir
  stop_frontend
  stop_backend
}

# 输出前端和后端服务状态。
status_all() {
  ensure_runtime_dir
  status_backend
  status_frontend
}

# 输出日志目录和常用查看命令。
print_logs() {
  ensure_runtime_dir
  echo "日志目录：${LOG_DIR}"
  echo "查看指定服务日志示例：tail -f ${LOG_DIR}/hospital-gateway.log"
}

# 输出指定服务的日志路径和最近日志内容。
print_service_log() {
  local service_name="$1"
  local log_file="${LOG_DIR}/${service_name}.log"

  ensure_runtime_dir
  echo "日志文件：${log_file}"
  if [ -f "${log_file}" ]; then
    echo "最近 80 行日志："
    tail -n 80 "${log_file}"
  else
    echo "日志文件不存在，请先启动服务或确认服务名称。"
  fi
}

# 输出所有后端服务的日志路径和最近日志内容。
print_backend_logs() {
  local module

  for module in ${MENU_BACKEND_MODULES}; do
    echo
    echo "========== ${module} =========="
    print_service_log "${module}"
  done
}

# 输出主菜单。
print_main_menu() {
  clear_screen
  cat <<EOF
互联网医院服务管理

1 前端
2 后端
3 退出
EOF
}

# 输出前端菜单。
print_frontend_menu() {
  clear_screen
  cat <<EOF
前端服务管理

1 启动服务
2 停止服务
3 返回上一级
4 退出
EOF
}

# 输出后端菜单。
print_backend_menu() {
  clear_screen
  cat <<EOF
后端服务管理

1 启动服务
2 停止服务
3 日志输出
4 返回上一级
5 退出
EOF
}

# 输出前端应用选择菜单。
print_frontend_app_menu() {
  local action_name="$1"
  local index=1

  clear_screen
  echo "请选择要${action_name}的前端服务"
  echo
  for app_name in ${MENU_FRONTEND_APPS}; do
    echo "${index} ${app_name}"
    index=$((index + 1))
  done
  echo "3 返回上一级"
  echo "4 退出"
}

# 输出后端服务选择菜单。
print_backend_service_menu() {
  local action_name="$1"
  local index=1

  clear_screen
  echo "请选择要${action_name}的后端服务"
  echo
  for module in ${MENU_BACKEND_MODULES}; do
    echo "${index} ${module} 服务"
    index=$((index + 1))
  done
  echo "11 ALL"
  echo "12 返回上一级"
  echo "13 退出"
}

# 按菜单编号查找前端应用名。
frontend_app_by_menu_choice() {
  local choice="$1"
  local index=1

  for app_name in ${MENU_FRONTEND_APPS}; do
    if [ "${choice}" = "${index}" ]; then
      echo "${app_name}"
      return 0
    fi
    index=$((index + 1))
  done

  return 1
}

# 按菜单编号查找后端服务名。
service_name_by_menu_choice() {
  local choice="$1"
  local index=1

  for module in ${MENU_BACKEND_MODULES}; do
    if [ "${choice}" = "${index}" ]; then
      echo "${module}"
      return 0
    fi
    index=$((index + 1))
  done

  return 1
}

# 执行前端应用启动动作。
handle_frontend_start_choice() {
  local choice="$1"
  local app_name

  case "${choice}" in
    3)
      return 1
      ;;
    4)
      echo "已退出"
      exit 0
      ;;
    *)
      app_name="$(frontend_app_by_menu_choice "${choice}")" || {
        echo "无效选项：${choice}"
        return 0
      }
      ensure_runtime_dir
      start_frontend_app "${app_name}"
      ;;
  esac
}

# 执行前端应用停止动作。
handle_frontend_stop_choice() {
  local choice="$1"
  local app_name

  case "${choice}" in
    3)
      return 1
      ;;
    4)
      echo "已退出"
      exit 0
      ;;
    *)
      app_name="$(frontend_app_by_menu_choice "${choice}")" || {
        echo "无效选项：${choice}"
        return 0
      }
      ensure_runtime_dir
      stop_frontend_app "${app_name}"
      ;;
  esac
}

# 执行单个后端服务或全部服务的启动动作。
handle_backend_start_choice() {
  local choice="$1"
  local module

  case "${choice}" in
    11)
      ensure_runtime_dir
      start_backend
      ;;
    12)
      return 1
      ;;
    13)
      echo "已退出"
      exit 0
      ;;
    *)
      module="$(service_name_by_menu_choice "${choice}")" || {
        echo "无效选项：${choice}"
        return 0
      }
      ensure_runtime_dir
      start_backend_module "${module}"
      ;;
  esac
}

# 执行单个后端服务或全部服务的停止动作。
handle_backend_stop_choice() {
  local choice="$1"
  local module

  case "${choice}" in
    11)
      ensure_runtime_dir
      stop_backend
      ;;
    12)
      return 1
      ;;
    13)
      echo "已退出"
      exit 0
      ;;
    *)
      module="$(service_name_by_menu_choice "${choice}")" || {
        echo "无效选项：${choice}"
        return 0
      }
      ensure_runtime_dir
      stop_process "${module}"
      ;;
  esac
}

# 执行单个后端服务或全部服务的日志输出动作。
handle_backend_log_choice() {
  local choice="$1"
  local module

  case "${choice}" in
    11)
      print_backend_logs
      ;;
    12)
      return 1
      ;;
    13)
      echo "已退出"
      exit 0
      ;;
    *)
      module="$(service_name_by_menu_choice "${choice}")" || {
        echo "无效选项：${choice}"
        return 0
      }
      print_service_log "${module}"
      ;;
  esac
}

# 进入前端应用选择菜单。
frontend_app_menu() {
  local action="$1"
  local action_name="$2"
  local choice

  while true; do
    print_frontend_app_menu "${action_name}"
    printf "请输入选项："
    read -r choice

    case "${action}" in
      start)
        handle_frontend_start_choice "${choice}" || return 0
        ;;
      stop)
        handle_frontend_stop_choice "${choice}" || return 0
        ;;
    esac

    pause_menu
  done
}

# 进入后端服务选择菜单。
backend_service_menu() {
  local action="$1"
  local action_name="$2"
  local choice

  while true; do
    print_backend_service_menu "${action_name}"
    printf "请输入选项："
    read -r choice

    case "${action}" in
      start)
        handle_backend_start_choice "${choice}" || return 0
        ;;
      stop)
        handle_backend_stop_choice "${choice}" || return 0
        ;;
      log)
        handle_backend_log_choice "${choice}" || return 0
        ;;
    esac

    pause_menu
  done
}

# 进入前端服务管理菜单。
frontend_menu() {
  local choice

  while true; do
    print_frontend_menu
    printf "请输入选项："
    read -r choice

    case "${choice}" in
      1)
        frontend_app_menu "start" "启动"
        ;;
      2)
        frontend_app_menu "stop" "停止"
        ;;
      3)
        return 0
        ;;
      4)
        echo "已退出"
        exit 0
        ;;
      *)
        echo "无效选项：${choice}"
        pause_menu
        ;;
    esac
  done
}

# 进入后端服务管理菜单。
backend_menu() {
  local choice

  while true; do
    print_backend_menu
    printf "请输入选项："
    read -r choice

    case "${choice}" in
      1)
        backend_service_menu "start" "启动"
        ;;
      2)
        backend_service_menu "stop" "停止"
        ;;
      3)
        backend_service_menu "log" "输出日志"
        ;;
      4)
        return 0
        ;;
      5)
        echo "已退出"
        exit 0
        ;;
      *)
        echo "无效选项：${choice}"
        pause_menu
        ;;
    esac
  done
}

# 进入一级交互式菜单。
main_menu() {
  local choice

  while true; do
    print_main_menu
    printf "请输入选项："
    read -r choice

    case "${choice}" in
      1)
        frontend_menu
        ;;
      2)
        backend_menu
        ;;
      3)
        echo "已退出"
        exit 0
        ;;
      *)
        echo "无效选项：${choice}"
        pause_menu
        ;;
    esac
  done
}

case "${1:-}" in
  start)
    start_all
    ;;
  stop)
    stop_all
    ;;
  restart)
    stop_all
    start_all
    ;;
  status)
    status_all
    ;;
  logs)
    print_logs
    ;;
  -h|--help|help)
    print_usage
    ;;
  "")
    main_menu
    ;;
  *)
    echo "未知命令：$1"
    print_usage
    exit 1
    ;;
esac
