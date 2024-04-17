# 接口加签验签流程与算法说明
> 为了保障服务器的api安全，EasyApi提供了对接口请求、响应数据、回调通知数据等等内容进行加签验签操作。主要有如下几种场景需要加签验签：
>
> * 在启用了请求加签功能的接口调用之前，我们需要对请求参数按如下规则生成数字签名sign，并将sign和当前调用时间的时间戳timestamp字段通过系统参数的方式一并上传到服务器，服务器会按相同的算法对请求数据进行加签，然后得到serverSign值，通过serverSign与sign进行判等比对，如果不一致，则说明请求数据在网络传递的过程有被恶意篡改，及时阻止业务功能的执行，并返回错误。
> * 在启用了响应加签功能的接口调用返回之后，服务器会在响应的系统参数中增加数字签名sign字段和timestamp字段，接口调用方接收到返回数据后，同样需要对服务器返回的数据进行数字签名校验，算法规则与服务器端一致。
> * 客户端应用在接收服务器异步通知时，跟接收接口调用响应信息验签流程一致。

### 1、签名算法

API调用过程中传递的数据的安全性可以利用数字签名机制防止传递过程中被恶意非法篡改，数字签名信息的生成步骤如下。

#### 1.1 第一步

设所有发送或者接收到的数据为集合M，将集合M内非空参数值的参数按照参数名ASCII码从小到大排序（字典序），使用URL键值对的格式（即key1value1key2value2…）拼接成字符串paramsContent。 

特别注意以下重要规则： 

1. ◆ 参数名ASCII码从小到大排序（字典序）； 
2. ◆ 如果参数的值为空不参与签名； 
3. ◆ 接口调用请求或响应中的系统参数均不参与签名；
4. ◆ 参数名区分大小写； 
5. ◆ 接口开启了验签后，需要对接口上传的所有需要验签的业务参数进行签名，将签名结果通过sign参数传递，且需要当前请求接口时间的timestamp时间戳信息，timestamp需要参与加签过程，还可以通过时间戳进行接口防重放攻击；
6. ◆ 回调通知返回签名sign时，将生成的签名与接收到的sign值作校验；
7. ◆ 接口可能增加字段，验证签名时必须支持增加的扩展字段 ；

#### 1.2 第二步

在paramsContent的首尾两端依次拼接上timestamp和秘钥secretKey得到signContent字符串，并对signContent进行SHA-1运算，再将得到的字符串所有字符转换为大写，得到sign值signValue。 注意：密钥secretKey的长度为32个字节。

◆ secretKey获取途径：通过向接口服务提供方申请开通API应用，您将会得到应用相关信息，信息中包含secretKey。

#### 1.3 示例

下面我们以调用POST类型的API接口请求数据加签过程作为示例。

默认可用的系统参数如下，其他自定义系统参数或者重命名后的系统参数以实际应用内定义为准：

| 参数名称    | 参数说明                         | 请求类型   | 是否必须 | 数据类型 | schema |
| ----------- | -------------------------------- | ---------- | -------- | -------- | ------ |
| appId       | 应用ID                           | query/body | false    | string   |        |
| channelId   | 渠道ID，一个渠道可能包含多个应用 | query/body | false    | string   |        |
| clientId    | 终端ID，用来唯一标识用户访问终端 | query/body | false    | string   |        |
| clientIp    | 终端IP                           | query/body | false    | string   |        |
| countryCode | 国家码                           | query/body | false    | string   |        |
| currency    | 货币类型                         | query/body | false    | string   |        |
| locale      | 语言类型，默认 zh_CN             | query/body | false    | string   |        |
| repeatCode  | 防重复提交码                     | query/body | false    | string   |        |
| sessionId   | 登录用户会话ID                   | query/body | false    | string   |        |
| sign        | 数字签名                         | query/body | false    | string   |        |
| timeZone    | 时区                             | query/body | false    | string   |        |
| timestamp   | 调用接口时的时间戳               | query/body | false    | Long     |        |
| userId      | 用户ID                           | query/body | false    | string   |        |
| versionCode | 客户端版本号                     | query/body | false    | Integer  |        |

假设传送的参数如下： 

``` json
{
  "appId" : "pddon-payment-demo", //系统参数
  "userId" : "Ued9c6825c5c851ecdafcbbdf24534a3a", //系统参数
  "currency" : "CNY", //系统参数
  "totalAmount" : 1,  
  "description" : "请我喝杯饮料！",
  "userNickname" : "游客",
  "orderId" : "202404101615191350",
  "returnPageUrl" : "http://localhost:8088/payment-demo/payResult.html?orderId=202404101615191350"
}
```

##### 1.3.1 第一步

 对参数按照keyvalue的格式，并按照参数名ASCII字典序排序如下： 

paramsContent=`"description请我喝杯饮料！orderId202404101615191350returnPageUrlhttp://localhost:8088/payment-demo/payResult.html?orderId=202404101615191350totalAmount1userNickname游客"`; 

##### 1.3.2 第二步

拼接时间戳和API密钥： 

SHA-1签名方式：

timestamp=`"1712736928277"`;//当前时间戳

secretKey=`"NKVNcuwwEF3sc22A"`; //当前应用secretKey

signContent=`secretKey+timestamp+paramsContent+timestamp+secretKey`; 

得到值如下：

signContent=`NKVNcuwwEF3sc22A1712736928277description请我喝杯饮料！orderId202404101615191350returnPageUrlhttp://localhost:8088/payment-demo/payResult.html?orderId=202404101615191350totalAmount1userNickname游客1712736928277NKVNcuwwEF3sc22A`;

##### 1.3.3 生成数字签名

对signContent进行SHA1签名

sign=`SHA1(signContent).toUpperCase()`;

得到如下结果：

sign=`"B44A68B18FF7FF84FA720EC5286916F89CD3CE29"`;

得到最终发送到服务器的数据如下： 

```json
{
  "appId" : "pddon-payment-demo", //系统参数
  "userId" : "Ued9c6825c5c851ecdafcbbdf24534a3a", //系统参数
  "currency" : "CNY", //系统参数
  "sign" : "B44A68B18FF7FF84FA720EC5286916F89CD3CE29", //系统参数
  "timestamp" : "1712736928277", //系统参数
  "totalAmount" : 1,  
  "description" : "请我喝杯饮料！",
  "userNickname" : "游客",
  "orderId" : "202404101615191350",
  "returnPageUrl" : "http://localhost:8088/payment-demo/payResult.html?orderId=202404101615191350"
}
```
### 2、客户端sdk开源代码示例

* java版SDK

  [https://github.com/pddon/easy-spring-boot-api/tree/main/easy-spring-boot-api-client](https://github.com/pddon/easy-spring-boot-api/)

* typescript版SDK

  [https://github.com/pddon/easy-api-client](https://github.com/pddon/easy-api-client)