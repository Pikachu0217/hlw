#!/usr/bin/env bash

set -u
set -o pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
REPORT_DIR="${HLW_API_REPORT_DIR:-${ROOT_DIR}/resources/reports/api-test}"
BASE_URL="${HLW_API_BASE_URL:-http://127.0.0.1:9000}"
DIRECT_MODE="${HLW_API_DIRECT_MODE:-0}"
USERNAME="${HLW_API_USERNAME:-admin}"
PASSWORD="${HLW_API_PASSWORD:-admin123}"
TENANT_HEADER="${HLW_API_TENANT_ID:-100}"
TIMEOUT_SECONDS="${HLW_API_TIMEOUT_SECONDS:-10}"
RUN_AT="$(date '+%Y-%m-%d %H:%M:%S')"
REPORT_STAMP="$(date '+%Y%m%d-%H%M%S')"
MARKDOWN_REPORT="${REPORT_DIR}/api-test-report-${REPORT_STAMP}.md"
JSON_REPORT="${REPORT_DIR}/api-test-report-${REPORT_STAMP}.json"
LATEST_MARKDOWN_REPORT="${REPORT_DIR}/latest.md"
LATEST_JSON_REPORT="${REPORT_DIR}/latest.json"

total_count=0
pass_count=0
fail_count=0
skip_count=0
token=""
last_body=""
json_items=""
markdown_rows=""

# 按接口路径解析直连服务地址，用于网关端口被占用或不启动网关时测试微服务。
resolve_base_url() {
  local path="$1"

  if [ "${DIRECT_MODE}" != "1" ]; then
    printf '%s' "${BASE_URL}"
    return
  fi

  case "${path}" in
    /auth/*)
      printf '%s' "${HLW_AUTH_BASE_URL:-http://127.0.0.1:9100}"
      ;;
    /system/*)
      printf '%s' "${HLW_SYSTEM_BASE_URL:-http://127.0.0.1:9200}"
      ;;
    /patient/*)
      printf '%s' "${HLW_PATIENT_BASE_URL:-http://127.0.0.1:9300}"
      ;;
    /doctor/*)
      printf '%s' "${HLW_DOCTOR_BASE_URL:-http://127.0.0.1:9400}"
      ;;
    /consult/*|/ws/consult/*)
      printf '%s' "${HLW_CONSULT_BASE_URL:-http://127.0.0.1:9500}"
      ;;
    /appointment/*)
      printf '%s' "${HLW_APPOINTMENT_BASE_URL:-http://127.0.0.1:9600}"
      ;;
    /prescription/*)
      printf '%s' "${HLW_PRESCRIPTION_BASE_URL:-http://127.0.0.1:9700}"
      ;;
    /drug/*)
      printf '%s' "${HLW_DRUG_BASE_URL:-http://127.0.0.1:9800}"
      ;;
    /order/*)
      printf '%s' "${HLW_ORDER_BASE_URL:-http://127.0.0.1:9900}"
      ;;
    *)
      printf '%s' "${BASE_URL}"
      ;;
  esac
}

# 输出当前测试目标说明，直连模式下展示端口映射。
target_description() {
  if [ "${DIRECT_MODE}" != "1" ]; then
    printf '%s' "${BASE_URL}"
    return
  fi

  printf '微服务直连：auth=9100, system=9200, patient=9300, doctor=9400, consult=9500, appointment=9600, prescription=9700, drug=9800, order=9900'
}

# 获取当前毫秒时间戳，用于计算接口耗时。
now_ms() {
  if command -v python3 >/dev/null 2>&1; then
    python3 - <<'PY'
import time
print(int(time.time() * 1000))
PY
    return
  fi

  echo "$(( $(date +%s) * 1000 ))"
}

# 输出脚本用法，说明可配置的测试目标和报告位置。
print_usage() {
  cat <<EOF
互联网医院接口测试脚本

用法：
  ./resources/scripts/api-test.sh

可选环境变量：
  HLW_API_BASE_URL=http://127.0.0.1:9000     接口基础地址，默认走网关
  HLW_API_DIRECT_MODE=1                      启用微服务直连模式，按路径自动选择 9100-9900 端口
  HLW_API_USERNAME=admin                     登录账号
  HLW_API_PASSWORD=admin123                  登录密码
  HLW_API_TENANT_ID=100                      租户请求头
  HLW_API_TIMEOUT_SECONDS=10                 单接口超时时间
  HLW_API_REPORT_DIR=resources/reports/api-test 报告输出目录

报告：
  Markdown: ${MARKDOWN_REPORT}
  JSON:     ${JSON_REPORT}
EOF
}

# 将文本转成 JSON 字符串，便于报告保留原始响应摘要。
json_escape() {
  local value="${1:-}"
  if command -v python3 >/dev/null 2>&1; then
    VALUE="${value}" python3 - <<'PY'
import json
import os
print(json.dumps(os.environ.get("VALUE", ""), ensure_ascii=False))
PY
    return
  fi

  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  value="${value//$'\n'/\\n}"
  printf '"%s"\n' "${value}"
}

# 从 JSON 响应中读取指定顶层字段，缺少 python3 时降级为空值。
json_field() {
  local body="$1"
  local field="$2"

  if ! command -v python3 >/dev/null 2>&1; then
    printf ''
    return
  fi

  BODY="${body}" FIELD="${field}" python3 - <<'PY'
import json
import os

body = os.environ.get("BODY", "")
field = os.environ.get("FIELD", "")
try:
    data = json.loads(body)
except Exception:
    print("")
else:
    value = data.get(field, "")
    if value is None:
        print("")
    elif isinstance(value, (dict, list)):
        print(json.dumps(value, ensure_ascii=False))
    else:
        print(value)
PY
}

# 从登录响应中提取 satoken，供后续接口携带。
extract_token() {
  local body="$1"

  if ! command -v python3 >/dev/null 2>&1; then
    printf ''
    return
  fi

  BODY="${body}" python3 - <<'PY'
import json
import os

try:
    data = json.loads(os.environ.get("BODY", ""))
except Exception:
    print("")
else:
    payload = data.get("data") or {}
    print(payload.get("token", ""))
PY
}

# 压缩响应体摘要，避免报告过长。
summarize_body() {
  local body="${1:-}"
  body="$(printf '%s' "${body}" | tr '\n' ' ' | sed 's/|/\\|/g')"
  if [ "${#body}" -gt 220 ]; then
    printf '%s...' "${body:0:220}"
  else
    printf '%s' "${body}"
  fi
}

# 追加一个接口测试结果到 Markdown 和 JSON 报告缓存。
record_result() {
  local name="$1"
  local method="$2"
  local path="$3"
  local status="$4"
  local http_code="$5"
  local biz_code="$6"
  local duration_ms="$7"
  local request_payload="$8"
  local summary="$9"

  total_count=$((total_count + 1))
  case "${status}" in
    PASS)
      pass_count=$((pass_count + 1))
      ;;
    SKIP)
      skip_count=$((skip_count + 1))
      ;;
    *)
      fail_count=$((fail_count + 1))
      ;;
  esac

  markdown_rows="${markdown_rows}| ${name} | ${method} | ${path} | ${status} | ${http_code} | ${biz_code} | ${duration_ms} | ${request_payload} | ${summary} |
"

  local json_name json_method json_path json_status json_http_code json_biz_code json_duration json_request_payload json_summary
  json_name="$(json_escape "${name}")"
  json_method="$(json_escape "${method}")"
  json_path="$(json_escape "${path}")"
  json_status="$(json_escape "${status}")"
  json_http_code="$(json_escape "${http_code}")"
  json_biz_code="$(json_escape "${biz_code}")"
  json_duration="$(json_escape "${duration_ms}")"
  json_request_payload="$(json_escape "${request_payload}")"
  json_summary="$(json_escape "${summary}")"

  if [ -n "${json_items}" ]; then
    json_items="${json_items},
"
  fi
  json_items="${json_items}    {\"name\":${json_name},\"method\":${json_method},\"path\":${json_path},\"status\":${json_status},\"httpCode\":${json_http_code},\"bizCode\":${json_biz_code},\"durationMs\":${json_duration},\"request\":${json_request_payload},\"summary\":${json_summary}}"
}

# 判断接口响应是否满足 HTTP 2xx 且业务 code 为 200。
assert_success() {
  local http_code="$1"
  local biz_code="$2"

  [[ "${http_code}" =~ ^2[0-9][0-9]$ ]] && [ "${biz_code}" = "200" ]
}

# 执行一个接口用例，并根据响应结果记录报告。
run_case() {
  local name="$1"
  local method="$2"
  local path="$3"
  local body="${4:-}"
  local expected="${5:-success}"
  local full_url="${BASE_URL}${path}"
  local response_file
  local meta_file
  local started_at
  local ended_at
  local curl_status
  local http_code
  local body_text
  local biz_code
  local duration_ms
  local result_status
  local summary
  local request_payload="-"

  full_url="$(resolve_base_url "${path}")${path}"
  response_file="$(mktemp)"
  meta_file="$(mktemp)"
  started_at="$(now_ms)"

  local curl_args=(
    --silent
    --show-error
    --max-time "${TIMEOUT_SECONDS}"
    --request "${method}"
    --header "Content-Type: application/json"
    --header "X-Tenant-Id: ${TENANT_HEADER}"
    --output "${response_file}"
    --write-out "%{http_code}"
    "${full_url}"
  )

  if [ -n "${token}" ]; then
    curl_args=(--header "satoken: ${token}" "${curl_args[@]}")
  fi

  if [ -n "${body}" ]; then
    curl_args=(--data "${body}" "${curl_args[@]}")
    request_payload="$(summarize_body "${body}")"
  fi

  http_code="$(curl "${curl_args[@]}" 2>"${meta_file}")"
  curl_status=$?
  ended_at="$(now_ms)"
  duration_ms=$((ended_at - started_at))
  body_text="$(cat "${response_file}")"
  last_body="${body_text}"
  biz_code="$(json_field "${body_text}" "code")"

  if [ "${curl_status}" -ne 0 ]; then
    result_status="FAIL"
    summary="curl 失败：$(summarize_body "$(cat "${meta_file}")")"
  elif [ "${expected}" = "http-only" ] && [[ "${http_code}" =~ ^2[0-9][0-9]$ ]]; then
    result_status="PASS"
    summary="$(summarize_body "${body_text}")"
  elif assert_success "${http_code}" "${biz_code}"; then
    result_status="PASS"
    summary="$(summarize_body "${body_text}")"
  else
    result_status="FAIL"
    summary="$(summarize_body "${body_text}")"
  fi

  record_result "${name}" "${method}" "${path}" "${result_status}" "${http_code}" "${biz_code}" "${duration_ms}" "${request_payload}" "${summary}"
  rm -f "${response_file}" "${meta_file}"
}

# 执行登录用例，并保存返回 token。
run_login_case() {
  local name="认证登录"
  local method="POST"
  local path="/auth/login"
  local body="{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}"
  local response_file
  local meta_file
  local started_at
  local ended_at
  local curl_status
  local http_code
  local body_text
  local biz_code
  local duration_ms
  local result_status
  local summary
  local request_payload

  request_payload="$(summarize_body "${body}")"

  response_file="$(mktemp)"
  meta_file="$(mktemp)"
  started_at="$(now_ms)"
  http_code="$(curl \
    --silent \
    --show-error \
    --max-time "${TIMEOUT_SECONDS}" \
    --request "${method}" \
    --header "Content-Type: application/json" \
    --header "X-Tenant-Id: ${TENANT_HEADER}" \
    --data "${body}" \
    --output "${response_file}" \
    --write-out "%{http_code}" \
    "$(resolve_base_url "${path}")${path}" 2>"${meta_file}")"
  curl_status=$?
  ended_at="$(now_ms)"
  duration_ms=$((ended_at - started_at))
  body_text="$(cat "${response_file}")"
  last_body="${body_text}"
  biz_code="$(json_field "${body_text}" "code")"
  token="$(extract_token "${body_text}")"

  if [ "${curl_status}" -ne 0 ]; then
    result_status="FAIL"
    summary="curl 失败：$(summarize_body "$(cat "${meta_file}")")"
  elif assert_success "${http_code}" "${biz_code}" && [ -n "${token}" ]; then
    result_status="PASS"
    summary="$(summarize_body "${body_text}")"
  else
    result_status="FAIL"
    summary="$(summarize_body "${body_text}")"
  fi

  record_result "${name}" "${method}" "${path}" "${result_status}" "${http_code}" "${biz_code}" "${duration_ms}" "${request_payload}" "${summary}"
  rm -f "${response_file}" "${meta_file}"
}

# 记录暂不适合 curl 覆盖的接口，避免报告误以为遗漏。
record_skip_case() {
  local name="$1"
  local method="$2"
  local path="$3"
  local request_payload="$4"
  local reason="$5"

  record_result "${name}" "${method}" "${path}" "SKIP" "-" "-" "-" "${request_payload}" "${reason}"
}

# 从号源列表响应中提取第一个可用号源的排班编号。
extract_available_schedule_id() {
  local body="$1"

  if ! command -v python3 >/dev/null 2>&1; then
    printf ''
    return
  fi

  BODY="${body}" python3 - <<'PY'
import json
import os

try:
    payload = json.loads(os.environ.get("BODY", ""))
except Exception:
    print("")
else:
    for item in payload.get("data") or []:
        if item.get("status") == "AVAILABLE" and item.get("scheduleId") is not None:
            print(item["scheduleId"])
            break
    else:
        print("")
PY
}

# 写出 Markdown 和 JSON 两种报告。
write_reports() {
  mkdir -p "${REPORT_DIR}"

  cat >"${MARKDOWN_REPORT}" <<EOF
# 互联网医院接口测试报告

- 测试时间：${RUN_AT}
- 基础地址：${BASE_URL}
- 测试目标：$(target_description)
- 直连模式：${DIRECT_MODE}
- 登录账号：${USERNAME}
- 总数：${total_count}
- 通过：${pass_count}
- 失败：${fail_count}
- 跳过：${skip_count}

| 用例 | 方法 | 路径 | 结果 | HTTP | 业务 code | 耗时 ms | 请求参数 | 响应摘要 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
${markdown_rows}
EOF

  local json_base_url json_username json_run_at json_target
  json_base_url="$(json_escape "${BASE_URL}")"
  json_username="$(json_escape "${USERNAME}")"
  json_run_at="$(json_escape "${RUN_AT}")"
  json_target="$(json_escape "$(target_description)")"

  cat >"${JSON_REPORT}" <<EOF
{
  "runAt": ${json_run_at},
  "baseUrl": ${json_base_url},
  "directMode": "${DIRECT_MODE}",
  "target": ${json_target},
  "username": ${json_username},
  "summary": {
    "total": ${total_count},
    "passed": ${pass_count},
    "failed": ${fail_count},
    "skipped": ${skip_count}
  },
  "cases": [
${json_items}
  ]
}
EOF

  cp "${MARKDOWN_REPORT}" "${LATEST_MARKDOWN_REPORT}"
  cp "${JSON_REPORT}" "${LATEST_JSON_REPORT}"
}

# 输出报告摘要，方便命令行直接查看测试结论。
print_summary() {
  echo "接口测试完成：总数=${total_count}，通过=${pass_count}，失败=${fail_count}，跳过=${skip_count}"
  echo "Markdown 报告：${MARKDOWN_REPORT}"
  echo "JSON 报告：${JSON_REPORT}"
}

# 按前后端主要业务路径编排接口用例。
run_all_cases() {
  run_login_case

  # 认证模块接口。
  run_case "查询登录用户资料" "GET" "/auth/profile"
  run_case "退出登录" "POST" "/auth/logout"

  # 系统管理接口。
  run_case "查询租户列表" "GET" "/system/tenants"
  run_case "创建租户" "POST" "/system/tenants" "{}"
  run_case "查询后台用户列表" "GET" "/system/users"
  run_case "查询角色列表" "GET" "/system/roles"
  run_case "查询菜单列表" "GET" "/system/menus"
  run_case "查询字典列表" "GET" "/system/dicts"
  run_case "创建字典项" "POST" "/system/dicts" "{\"dictType\":\"api_test\",\"dictLabel\":\"接口测试\",\"dictValue\":\"API_TEST\",\"sort\":99,\"remark\":\"脚本自动创建\"}"
  run_case "查询参数配置列表" "GET" "/system/configs"
  run_case "更新参数配置" "PUT" "/system/configs/1" "{\"configValue\":\"30\",\"remark\":\"接口测试更新\"}"
  run_case "查询岗位列表" "GET" "/system/posts"
  run_case "创建岗位" "POST" "/system/posts" "{\"postName\":\"接口测试岗\",\"postCode\":\"API_TEST_POST\",\"sort\":99,\"remark\":\"脚本自动创建\"}"
  run_case "查询权限码列表" "GET" "/system/permissions"
  run_case "查询用户角色授权" "GET" "/system/user-roles"
  run_case "绑定用户角色" "POST" "/system/user-roles" "{\"userId\":1,\"roleId\":1}"
  run_case "查询角色菜单授权" "GET" "/system/role-menus"
  run_case "绑定角色菜单" "POST" "/system/role-menus" "{\"roleId\":1,\"menuId\":1}"

  # 患者端首页与档案接口。
  run_case "查询当前患者档案" "GET" "/patient/profile"
  run_case "更新当前患者档案" "PUT" "/patient/profile" "{\"name\":\"张小满\",\"phone\":\"13800000009\",\"gender\":\"女\"}"
  run_case "查询患者列表" "GET" "/patient/patients"
  run_case "查询健康档案列表" "GET" "/patient/health-records"
  run_case "创建健康档案" "POST" "/patient/health-records" "{\"title\":\"接口测试档案\",\"summary\":\"脚本自动创建\"}"

  # 医生与排班接口。
  run_case "查询科室列表" "GET" "/doctor/departments"
  run_case "创建科室" "POST" "/doctor/departments" "{\"name\":\"接口测试科室\",\"status\":\"启用\"}"
  run_case "查询医生列表" "GET" "/doctor/doctors"
  run_case "查询医生详情" "GET" "/doctor/doctors/1"
  run_case "创建医生" "POST" "/doctor/doctors" "{\"name\":\"接口测试医生\",\"title\":\"主治医师\",\"department\":\"全科\"}"
  run_case "更新医生状态" "PUT" "/doctor/doctors/1/status" "{\"status\":\"ONLINE\"}"
  run_case "绑定医生科室" "POST" "/doctor/doctors/1/departments" "{\"departmentId\":10,\"appointmentFee\":50}"
  run_case "查询排班列表" "GET" "/doctor/schedules"
  run_case "创建排班" "POST" "/doctor/schedules" "{\"doctorId\":1,\"slot\":\"2026-06-13 上午\"}"
  run_case "计算挂号费" "POST" "/doctor/appointment-fee/resolve" "{\"title\":\"主任医师\",\"doctorFee\":80,\"departmentFee\":20}"

  # 预约挂号接口。
  run_case "查询预约单列表" "GET" "/appointment/appointments"
  run_case "创建预约单" "POST" "/appointment/appointments" "{\"doctorName\":\"陈知衡\",\"timeSlot\":\"2026-06-13 上午\"}"
  run_case "支付预约单" "POST" "/appointment/appointments/1/pay"
  run_case "预约签到" "POST" "/appointment/appointments/1/check-in"
  run_case "抢便民门诊预约单" "POST" "/appointment/appointments/1/grab" "{\"doctorId\":20}"
  run_case "查询号源列表" "GET" "/appointment/number-sources"
  local available_schedule_id
  available_schedule_id="$(extract_available_schedule_id "${last_body}")"
  if [ -n "${available_schedule_id}" ]; then
    run_case "锁定号源" "POST" "/appointment/number-sources/${available_schedule_id}/lock"
  else
    record_skip_case "锁定号源" "POST" "/appointment/number-sources/{scheduleId}/lock" "-" "当前服务内存中没有 AVAILABLE 号源，跳过不可重复的状态变更用例"
  fi
  run_case "创建放号配置" "POST" "/appointment/release-configs" "{\"scheduleId\":1,\"releaseAt\":\"2026-06-13 08:00:00\"}"

  # 问诊接口。
  run_case "查询问诊单列表" "GET" "/consult/consults"
  run_case "创建图文问诊" "POST" "/consult/consults" "{\"type\":\"IMAGE_TEXT\",\"patientName\":\"接口测试患者\",\"doctorName\":\"接口测试医生\",\"chiefComplaint\":\"接口测试问诊\"}"
  run_case "接单问诊" "POST" "/consult/consults/1/accept" "{\"doctorId\":1}"
  run_case "延长问诊" "POST" "/consult/consults/1/extend"
  run_case "完成问诊" "POST" "/consult/consults/1/complete"
  run_case "查询问诊消息" "GET" "/consult/consults/1/messages"
  record_skip_case "问诊 WebSocket 通道" "WS" "/ws/consult/{consultId}" "{\"consultId\":\"占位示例\"}" "WebSocket 长连接不适合用 curl 在本脚本中断言，建议使用专用 ws 客户端补充验证"

  # 处方接口。
  run_case "查询处方列表" "GET" "/prescription/prescriptions"
  run_case "创建处方草稿" "POST" "/prescription/prescriptions" "{\"patientId\":1,\"doctorId\":1,\"drugIds\":[1]}"
  run_case "提交处方" "POST" "/prescription/prescriptions/1/submit"
  run_case "审核通过处方" "POST" "/prescription/prescriptions/1/approve" "{\"pharmacistId\":1,\"remark\":\"接口测试通过\"}"
  run_case "驳回处方" "POST" "/prescription/prescriptions/1/reject" "{\"remark\":\"接口测试驳回\"}"

  # 药品库存接口。
  run_case "查询药品列表" "GET" "/drug/drugs"
  run_case "创建药品资料" "POST" "/drug/drugs" "{\"drugName\":\"接口测试药品\",\"spec\":\"10mg*12片\",\"inventory\":100}"
  run_case "查询库存列表" "GET" "/drug/stocks"
  run_case "创建库存记录" "POST" "/drug/stocks" "{\"drugId\":1,\"warehouseName\":\"接口测试仓\",\"inventory\":20}"
  run_case "配送单发货" "POST" "/drug/deliveries/1/ship"

  # 订单接口。
  run_case "查询订单列表" "GET" "/order/orders"
  run_case "创建订单" "POST" "/order/orders" "{\"businessType\":\"APPOINTMENT\",\"patientName\":\"张小满\",\"amount\":25}"
  run_case "模拟支付订单" "POST" "/order/orders/1/pay" "{\"payMethod\":\"MOCK_PAY\"}"
}

if [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ]; then
  print_usage
  exit 0
fi

mkdir -p "${REPORT_DIR}"
run_all_cases
write_reports
print_summary

if [ "${fail_count}" -gt 0 ]; then
  exit 1
fi
