> 基于 Spring AOP 对开放接口进行签名认证

核心代码在 `sign` 文件夹内，默认使用的 `SHA256` 摘要算法，对应位置：`sign.aop.ApiSignValidAspect.sign()`。

备注：低配版方案，代码有很多不足之处，如有问题，望指正。


**提供一个前端对接口进行签名的工具类（TS版本）**

ApiSignUtil.ts

```typescript
/**
 * 使用文档
 * 1. 安装依赖： npm i crypto-js
 * 2. 引入工具： import { sign } from '。。/ApiSignUtil';
 * 3. 签名参数： const signResult = sign(appId, appSecret, {...参数对象});
 * 4. 打印结果：console.log('签名数据', signResult);
 */
import sha256 from 'crypto-js/sha256'

/**
 * 签名结果
 */
type SignResult = {
  sign: string
  timestamp: number
  nonce: string
}

/**
 * 对参数进行签名
 * @param appId
 * @param appSecret
 * @param params
 */
const sign = (appId: string, appSecret: string, params: any): SignResult => {
  // 当前秒级时间戳
  const timestamp = getSecondTimestamp()
  // 18位随机字符串
  const nonce = randomNonce()
  // 追加签名参数
  params.appId = appId
  params.timestamp = timestamp
  params.nonce = nonce
  // 对参数按ASCII进行排序
  const sortParams = sortParamsAscii(params)
  // 将参数组装成字符串，剔除空value的key
  let paramsStr = ''
  for (let key in sortParams) {
    const val = sortParams[key]
    if (val) paramsStr += `${key}${val}`
  }
  // 将secret拼接到最后
  paramsStr += appSecret
  // 签名
  const sign = sha256(paramsStr).toString().toUpperCase()
  return {
    sign: sign,
    timestamp: timestamp,
    nonce: nonce
  }
}

/**
 * 对参数进行 ASCII 进行排序
 * @param params
 */
const sortParamsAscii = (params: Object): Object => {
  const arr = new Array()
  let num = 0
  for (let i in params) {
    arr[num] = i
    num++
  }
  const sortArr = arr.sort()
  const sortParams = {}
  for (let i in sortArr) {
    sortParams[sortArr[i]] = params[sortArr[i]]
  }
  return sortParams
}

/**
 * 获取当前秒级时间戳
 */
const getSecondTimestamp = (): number => {
  return parseInt(String(new Date().getTime() / 1000), 10)
}

/**
 * 生成随机字符串
 * @param len,默认生成18位长度
 */
const randomNonce = (len: number = 18): string => {
  const str = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'
  let result = ''
  for (let i = len; i > 0; --i) result += str[Math.floor(Math.random() * str.length)]
  return result
}

export { sign }
```
