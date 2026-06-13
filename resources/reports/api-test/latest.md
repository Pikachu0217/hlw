# 互联网医院接口测试报告

- 测试时间：2026-06-13 11:29:21
- 基础地址：http://127.0.0.1:9000
- 测试目标：微服务直连：auth=9100, system=9200, patient=9300, doctor=9400, consult=9500, appointment=9600, prescription=9700, drug=9800, order=9900
- 直连模式：1
- 登录账号：admin
- 总数：51
- 通过：50
- 失败：0
- 跳过：1

| 用例 | 方法 | 路径 | 结果 | HTTP | 业务 code | 耗时 ms | 请求参数 | 响应摘要 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 认证登录 | POST | /auth/login | PASS | 200 | 200 | 189 | {"username":"admin","password":"admin123"} | {"code":200,"message":"success","data":{"token":"satoken-demo-1-100","tenantId":100,"userType":"ADMIN"}} |
| 查询登录用户资料 | GET | /auth/profile | PASS | 200 | 200 | 75 | - | {"code":200,"message":"success","data":{}} |
| 退出登录 | POST | /auth/logout | PASS | 200 | 200 | 45 | - | {"code":200,"message":"success","data":null} |
| 查询租户列表 | GET | /system/tenants | PASS | 200 | 200 | 56 | - | {"code":200,"message":"success","data":[{"key":"1","tenantName":"海岚门诊","packageName":"标准医疗版","adminName":"刘院长","expireAt":"2026-12-31","status":"正常"},{"key":"2","tenantName":"青禾互联网医院","packageName":"集团旗舰版","adminName":"姜... |
| 创建租户 | POST | /system/tenants | PASS | 200 | 200 | 39 | {} | {"code":200,"message":"success","data":{}} |
| 查询后台用户列表 | GET | /system/users | PASS | 200 | 200 | 32 | - | {"code":200,"message":"success","data":[{"key":"1","username":"门诊运营","deptName":"运营中心","roleName":"运营管理员","phone":"13800001111","lastLogin":"今天 08:40","status":"启用"},{"key":"2","username":"药房主管","deptName":"药房组","roleNam... |
| 查询角色列表 | GET | /system/roles | PASS | 200 | 200 | 33 | - | {"code":200,"message":"success","data":[{"key":"1","roleName":"系统管理员","dataScope":"全部数据","memberCount":3,"updatedAt":"2026-06-10 11:20","status":"启用"},{"key":"2","roleName":"运营管理员","dataScope":"本租户数据","memberCount":11,"u... |
| 查询菜单列表 | GET | /system/menus | PASS | 200 | 200 | 34 | - | {"code":200,"message":"success","data":[{"key":"1","menuName":"工作台","menuType":"菜单","permission":"dashboard:view","routePath":"/dashboard","status":"启用"},{"key":"2","menuName":"医生管理","menuType":"菜单","permission":"doctor:... |
| 查询当前患者档案 | GET | /patient/profile | PASS | 200 | 200 | 45 | - | {"code":200,"message":"success","data":{"id":1,"name":"赵晓岚","maskedPhone":"139****1111","gender":"女"}} |
| 更新当前患者档案 | PUT | /patient/profile | PASS | 200 | 200 | 40 | {"name":"张小满","phone":"13800000009","gender":"女"} | {"code":200,"message":"success","data":{"id":1,"name":"张小满","maskedPhone":"138****0009","gender":"女"}} |
| 查询患者列表 | GET | /patient/patients | PASS | 200 | 200 | 32 | - | {"code":200,"message":"success","data":[{"key":"1","patientName":"赵晓岚","gender":"女","age":34,"riskLevel":"中风险","phone":"13900001111","lastVisit":"2026-06-11"},{"key":"2","patientName":"沈博远","gender":"男","age":58,"riskLev... |
| 查询健康档案列表 | GET | /patient/health-records | PASS | 200 | 200 | 34 | - | {"code":200,"message":"success","data":[{"id":1,"title":"发热问诊","summary":"儿童发热 12 小时，已线上问诊"},{"id":2,"title":"复诊续方","summary":"慢病用药复诊记录"}]} |
| 创建健康档案 | POST | /patient/health-records | PASS | 200 | 200 | 33 | {"title":"接口测试档案","summary":"脚本自动创建"} | {"code":200,"message":"success","data":{"title":"接口测试档案","summary":"脚本自动创建"}} |
| 查询科室列表 | GET | /doctor/departments | PASS | 200 | 200 | 40 | - | {"code":200,"message":"success","data":[{"id":10,"name":"心内科","doctorCount":8,"queue":"当前等候 6 人"},{"id":20,"name":"儿科","doctorCount":12,"queue":"当前等候 8 人"},{"id":30,"name":"皮肤科","doctorCount":5,"queue":"当前等候 3 人"}]} |
| 创建科室 | POST | /doctor/departments | PASS | 200 | 200 | 31 | {"name":"接口测试科室","status":"启用"} | {"code":200,"message":"success","data":{"name":"接口测试科室","status":"启用"}} |
| 查询医生列表 | GET | /doctor/doctors | PASS | 200 | 200 | 29 | - | {"code":200,"message":"success","data":[{"id":1,"key":"1","name":"陈知衡","title":"主任医师","department":"心内科","specialty":"冠脉慢病管理","status":"接诊中","consultStatus":"ONLINE","schedule":"上午门诊","patientCount":16,"consultFee":"50.0... |
| 查询医生详情 | GET | /doctor/doctors/1 | PASS | 200 | 200 | 30 | - | {"code":200,"message":"success","data":{"id":1,"key":"1","name":"陈知衡","title":"主任医师","department":"心内科","specialty":"冠脉慢病管理","status":"接诊中","consultStatus":"ONLINE","schedule":"上午门诊","patientCount":16,"consultFee":"50.00... |
| 创建医生 | POST | /doctor/doctors | PASS | 200 | 200 | 30 | {"name":"接口测试医生","title":"主治医师","department":"全科"} | {"code":200,"message":"success","data":{"name":"接口测试医生","title":"主治医师","department":"全科"}} |
| 更新医生状态 | PUT | /doctor/doctors/1/status | PASS | 200 | 200 | 30 | {"status":"ONLINE"} | {"code":200,"message":"success","data":{"status":"ONLINE","id":1}} |
| 绑定医生科室 | POST | /doctor/doctors/1/departments | PASS | 200 | 200 | 30 | {"departmentId":1} | {"code":200,"message":"success","data":{"doctorId":1,"department":{"departmentId":1}}} |
| 查询排班列表 | GET | /doctor/schedules | PASS | 200 | 200 | 30 | - | {"code":200,"message":"success","data":[{"id":1,"doctorId":1,"doctorName":"陈知衡","slot":"上午","remain":6},{"id":2,"doctorId":2,"doctorName":"顾清和","slot":"下午","remain":1}]} |
| 创建排班 | POST | /doctor/schedules | PASS | 200 | 200 | 32 | {"doctorId":1,"slot":"2026-06-13 上午"} | {"code":200,"message":"success","data":{"doctorId":1,"slot":"2026-06-13 上午"}} |
| 计算挂号费 | POST | /doctor/appointment-fee/resolve | PASS | 200 | 200 | 44 | {"title":"主任医师","doctorFee":80,"departmentFee":20} | {"code":200,"message":"success","data":20} |
| 查询预约单列表 | GET | /appointment/appointments | PASS | 200 | 200 | 207 | - | {"code":200,"message":"success","data":[{"key":"1","appointmentNo":"YY20260612001","patientName":"赵晓岚","doctorName":"陈知衡","clinicTime":"2026-06-13 14:00","source":"小程序","status":"待就诊"},{"key":"2","appointmentNo":"YY20260... |
| 创建预约单 | POST | /appointment/appointments | PASS | 200 | 200 | 45 | {"doctorName":"陈知衡","timeSlot":"2026-06-13 上午"} | {"code":200,"message":"success","data":{"doctorName":"陈知衡","id":1,"status":"PENDING_PAY","appointmentNo":"YY20260612001"}} |
| 支付预约单 | POST | /appointment/appointments/1/pay | PASS | 200 | 200 | 31 | - | {"code":200,"message":"success","data":{"id":1,"status":"PAID"}} |
| 预约签到 | POST | /appointment/appointments/1/check-in | PASS | 200 | 200 | 29 | - | {"code":200,"message":"success","data":{"id":1,"status":"CHECKED_IN"}} |
| 抢便民门诊预约单 | POST | /appointment/appointments/1/grab | PASS | 200 | 200 | 58 | {"doctorId":20} | {"code":200,"message":"success","data":true} |
| 查询号源列表 | GET | /appointment/number-sources | PASS | 200 | 200 | 35 | - | {"code":200,"message":"success","data":[{"id":1,"scheduleId":1,"numberSeq":1,"status":"AVAILABLE"},{"id":2,"scheduleId":1,"numberSeq":2,"status":"AVAILABLE"}]} |
| 锁定号源 | POST | /appointment/number-sources/1/lock | PASS | 200 | 200 | 28 | - | {"code":200,"message":"success","data":{"id":1,"scheduleId":1,"numberSeq":1,"status":"LOCKED"}} |
| 创建放号配置 | POST | /appointment/release-configs | PASS | 200 | 200 | 31 | {"scheduleId":1,"releaseAt":"2026-06-13 08:00:00"} | {"code":200,"message":"success","data":{"scheduleId":1,"releaseAt":"2026-06-13 08:00:00"}} |
| 查询问诊单列表 | GET | /consult/consults | PASS | 200 | 200 | 37 | - | {"code":200,"message":"success","data":[{"key":"1","consultNo":"ZX20260612001","patientName":"赵晓岚","doctorName":"陈知衡","channel":"图文","status":"待接单","updatedAt":"10:18"},{"key":"2","consultNo":"ZX20260612002","patientName... |
| 创建图文问诊 | POST | /consult/consults | PASS | 200 | 200 | 53 | {"type":"IMAGE_TEXT","chiefComplaint":"接口测试问诊"} | {"code":200,"message":"success","data":{"type":"IMAGE_TEXT","id":1,"status":"WAITING","chiefComplaint":"接口测试问诊"}} |
| 接单问诊 | POST | /consult/consults/1/accept | PASS | 200 | 200 | 34 | {"tenantId":100} | {"code":200,"message":"success","data":{"id":1,"tenantId":100,"status":"IN_PROGRESS","durationLimit":30,"remainingSeconds":1800}} |
| 完成问诊 | POST | /consult/consults/1/complete | PASS | 200 | 200 | 28 | - | {"code":200,"message":"success","data":{"id":1,"status":"COMPLETED"}} |
| 延长问诊 | POST | /consult/consults/1/extend | PASS | 200 | 200 | 27 | - | {"code":200,"message":"success","data":{"id":1,"status":"EXTENDED"}} |
| 查询问诊消息 | GET | /consult/consults/1/messages | PASS | 200 | 200 | 34 | - | {"code":200,"message":"success","data":[{"consultId":1,"senderId":2,"senderType":"DOCTOR","content":"哪里不舒服","contentType":"TEXT","read":false,"createTime":"2026-06-13T10:15:00"},{"consultId":1,"senderId":1,"senderType":"... |
| 问诊 WebSocket 通道 | WS | /ws/consult/{consultId} | SKIP | - | - | - | {"consultId":"占位示例"} | WebSocket 长连接不适合用 curl 在本脚本中断言，建议使用专用 ws 客户端补充验证 |
| 查询处方列表 | GET | /prescription/prescriptions | PASS | 200 | 200 | 41 | - | {"code":200,"message":"success","data":[{"key":"1","prescriptionNo":"CF20260612001","patientName":"赵晓岚","doctorName":"陈知衡","drugCount":3,"issuedAt":"09:42","status":"待审方"},{"key":"2","prescriptionNo":"CF20260612002","pat... |
| 创建处方草稿 | POST | /prescription/prescriptions | PASS | 200 | 200 | 31 | {"patientId":1,"doctorId":1,"drugIds":[1]} | {"code":200,"message":"success","data":{"patientId":1,"doctorId":1,"drugIds":[1]}} |
| 提交处方 | POST | /prescription/prescriptions/1/submit | PASS | 200 | 200 | 27 | - | {"code":200,"message":"success","data":{"id":1,"status":"SUBMITTED"}} |
| 审核通过处方 | POST | /prescription/prescriptions/1/approve | PASS | 200 | 200 | 37 | {"pharmacistId":1,"remark":"接口测试通过"} | {"code":200,"message":"success","data":{"id":1,"status":"AUDITED","pharmacistId":1,"auditRemark":"接口测试通过"}} |
| 驳回处方 | POST | /prescription/prescriptions/1/reject | PASS | 200 | 200 | 31 | {"remark":"接口测试驳回"} | {"code":200,"message":"success","data":{"id":1,"status":"REJECTED","remark":"接口测试驳回"}} |
| 查询药品列表 | GET | /drug/drugs | PASS | 200 | 200 | 42 | - | {"code":200,"message":"success","data":[{"key":"1","drugName":"阿托伐他汀钙片","spec":"20mg*14片","inventory":124,"unit":"盒","warningStatus":"正常"},{"key":"2","drugName":"盐酸二甲双胍缓释片","spec":"0.5g*30片","inventory":42,"unit":"盒","wa... |
| 创建药品资料 | POST | /drug/drugs | PASS | 200 | 200 | 30 | {"drugName":"接口测试药品","spec":"10mg*12片","inventory":100} | {"code":200,"message":"success","data":{"drugName":"接口测试药品","spec":"10mg*12片","inventory":100}} |
| 查询库存列表 | GET | /drug/stocks | PASS | 200 | 200 | 29 | - | {"code":200,"message":"success","data":[{"key":"1","drugName":"阿托伐他汀钙片","warehouseName":"中心药房","inventory":124,"warningStatus":"正常"},{"key":"2","drugName":"盐酸二甲双胍缓释片","warehouseName":"中心药房","inventory":42,"warningStatus"... |
| 创建库存记录 | POST | /drug/stocks | PASS | 200 | 200 | 30 | {"drugId":1,"warehouseName":"接口测试仓","inventory":20} | {"code":200,"message":"success","data":{"drugId":1,"warehouseName":"接口测试仓","inventory":20}} |
| 配送单发货 | POST | /drug/deliveries/1/ship | PASS | 200 | 200 | 28 | - | {"code":200,"message":"success","data":{"id":1,"status":"SHIPPED"}} |
| 查询订单列表 | GET | /order/orders | PASS | 200 | 200 | 37 | - | {"code":200,"message":"success","data":[{"key":"1","orderNo":"DD20260612001","businessType":"门诊预约","patientName":"赵晓岚","amount":"¥58.00","payStatus":"已支付","createdAt":"09:12"},{"key":"2","orderNo":"DD20260612002","busine... |
| 创建订单 | POST | /order/orders | PASS | 200 | 200 | 30 | {"businessType":"APPOINTMENT","patientName":"张小满","amount":25} | {"code":200,"message":"success","data":{"businessType":"APPOINTMENT","patientName":"张小满","amount":25}} |
| 模拟支付订单 | POST | /order/orders/1/pay | PASS | 200 | 200 | 30 | {"payMethod":"MOCK_PAY"} | {"code":200,"message":"success","data":{"id":1,"status":"PAID","payMethod":"MOCK_PAY"}} |

