##接口文档：
https://shimo.im/docx/gRDdDW93KJwP3jY8
##项目需求列表：
 https://shimo.im/sheets/XWXWYQTqTghQxvtd/tF2WQ 
##需求文档V1.0：
https://shimo.im/docs/H8XJh9RPyyjQqPkp
##需求更新文档：
 https://shimo.im/docs/hHrkxKYGXwg6WdPH 
##原型：
 https://d33ajr.axshare.com/ 
##对接文件：
 https://shimo.im/docs/8jhTyQCcXyT9JXvP/ 

##域名映射
192.168.201.29
海外的访问：hk-dispatcher.valsun.cn
内网的访问：lan-dispatcher.valsun.cn


# https生成ssl连接

### 生成服务端密钥文件 server.jks
keytool -genkey -alias server -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore server.jks -dname CN=server,OU=Test,O=pkslow,L=Guangzhou,C=CN -validity 36500 -storepass 123456 -keypass 123456

### 导出服务端的cert文件
keytool -export -alias server -file server.cer -keystore server.jks

### 生成客户端的密钥文件 client.jks
keytool -genkey -alias client -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore client.jks -dname CN=client,OU=Test,O=pkslow,L=Guangzhou,C=CN -validity 36500 -storepass 123456 -keypass 123456

### 导出客户端的cert文件
keytool -export -alias client -file client.cer -keystore client.jks

### 把客户端的cert导入到服务端
keytool -import -alias client -file client.cer -keystore server.jks

### 把服务端的cert导入到客户端
keytool -import -alias server -file server.cer -keystore client.jks

### 检验服务端是否具有自己的private key和客户端的cert
keytool -list -keystore server.jks

### 转换JKS格式为P12
keytool -importkeystore -srckeystore client.jks -destkeystore client.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass 123456 -deststorepass 123456 -srckeypass 123456 -destkeypass 123456 -srcalias client -destalias client -noprompt
keytool -importkeystore -srckeystore server.jks -destkeystore server.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass 123456 -deststorepass 123456 -srckeypass 123456 -destkeypass 123456 -srcalias server -destalias server -noprompt

### 生成私钥pem
openssl pkcs12 -in server.p12 -nocerts -out server-private-key.pem

### 生成公钥pem
openssl pkcs12 -in client.p12 -clcerts -nokeys -out client-public-cert.pem