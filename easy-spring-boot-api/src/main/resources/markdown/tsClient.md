# JavaScript客户端调用API使用方法
> easy-api-client项目用于快速接入服务端api框架easy-spring-boot-api，其基于axios，可用于web或者node环境。提供了ApiClient工具，自动对api加签、验签、加解密等等功能，在保障服务器api安全的前提下，能做到业务无感知，简化前后端开发人员的联调工作。
## 1. 安装依赖
  `npm install easy-api-client`
  
  或者

  `yarn add easy-api-client`

## 2. 接口调用使用示例
```ts
import {ApiClient, newApiClientInstance, RequestType} from "easy-api-client";
let client: ApiClient = newApiClientInstance({
    baseUrl: "http://localhost:8080/server/",
    channelId: "web",
    appId: "test-demo",
    secret: "NKVNcuwwEF56c22A",
    locale: "zh_CN",
    apiSuffix: ".do"
});
client.request({
    apiName: "/demo/pay",
    type: RequestType.post,
    needSignResult: true,
    needSign: true
}, {
    appId: client.options.appId,
    orderId: "easy_api_client001",
    description: "just a test!",
    currency: "CNY",
    totalAmount: 1,
    originalAmount: 5,
    userId: "U008958",
    userNickname: "小明"
}).then((response: any) => {
    console.log(response.data);
}, (reason: any) => {
    console.log(reason);
});
```
## 3. 接口调用结果
```text
  {
    code: 0,
    data: {
      outTradeNo: '202403112208576967291582',
      orderId: 'easy_api_client001',
      redirectUrl: 'https://www.xxx.com/h5.html'
    },
    sign: '3DB9F78499E8F31D3800FA3415009BE3D19EBA19',
    timestamp: 1710418049919
  }
```