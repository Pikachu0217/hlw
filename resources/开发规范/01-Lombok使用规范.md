# Lombok 使用规范

## 1. 目的

为统一后端代码风格，降低样板代码数量，同时避免因 Lombok 滥用带来的可读性、调试性和对象语义问题，特制定本规范。

## 2. 适用范围

本规范适用于 `code/backend/` 下所有 Java 代码，包括但不限于：

- Controller
- Service
- Manager
- DTO
- VO
- Query
- Entity
- 配置类

## 3. 总体原则

1. 可以使用 Lombok，但必须按场景谨慎使用，禁止无差别添加注解。
2. 优先选择语义明确、影响范围可控的注解，避免使用“生成过多方法”的聚合型注解。
3. 涉及实体语义、日志安全、对象比较、敏感字段输出时，必须显式控制生成行为。
4. 代码可读性优先于省略样板代码，不能为了少写几行代码牺牲维护性。

## 4. `@Slf4j` 使用规范

### 4.1 是否推荐

`@Slf4j` 属于项目中推荐使用的注解，可作为默认日志注解使用。

### 4.2 适用场景

以下场景建议使用 `@Slf4j`：

- Controller 入口类
- Service 实现类
- 定时任务类
- 消息监听或消息消费类
- 关键业务编排类
- 需要记录操作链路和异常信息的组件类

### 4.3 使用要求

1. Controller 入口方法必须记录 `log.info`，至少包含接口名、核心入参、请求标识。
2. 关键 Service 方法必须记录 `log.info` 或 `log.warn`，至少包含业务主键、关键分支和处理结果。
3. 异常场景必须记录错误上下文，避免只打印“操作失败”等无效日志。
4. 日志中不得直接输出密码、身份证号、手机号、银行卡号、token、验证码等敏感信息。
5. 大对象日志输出前应做脱敏或裁剪，避免日志膨胀和泄露风险。

### 4.4 示例

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatientServiceImpl implements PatientService {

    /**
     * 根据患者编号查询患者信息。
     *
     * @param patientId 患者编号
     * @return 患者信息
     */
    @Override
    public PatientVO getPatientById(Long patientId) {
        log.info("查询患者信息，patientId={}", patientId);
        return new PatientVO();
    }
}
```

## 5. `@Data` 使用规范

### 5.1 是否推荐

`@Data` 不作为全场景默认推荐注解，只能在简单数据承载对象中谨慎使用。

### 5.2 适用场景

以下对象可以优先评估使用 `@Data`：

- 纯 DTO
- 纯 VO
- 请求参数对象
- 返回结果对象
- 无复杂关联、无敏感字段、无自定义比较语义的简单对象

### 5.3 禁止或不推荐场景

以下场景禁止或不推荐直接使用 `@Data`：

- Entity 实体类
- 领域模型对象
- 包含集合、继承层次或复杂关联关系的对象
- 包含密码、密钥、证件号、手机号等敏感字段的对象
- 需要精确控制 `equals`、`hashCode`、`toString` 行为的对象
- 可能参与缓存键、去重比较、集合判等的对象

### 5.4 原因说明

`@Data` 会一次性生成以下内容：

- `getter`
- `setter`
- `toString`
- `equals`
- `hashCode`
- `requiredArgsConstructor`

虽然使用方便，但在企业级项目中存在以下风险：

1. `toString()` 可能打印敏感字段，造成日志泄露。
2. `equals()` 和 `hashCode()` 默认参与全部字段，可能不符合业务主键语义。
3. 复杂对象存在双向引用时，可能导致 `toString()` 递归输出问题。
4. 后期局部定制某个方法时，可读性和维护成本较高。

### 5.5 替代建议

对于大多数业务对象，优先使用以下更细粒度的注解组合：

- `@Getter`
- `@Setter`
- `@ToString`
- `@EqualsAndHashCode`

如对象仅需读取能力，可优先考虑：

- `@Getter`

如对象不希望暴露全部字段输出，可在 `@ToString` 中显式排除字段，或直接手写 `toString()`。

## 6. 其他常用注解建议

### 6.1 `@Getter` / `@Setter`

推荐使用，适合绝大多数普通 Java Bean。相比 `@Data` 更可控，也更符合企业项目的长期维护要求。

### 6.2 `@Builder`

可用于创建参数较多的 DTO、VO 或命令对象，但应避免在 JPA 风格实体或框架强依赖无参构造器的类中滥用。

### 6.3 `@NoArgsConstructor` / `@AllArgsConstructor`

可以使用，但应结合框架要求和对象语义判断是否必要，不能为了“补齐注解”而机械添加。

### 6.4 `@Accessors(chain = true)`

默认不推荐在核心业务模型中使用。链式调用虽然简洁，但会降低部分代码的语义清晰度，也不利于统一风格。

## 7. 推荐落地规则

为便于团队统一执行，建议按以下规则落地：

1. Controller、Service、Manager 默认使用 `@Slf4j`。
2. DTO、VO、Query 如字段简单且无敏感信息，可使用 `@Data`。
3. Entity 默认使用 `@Getter`、`@Setter`，禁止直接使用 `@Data`。
4. 涉及对象比较语义的类，必须显式声明 `@EqualsAndHashCode` 规则，必要时手写实现。
5. 涉及日志输出的类，必须评估敏感字段脱敏策略。
6. 代码评审时应重点检查 `@Data` 是否误用于实体类、复杂对象和敏感对象。

## 8. 推荐示例

### 8.1 DTO 示例

```java
import lombok.Data;

@Data
public class DoctorQueryDTO {

    /** 医生姓名 */
    private String doctorName;

    /** 科室编号 */
    private Long departmentId;
}
```

### 8.2 Entity 示例

```java
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"idCardNo", "phone"})
public class PatientEntity {

    /** 主键 */
    private Long id;

    /** 患者姓名 */
    private String patientName;

    /** 身份证号 */
    private String idCardNo;

    /** 手机号 */
    private String phone;
}
```

## 9. 结论

在企业级项目中：

- `@Slf4j` 可以作为常规推荐写法使用。
- `@Data` 可以使用，但不能滥用，尤其不能无脑用于 Entity。

如无特殊原因，团队默认采取“日志注解放开、聚合注解收敛、实体对象从严”的策略。
