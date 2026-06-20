#!/usr/bin/env bash

set -u
set -o pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
REPORT_DIR="${HLW_API_REPORT_DIR:-${ROOT_DIR}/resources/reports/api-test}"
BASE_URL="${HLW_API_BASE_URL:-http://127.0.0.1:19000}"
DIRECT_MODE="${HLW_API_DIRECT_MODE:-0}"
USERNAME="${HLW_API_USERNAME:-hlw_admin}"
PASSWORD="${HLW_API_PASSWORD:-123456}"
TENANT_HEADER="${HLW_API_TENANT_ID:-0}"
RUN_PLATFORM_CASES="${HLW_API_RUN_PLATFORM_CASES:-0}"
TOKEN_HEADER_NAME="${HLW_API_TOKEN_NAME:-Authorization}"
TOKEN_PREFIX="${HLW_API_TOKEN_PREFIX:-Bearer}"
TENANT_HEADER_NAME="${HLW_API_TENANT_HEADER_NAME:-X-Tenant-Id}"
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
      printf '%s' "${HLW_AUTH_BASE_URL:-http://127.0.0.1:19100}"
      ;;
    /gateway/*)
      printf '%s' "${HLW_GATEWAY_BASE_URL:-http://127.0.0.1:19000}"
      ;;
    /system/*)
      printf '%s' "${HLW_SYSTEM_BASE_URL:-http://127.0.0.1:19200}"
      ;;
    /patient/*)
      printf '%s' "${HLW_PATIENT_BASE_URL:-http://127.0.0.1:19300}"
      ;;
    /doctor/*)
      printf '%s' "${HLW_DOCTOR_BASE_URL:-http://127.0.0.1:19400}"
      ;;
    /consult/*|/ws/consult/*)
      printf '%s' "${HLW_CONSULT_BASE_URL:-http://127.0.0.1:19500}"
      ;;
    /appointment/*)
      printf '%s' "${HLW_APPOINTMENT_BASE_URL:-http://127.0.0.1:19600}"
      ;;
    /prescription/*)
      printf '%s' "${HLW_PRESCRIPTION_BASE_URL:-http://127.0.0.1:19700}"
      ;;
    /drug/*)
      printf '%s' "${HLW_DRUG_BASE_URL:-http://127.0.0.1:19800}"
      ;;
    /order/*)
      printf '%s' "${HLW_ORDER_BASE_URL:-http://127.0.0.1:19900}"
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

  printf '微服务直连：gateway=19000, auth=19100, system=19200, patient=19300, doctor=19400, consult=19500, appointment=19600, prescription=19700, drug=19800, order=19900'
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
  HLW_API_BASE_URL=http://127.0.0.1:19000     接口基础地址，默认走网关
  HLW_API_DIRECT_MODE=1                      启用微服务直连模式，按路径自动选择 19100-19900 端口
  HLW_API_USERNAME=hlw_admin                 登录账号
  HLW_API_PASSWORD=123456                    登录密码
  HLW_API_TENANT_ID=0                        租户请求头，默认平台租户
  HLW_API_RUN_PLATFORM_CASES=1               执行租户、租户套餐等平台级全局配置用例
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

# 从登录响应中提取登录令牌，供后续接口通过配置化请求头携带。
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

# 按公共认证配置拼接登录令牌请求头值。
build_token_header_value() {
  local raw_token="$1"

  if [ -n "${TOKEN_PREFIX}" ]; then
    printf '%s %s' "${TOKEN_PREFIX}" "${raw_token}"
  else
    printf '%s' "${raw_token}"
  fi
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
    --header "${TENANT_HEADER_NAME}: ${TENANT_HEADER}"
    --output "${response_file}"
    --write-out "%{http_code}"
    "${full_url}"
  )

  if [ -n "${token}" ]; then
    curl_args=(--header "${TOKEN_HEADER_NAME}: $(build_token_header_value "${token}")" "${curl_args[@]}")
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
  local body="{\"tenantId\":${TENANT_HEADER},\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}"
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
  local curl_args=(
    --silent \
    --show-error \
    --max-time "${TIMEOUT_SECONDS}" \
    --request "${method}" \
    --header "Content-Type: application/json" \
    --data "${body}" \
    --output "${response_file}" \
    --write-out "%{http_code}" \
    "$(resolve_base_url "${path}")${path}"
  )
  if [ "${TENANT_HEADER}" != "0" ]; then
    curl_args=(--header "${TENANT_HEADER_NAME}: ${TENANT_HEADER}" "${curl_args[@]}")
  fi
  http_code="$(curl "${curl_args[@]}" 2>"${meta_file}")"
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

# 从最近一次接口响应 data 中提取 id，便于新增后串联详情、更新和删除用例。
extract_data_id() {
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
    data = payload.get("data") or {}
    if isinstance(data, dict):
        print(data.get("id") or "")
    elif isinstance(data, list) and data and isinstance(data[0], dict):
        print(data[0].get("id") or "")
    else:
        print("")
PY
}

# 从最近一次接口响应 data 中提取指定字段，便于串联业务主键。
extract_data_value() {
  local body="$1"
  local field="$2"

  if ! command -v python3 >/dev/null 2>&1; then
    printf ''
    return
  fi

  BODY="${body}" FIELD="${field}" python3 - <<'PY'
import json
import os

try:
    payload = json.loads(os.environ.get("BODY", ""))
except Exception:
    print("")
else:
    data = payload.get("data") or {}
    field = os.environ.get("FIELD", "")
    if isinstance(data, dict):
        print(data.get(field) or "")
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
  run_case "查询登录前租户选项" "GET" "/system/tenant/options"
  run_login_case

  # 认证模块接口。
  run_case "查询登录用户资料" "GET" "/auth/detail"

  # 系统管理接口。
  run_case "查询登录用户信息" "GET" "/system/getInfo"
  run_case "查询登录用户路由" "GET" "/system/getRouters"
  if [ "${RUN_PLATFORM_CASES}" = "1" ]; then
    run_case "查询租户列表" "GET" "/system/tenant"
    run_case "查询租户套餐列表" "GET" "/system/tenant-package"
    run_case "创建租户套餐" "POST" "/system/tenant-package" "{\"packageName\":\"接口测试套餐\",\"menuIds\":[1],\"remark\":\"脚本自动创建\"}"
    local tenant_package_id
    tenant_package_id="$(extract_data_id "${last_body}")"
    if [ -n "${tenant_package_id}" ]; then
      run_case "查询租户套餐详情" "GET" "/system/tenant-package/${tenant_package_id}"
      run_case "绑定租户套餐菜单" "PUT" "/system/tenant-package/${tenant_package_id}" "{\"packageName\":\"接口测试套餐更新\",\"menuIds\":[1,3],\"remark\":\"脚本自动绑定菜单\"}"
      run_case "回查租户套餐菜单" "GET" "/system/tenant-package/${tenant_package_id}"
      run_case "删除租户套餐" "DELETE" "/system/tenant-package/${tenant_package_id}"
    else
      record_skip_case "租户套餐详情更新删除" "GET/PUT/DELETE" "/system/tenant-package/{id}" "-" "创建租户套餐未返回 data.id，跳过串联用例"
    fi
    run_case "创建租户" "POST" "/system/tenant" "{\"contactUserName\":\"接口管理员\",\"contactPhone\":\"13800008888\",\"companyName\":\"接口测试医院\",\"licenseNumber\":\"LIC-API-TEST\",\"address\":\"接口测试地址\",\"intro\":\"脚本自动创建\",\"domain\":\"api-test\",\"remark\":\"接口测试租户\",\"packageId\":1,\"expireTime\":\"2026-12-31 23:59:59\",\"accountCount\":50,\"status\":\"0\"}"
    local tenant_id
    tenant_id="$(extract_data_id "${last_body}")"
    if [ -n "${tenant_id}" ]; then
      run_case "查询租户详情" "GET" "/system/tenant/${tenant_id}"
      run_case "更新租户" "PUT" "/system/tenant/${tenant_id}" "{\"contactUserName\":\"接口管理员\",\"contactPhone\":\"13800008889\",\"companyName\":\"接口测试医院更新\",\"licenseNumber\":\"LIC-API-TEST\",\"address\":\"接口测试地址更新\",\"intro\":\"脚本自动更新\",\"domain\":\"api-test\",\"remark\":\"接口测试租户更新\",\"packageId\":1,\"expireTime\":\"2026-12-31 23:59:59\",\"accountCount\":60,\"status\":\"0\"}"
      run_case "删除租户" "DELETE" "/system/tenant/${tenant_id}"
    else
      record_skip_case "租户详情更新删除" "GET/PUT/DELETE" "/system/tenant/{id}" "-" "创建租户未返回 data.id，跳过串联用例"
    fi
  else
    record_skip_case "平台级租户管理" "GET/POST/PUT/DELETE" "/system/tenant" "-" "默认不执行全局租户管理写操作，设置 HLW_API_RUN_PLATFORM_CASES=1 后执行"
    record_skip_case "平台级租户套餐管理" "GET/POST/PUT/DELETE" "/system/tenant-package" "-" "默认不执行全局套餐管理写操作，设置 HLW_API_RUN_PLATFORM_CASES=1 后执行"
  fi
  run_case "查询后台用户列表" "GET" "/system/user"
  run_case "创建后台用户" "POST" "/system/user" "{\"userName\":\"api_user\",\"nickName\":\"接口测试用户\",\"deptId\":1,\"userType\":\"ADMIN\",\"phone\":\"13800006666\",\"email\":\"api_user@example.com\",\"sex\":\"0\",\"password\":\"123456\",\"status\":0,\"remark\":\"脚本自动创建\"}"
  local user_id
  user_id="$(extract_data_id "${last_body}")"
  local business_user_id
  business_user_id="$(extract_data_value "${last_body}" "userId")"
  if [ -n "${user_id}" ]; then
    run_case "查询后台用户详情" "GET" "/system/user/${user_id}"
    run_case "更新后台用户" "PUT" "/system/user/${user_id}" "{\"userName\":\"api_user\",\"nickName\":\"接口测试用户更新\",\"deptId\":1,\"userType\":\"ADMIN\",\"phone\":\"13800006667\",\"email\":\"api_user@example.com\",\"sex\":\"0\",\"password\":\"123456\",\"status\":0,\"remark\":\"脚本自动更新\"}"
  else
    record_skip_case "后台用户详情更新删除" "GET/PUT/DELETE" "/system/user/{id}" "-" "创建后台用户未返回 data.id，跳过串联用例"
  fi
  run_case "查询角色列表" "GET" "/system/role"
  run_case "创建角色" "POST" "/system/role" "{\"roleName\":\"接口测试角色\",\"roleCode\":\"API_TEST_ROLE\",\"orderNum\":99,\"dataScope\":1,\"status\":0,\"remark\":\"脚本自动创建\"}"
  local role_id
  role_id="$(extract_data_id "${last_body}")"
  if [ -n "${role_id}" ]; then
    run_case "查询角色详情" "GET" "/system/role/${role_id}"
    run_case "更新角色" "PUT" "/system/role/${role_id}" "{\"roleName\":\"接口测试角色更新\",\"roleCode\":\"API_TEST_ROLE\",\"orderNum\":98,\"dataScope\":1,\"status\":0,\"remark\":\"脚本自动更新\"}"
  else
    record_skip_case "角色详情更新删除" "GET/PUT/DELETE" "/system/role/{id}" "-" "创建角色未返回 data.id，跳过串联用例"
  fi
  run_case "查询菜单列表" "GET" "/system/menu"
  run_case "创建菜单" "POST" "/system/menu" "{\"menuName\":\"接口测试菜单\",\"parentId\":0,\"orderNum\":99,\"path\":\"/api-test/menu\",\"component\":\"system/api-test/index\",\"isFrame\":1,\"menuType\":\"C\",\"visible\":\"0\",\"status\":\"0\",\"perms\":\"api:test:menu\",\"icon\":\"documentation\",\"remark\":\"脚本自动创建\"}"
  local menu_id
  menu_id="$(extract_data_id "${last_body}")"
  if [ -n "${menu_id}" ]; then
    run_case "查询菜单详情" "GET" "/system/menu/${menu_id}"
    run_case "更新菜单" "PUT" "/system/menu/${menu_id}" "{\"menuName\":\"接口测试菜单更新\",\"parentId\":0,\"orderNum\":98,\"path\":\"/api-test/menu\",\"component\":\"system/api-test/index\",\"isFrame\":1,\"menuType\":\"C\",\"visible\":\"0\",\"status\":\"0\",\"perms\":\"api:test:menu\",\"icon\":\"documentation\",\"remark\":\"脚本自动更新\"}"
  else
    record_skip_case "菜单详情更新删除" "GET/PUT/DELETE" "/system/menu/{id}" "-" "创建菜单未返回 data.id，跳过串联用例"
  fi
  run_case "查询字典列表" "GET" "/system/dict"
  run_case "创建字典项" "POST" "/system/dict" "{\"dictName\":\"接口测试字典\",\"dictType\":\"api_test\",\"dictLabel\":\"接口测试\",\"dictValue\":\"API_TEST\",\"dictSort\":99,\"remark\":\"脚本自动创建\"}"
  local dict_id
  dict_id="$(extract_data_id "${last_body}")"
  if [ -n "${dict_id}" ]; then
    run_case "查询字典详情" "GET" "/system/dict/${dict_id}"
    run_case "更新字典项" "PUT" "/system/dict/${dict_id}" "{\"dictName\":\"接口测试字典\",\"dictType\":\"api_test\",\"dictLabel\":\"接口测试更新\",\"dictValue\":\"API_TEST\",\"dictSort\":98,\"remark\":\"脚本自动更新\"}"
    run_case "删除字典项" "DELETE" "/system/dict/${dict_id}"
  else
    record_skip_case "字典详情更新删除" "GET/PUT/DELETE" "/system/dict/{id}" "-" "创建字典未返回 data.id，跳过串联用例"
  fi
  run_case "查询参数配置列表" "GET" "/system/config"
  run_case "创建参数配置" "POST" "/system/config" "{\"configName\":\"接口超时时间\",\"configKey\":\"api.test.timeout\",\"configValue\":\"30\",\"remark\":\"脚本自动创建\"}"
  local config_id
  config_id="$(extract_data_id "${last_body}")"
  if [ -n "${config_id}" ]; then
    run_case "查询参数配置详情" "GET" "/system/config/${config_id}"
    run_case "更新参数配置" "PUT" "/system/config/${config_id}" "{\"configName\":\"接口超时时间\",\"configKey\":\"api.test.timeout\",\"configValue\":\"60\",\"remark\":\"接口测试更新\"}"
    run_case "删除参数配置" "DELETE" "/system/config/${config_id}"
  else
    record_skip_case "参数配置详情更新删除" "GET/PUT/DELETE" "/system/config/{id}" "-" "创建参数配置未返回 data.id，跳过串联用例"
  fi
  run_case "查询岗位列表" "GET" "/system/post"
  run_case "创建岗位" "POST" "/system/post" "{\"postName\":\"接口测试岗\",\"postCode\":\"API_TEST_POST\",\"orderNum\":99,\"status\":0,\"remark\":\"脚本自动创建\"}"
  local post_id
  post_id="$(extract_data_id "${last_body}")"
  if [ -n "${post_id}" ]; then
    run_case "查询岗位详情" "GET" "/system/post/${post_id}"
    run_case "更新岗位" "PUT" "/system/post/${post_id}" "{\"postName\":\"接口测试岗更新\",\"postCode\":\"API_TEST_POST\",\"orderNum\":98,\"status\":0,\"remark\":\"脚本自动更新\"}"
    run_case "删除岗位" "DELETE" "/system/post/${post_id}"
  else
    record_skip_case "岗位详情更新删除" "GET/PUT/DELETE" "/system/post/{id}" "-" "创建岗位未返回 data.id，跳过串联用例"
  fi
  run_case "查询通知公告列表" "GET" "/system/notice"
  run_case "创建通知公告" "POST" "/system/notice" "{\"noticeTitle\":\"接口测试公告\",\"noticeType\":\"1\",\"noticeContent\":\"脚本自动创建公告\",\"status\":\"0\",\"remark\":\"脚本自动创建\"}"
  local notice_id
  notice_id="$(extract_data_id "${last_body}")"
  if [ -n "${notice_id}" ]; then
    run_case "查询通知公告详情" "GET" "/system/notice/${notice_id}"
    run_case "更新通知公告" "PUT" "/system/notice/${notice_id}" "{\"noticeTitle\":\"接口测试公告更新\",\"noticeType\":\"1\",\"noticeContent\":\"脚本自动更新公告\",\"status\":\"0\",\"remark\":\"脚本自动更新\"}"
    run_case "删除通知公告" "DELETE" "/system/notice/${notice_id}"
  else
    record_skip_case "通知公告详情更新删除" "GET/PUT/DELETE" "/system/notice/{id}" "-" "创建通知公告未返回 data.id，跳过串联用例"
  fi
  run_case "查询登录日志列表" "GET" "/system/log/login"
  run_case "查询操作日志列表" "GET" "/system/log/operator"
  run_case "查询用户角色授权" "GET" "/system/user-role"
  local user_role_id
  if [ -n "${business_user_id}" ] && [ -n "${role_id}" ]; then
    run_case "绑定用户角色" "POST" "/system/user-role" "{\"userId\":\"${business_user_id}\",\"roleIds\":[${role_id}]}"
    user_role_id="$(extract_data_id "${last_body}")"
    if [ -n "${user_role_id}" ]; then
      run_case "查询用户角色授权详情" "GET" "/system/user-role/${user_role_id}"
      run_case "删除用户角色授权" "DELETE" "/system/user-role/${user_role_id}"
    else
      record_skip_case "用户角色授权详情删除" "GET/DELETE" "/system/user-role/{id}" "-" "绑定用户角色未返回 data.id，跳过串联用例"
    fi
  else
    record_skip_case "绑定用户角色" "POST" "/system/user-role" "-" "缺少本轮新建用户或角色，跳过授权绑定用例，避免修改初始化授权关系"
  fi
  run_case "查询角色菜单授权" "GET" "/system/role-menu"
  local role_menu_id
  if [ -n "${role_id}" ] && [ -n "${menu_id}" ]; then
    run_case "绑定角色菜单" "POST" "/system/role-menu" "{\"roleId\":${role_id},\"menuIds\":[${menu_id}]}"
    role_menu_id="$(extract_data_id "${last_body}")"
    if [ -n "${role_menu_id}" ]; then
      run_case "查询角色菜单授权详情" "GET" "/system/role-menu/${role_menu_id}"
      run_case "删除角色菜单授权" "DELETE" "/system/role-menu/${role_menu_id}"
    else
      record_skip_case "角色菜单授权详情删除" "GET/DELETE" "/system/role-menu/{id}" "-" "绑定角色菜单未返回 data.id，跳过串联用例"
    fi
  else
    record_skip_case "绑定角色菜单" "POST" "/system/role-menu" "-" "缺少本轮新建角色或菜单，跳过授权绑定用例，避免修改初始化授权关系"
  fi
  if [ -n "${menu_id}" ]; then
    run_case "删除菜单" "DELETE" "/system/menu/${menu_id}"
  fi
  if [ -n "${role_id}" ]; then
    run_case "删除角色" "DELETE" "/system/role/${role_id}"
  fi
  if [ -n "${user_id}" ]; then
    run_case "删除后台用户" "DELETE" "/system/user/${user_id}"
  fi

  # 网关管理接口。
  run_case "查询网关路由列表" "GET" "/gateway/route"
  run_case "创建网关路由" "POST" "/gateway/route" "{\"routeCode\":\"api-test-route\",\"uri\":\"lb://hospital-system\",\"pathPredicate\":\"/api-test/**\",\"sort\":999,\"status\":\"0\",\"remark\":\"脚本自动创建\"}"
  local gateway_route_id
  gateway_route_id="$(extract_data_id "${last_body}")"
  if [ -n "${gateway_route_id}" ]; then
    run_case "查询网关路由详情" "GET" "/gateway/route/${gateway_route_id}"
    run_case "更新网关路由" "PUT" "/gateway/route/${gateway_route_id}" "{\"routeCode\":\"api-test-route\",\"uri\":\"lb://hospital-system\",\"pathPredicate\":\"/api-test/**\",\"sort\":998,\"status\":\"0\",\"remark\":\"脚本自动更新\"}"
    run_case "删除网关路由" "DELETE" "/gateway/route/${gateway_route_id}"
  else
    record_skip_case "网关路由详情更新删除" "GET/PUT/DELETE" "/gateway/route/{id}" "-" "创建网关路由未返回 data.id，跳过串联用例"
  fi

  if [ "${TENANT_HEADER}" = "0" ]; then
    record_skip_case "业务模块接口" "GET/POST/PUT" "/patient,/doctor,/appointment,/consult,/prescription,/drug,/order" "-" "当前默认账号为平台租户，业务服务拒绝平台租户上下文；设置 HLW_API_TENANT_ID 为业务租户并使用业务账号后执行"
    record_skip_case "问诊 WebSocket 通道" "WS" "/ws/consult/{consultId}" "{\"consultId\":\"占位示例\"}" "当前默认账号为平台租户，业务服务拒绝平台租户上下文；业务租户场景建议使用专用 ws 客户端补充验证"
  else
    # 患者端首页与档案接口。
    run_case "查询当前患者档案" "GET" "/patient/profile"
    run_case "更新当前患者档案" "PUT" "/patient/profile" "{\"patientName\":\"张小满\",\"phone\":\"13800000009\",\"gender\":\"女\",\"age\":28,\"riskLevel\":\"中风险\",\"idCard\":\"330101199801010011\",\"birthday\":\"1998-01-01\",\"address\":\"杭州市上城区\",\"lastVisit\":\"2026-06-13\"}"
    run_case "查询患者列表" "GET" "/patient/patients"
    run_case "查询患者详情" "GET" "/patient/patients/1"
    run_case "创建患者档案" "POST" "/patient/patients" "{\"patientName\":\"接口测试患者\",\"phone\":\"13800001234\",\"gender\":\"男\",\"age\":36,\"riskLevel\":\"低风险\",\"idCard\":\"330101198801010012\",\"birthday\":\"1988-01-01\",\"address\":\"杭州市余杭区\",\"lastVisit\":\"2026-06-12\"}"
    run_case "更新患者档案" "PUT" "/patient/patients/1" "{\"patientName\":\"赵晓岚\",\"phone\":\"13900001111\",\"gender\":\"女\",\"age\":35,\"riskLevel\":\"中风险\",\"idCard\":\"110101199201010011\",\"birthday\":\"1992-01-01\",\"address\":\"杭州市西湖区\",\"lastVisit\":\"2026-06-12\"}"
    run_case "查询健康档案列表" "GET" "/patient/health-records?patientId=1"
    run_case "创建健康档案" "POST" "/patient/health-records" "{\"patientId\":1,\"title\":\"接口测试档案\",\"summary\":\"脚本自动创建\",\"allergies\":\"无\",\"history\":\"随访记录\",\"diagnosis\":\"血压稳定\",\"remark\":\"自动化脚本写入\"}"

    # 医生与排班接口。
    run_case "查询科室列表" "GET" "/doctor/departments"
    run_case "创建科室" "POST" "/doctor/departments" "{\"name\":\"接口测试科室\",\"status\":\"启用\"}"
    run_case "查询医生列表" "GET" "/doctor/doctors"
    run_case "查询医生详情" "GET" "/doctor/doctors/1"
    run_case "创建医生" "POST" "/doctor/doctors" "{\"name\":\"接口测试医生\",\"title\":\"主治医师\",\"department\":\"全科\",\"specialty\":\"慢病复诊\",\"consultFee\":30,\"consultStatus\":\"ONLINE\",\"status\":\"接诊中\",\"schedule\":\"2026-06-13 上午\"}"
    run_case "更新医生状态" "PUT" "/doctor/doctors/1/status" "{\"status\":\"ONLINE\"}"
    run_case "绑定医生科室" "POST" "/doctor/doctors/1/departments" "{\"departmentId\":10,\"appointmentFee\":50}"
    run_case "查询排班列表" "GET" "/doctor/schedules"
    run_case "创建排班" "POST" "/doctor/schedules" "{\"doctorId\":1,\"slot\":\"2026-06-13 上午\",\"scheduleDate\":\"2026-06-13\",\"timeSlot\":\"上午\",\"totalNumber\":30,\"remainNumber\":30}"
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
    run_case "驳回处方" "POST" "/prescription/prescriptions/3/reject" "{\"remark\":\"接口测试驳回\"}"

    # 药品库存接口。
    run_case "查询药品列表" "GET" "/drug/drugs"
    run_case "创建药品资料" "POST" "/drug/drugs" "{\"drugName\":\"接口测试药品\",\"spec\":\"10mg*12片\",\"inventory\":100}"
    run_case "查询库存列表" "GET" "/drug/stocks"
    run_case "创建库存记录" "POST" "/drug/stocks" "{\"drugId\":1,\"warehouseName\":\"接口测试仓\",\"inventory\":20}"
    run_case "配送单发货" "POST" "/drug/deliveries/1/ship"

    # 订单接口。
    run_case "查询订单列表" "GET" "/order/orders"
    run_case "创建订单" "POST" "/order/orders" "{\"bizType\":\"APPOINTMENT\",\"bizId\":1,\"patientId\":1,\"patientName\":\"张小满\",\"amount\":25}"
    run_case "模拟支付订单" "POST" "/order/orders/1/pay" "{\"payMethod\":\"MOCK_PAY\"}"
  fi

  # 认证退出接口放在最后执行，避免提前失效影响后续用例。
  run_case "退出登录" "POST" "/auth/logout"
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
