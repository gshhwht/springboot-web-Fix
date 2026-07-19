package com.example.vulnerable;

import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileReader;
import org.springframework.beans.factory.annotation.Value;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputFilter;

@RestController
@RequestMapping("/api")
public class VulnerableController {

    // Vulnerability 1: SQL Injection
    @GetMapping("/user")
    public Map<String, Object> getUser(@RequestParam String username) {
        Map<String, Object> result = new HashMap<>();
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            // SQL Injection vulnerability - directly concatenating user input
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1,username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                result.put("username", rs.getString("username"));
                result.put("email", rs.getString("email"));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    // Vulnerability 2: Command Injection
    @GetMapping("/ping")
    public Map<String, String> ping(@RequestParam String host) {
        Map<String, String> result = new HashMap<>();
        try {
            // Command Injection vulnerability - directly using user input in system command
            Process process = Runtime.getRuntime().exec("ping -n 1 " + host);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            result.put("output", output.toString());
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    // Vulnerability 3: Path Traversal
    @GetMapping("/file")
    public Map<String, String> readFile(@RequestParam String filename) {
        Map<String, String> result = new HashMap<>();
        try {
            // Path Traversal vulnerability - no validation of file path
            //java.io.File file = new java.io.File(filename);
            // 1. 定义安全的基础目录（文件只能从这里读取）
            String baseDir = System.getProperty("user.dir") + File.separator + "uploads";
            File baseFile = new File(baseDir).getCanonicalFile();

            // 2. 用用户输入和基础目录拼接，然后获取其真实路径
            File file = new File(baseFile, filename);
            File canonicalFile = file.getCanonicalFile();

            // 3. 核心校验：真实路径是否以基础目录开头？
            if (!canonicalFile.getPath().startsWith(baseFile.getPath())) {
                result.put("error", "Access denied: Illegal path.");
                return result;
            }


            BufferedReader reader = new BufferedReader(new java.io.FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            result.put("content", content.toString());
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    // Vulnerability 4: Hardcoded Credentials
    // 从配置文件读取，不再硬编码
    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/admin/login")
    public Map<String, Object> adminLogin(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        // Hardcoded credentials vulnerability
        //String adminUsername = "admin";
        //String adminPassword = "admin123";

        if (username.equals(adminUsername) && password.equals(adminPassword)) {
            result.put("success", true);
            result.put("message", "Admin login successful");

            String token = generateJwtToken(username);
            result.put("token", token);
        } else {
            result.put("success", false);
            result.put("message", "Invalid credentials");
        }
        return result;
    }

    private String generateJwtToken(String username) {
        // 简单示例，生产环境请用 jjwt 或 Nimbus JOSE 等标准库
        long now = System.currentTimeMillis();
        long expiry = now + 3600000; // 1小时后过期
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiI" + username + "IiwiZXhwIjoi" + expiry + "IiwiaWF0Ijoi" + now + "\"}." +
                "fake-signature-with-" + jwtSecret;
    }

    // Vulnerability 5: Sensitive Data Exposure
    //建议注释掉接口
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        // Exposing sensitive configuration data
        config.put("database_url", "jdbc:mysql://localhost:3306/mydb");
        config.put("database_username", "root");
        config.put("database_password", "root123");
        config.put("api_key", "sk-1234567890abcdef");
        config.put("secret_key", "my-super-secret-key");
        return config;
    }

    // Vulnerability 6: XML External Entity (XXE)
    @PostMapping("/parse-xml")
    public Map<String, String> parseXml(@RequestBody String xmlContent) {
        Map<String, String> result = new HashMap<>();
        try {
            javax.xml.parsers.DocumentBuilderFactory factory =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance();

            // 核心修复：彻底禁止 DOCTYPE 声明
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                    new java.io.ByteArrayInputStream(xmlContent.getBytes())
            );
            result.put("status", "parsed");
            result.put("root", doc.getDocumentElement().getNodeName());
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    // Vulnerability 7: Insecure Deserialization
    //可以使用白名单防护，但较为麻烦，且实际不会有直接反序列化的生产环境，建议注释掉
    @PostMapping("/deserialize")
    public Map<String, String> deserialize(@RequestBody byte[] data) {
        Map<String, String> result = new HashMap<>();
        try {
            // Insecure deserialization vulnerability
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(data);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);

            Object obj = ois.readObject();
            result.put("status", "deserialized");
            result.put("type", obj.getClass().getName());
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    // Vulnerability 8: Missing Authentication
    //加管理员认证（@PreAuthorize("hasRole('ADMIN')）
    @DeleteMapping("/user/{id}")
    public Map<String, String> deleteUser(@PathVariable String id) {
        Map<String, String> result = new HashMap<>();
        // No authentication check - anyone can delete users
        result.put("message", "User " + id + " deleted successfully");
        result.put("status", "success");
        return result;
    }

    // Vulnerability 9: Server-Side Request Forgery (SSRF)
    @GetMapping("/fetch")
    public Map<String, String> fetchUrl(@RequestParam String url) {
        Map<String, String> result = new HashMap<>();
        try {
            // SSRF vulnerability - no validation of URL
            //java.net.URL targetUrl = new java.net.URL(url);
            //BufferedReader reader = new BufferedReader(
            //    new InputStreamReader(targetUrl.openStream())
            //);
            java.net.URL targetUrl = new java.net.URL(url);
            String host = targetUrl.getHost();

            // 1. 白名单：只允许固定的域名
            List<String> allowedHosts = Arrays.asList("api.example.com", "cdn.example.com", "www.baidu.com");
            if (!allowedHosts.contains(host)) {
                result.put("error", "Access denied: host not allowed");
                return result;
            }

            // 2. 禁止访问内网 IP
            if (host.startsWith("192.168.") || host.startsWith("10.") ||
                    host.startsWith("127.") || host.startsWith("172.16.") ||
                    host.equals("localhost") || host.equals("169.254.169.254")) {
                result.put("error", "Access denied: internal IP not allowed");
                return result;
            }

            // 3. 只允许 HTTP/HTTPS
            String protocol = targetUrl.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                result.put("error", "Access denied: protocol not allowed");
                return result;
            }

            // 安全请求
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(targetUrl.openStream())
            );
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            result.put("content", content.toString());
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    // Vulnerability 10: Weak Cryptography
    @Value("${encryption.key}")
    private String base64Key;  // Spring 自动注入配置值

    @PostMapping("/encrypt")
    public Map<String, String> encrypt(@RequestParam String data) {
        Map<String, String> result = new HashMap<>();
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            javax.crypto.spec.SecretKeySpec keySpec =
                    new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");

            // 生成随机 IV
            byte[] iv = new byte[16];
            java.security.SecureRandom random = new java.security.SecureRandom();
            random.nextBytes(iv);
            javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);

            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());

            // 组合 IV + 密文
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            result.put("encrypted", java.util.Base64.getEncoder().encodeToString(combined));

        }  catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/decrypt")
    public Map<String, String> decrypt(@RequestParam String encryptedData) {
        Map<String, String> result = new HashMap<>();
        try {
            // 1. 将 Base64 字符串解码为字节数组
            byte[] combined = java.util.Base64.getDecoder().decode(encryptedData);

            // 2. 提取 IV（前 16 字节）
            byte[] iv = new byte[16];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            // 3. 提取真正的密文（剩下的部分）
            byte[] encrypted = new byte[combined.length - iv.length];
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            // 4. 解码密钥（和加密时完全一样）
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Key);
            javax.crypto.spec.SecretKeySpec keySpec =
                    new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

            // 5. 初始化 Cipher 为解密模式
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 6. 执行解密
            byte[] decrypted = cipher.doFinal(encrypted);

            // 7. 将解密后的字节转为字符串返回
            result.put("decrypted", new String(decrypted));

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

}



// Made with Bob
