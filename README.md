# Spring Boot 漏洞靶场修复实战

本项目基于 `vulnerable-spring-boot`，完成了 10 个常见 Web 漏洞 的 发现 → 验证 → 修复 → 验证 全过程。旨在通过实战方式，深入理解漏洞成因与修复方法，适合作为安全开发与代码审计的入门项目。

## 修复的漏洞清单

| 漏洞类型 | CWE | 修复方式 |
|----------|-----|----------|
| SQL 注入 | CWE-89 | 使用 `PreparedStatement` 参数化查询 |
| 命令注入 | CWE-78 | 使用 `ProcessBuilder` 分离命令与参数 |
| 路径遍历 | CWE-22 | `getCanonicalPath()` + 基础目录校验 |
| 硬编码凭证 | CWE-798 | 使用 `@Value` 注入外部配置文件 |
| 敏感数据暴露 | CWE-200 | 删除 `/api/config` 接口 |
| XXE | CWE-611 | 禁用 DOCTYPE 声明 |
| 不安全反序列化 | CWE-502 | 删除 `/api/deserialize` 接口 |
| 缺少身份验证 | CWE-306 | 使用 `@PreAuthorize` 权限注解 |
| SSRF | CWE-918 | 白名单域名 + 内网 IP 拦截 + 协议限制 |
| 弱加密算法 | CWE-327 | 使用 AES-256 替换 DES |

## 技术栈

- Java 11
- Spring Boot 2.5.0
- H2 Database（内存数据库）
- Maven
- Lombok

## 进入项目目录
cd springboot-web-Fix

## 编译并运行
mvn clean install

mvn spring-boot:run

## 关键修复示例
1. **SQL 注入修复**

修复前（拼接写法）：

用户输入被直接拼进SQL语句，相当于让用户“参与”了代码的编写。比如输入 admin' OR '1'='1，拼出来后变成了 SELECT * FROM users WHERE username = 'admin' OR '1'='1'，这个 OR '1'='1' 被当作SQL代码执行了，所以会返回所有用户的数据。

修复后（PreparedStatement）：

SQL语句的“骨架”（SELECT * FROM users WHERE username = ?）先发给数据库编译，? 是固定的数据占位符，结构已经定死了。用户输入的内容只是填充到占位符里的数据，不管输入什么，数据库都只把它当作普通字符串来匹配，不会当作代码执行。即使输入 admin' OR '1'='1，数据库也只会去查找用户名叫 "admin' OR '1'='1" 的人——当然不存在，所以什么都查不到。



2. **SSRF 修复**

修复前：

用户传什么 URL，服务器就去访问什么地址。相当于一个“听话的快递员”，你说去哪它就去哪，完全不问安不安全。攻击者可以让它访问内网服务（如 http://localhost:8080/api/config）、读取本地文件（如 file:///etc/passwd），甚至扫描内网里的其他机器。

修复后：
给这个“快递员”装了一个门禁系统：

只允许去白名单里的地址（比如 api.example.com），其他一概拒绝；

拦截内网 IP（127.0.0.1、192.168.x.x、10.x.x.x 等），不让它访问内部资源；

只允许 HTTP/HTTPS 协议，禁止 file:// 这种危险的协议。



3. **硬编码凭证修复**

修复前：

管理员账号密码直接以明文写在 Java 代码里。相当于把保险柜的密码贴在保险柜门上，谁拿到源码（比如通过路径遍历漏洞读取文件，或者代码仓库泄露），谁就能直接用 admin/admin123 登录系统。

修复后：

账号密码从代码里移出来，放在外部配置文件（application.properties）中，通过 @Value 注解注入到代码里。代码里只保留变量名，不保留具体值。配置文件不提交到代码仓库，避免泄露。

## 目录结构
```
springboot-vulnerability-lab/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/vulnerable/
│       │       ├── VulnerableApplication.java   # 主启动类
│       │       └── VulnerableController.java    # 漏洞接口
│       └── resources/
│           ├── application.properties           # 配置文件
│           └── data.sql                         # H2 初始化数据
├── uploads/                                     # 文件上传测试目录
├── .gitignore                                   # Git 忽略文件
├── pom.xml                                      # Maven 配置
└── README.md                                    # 项目文档
