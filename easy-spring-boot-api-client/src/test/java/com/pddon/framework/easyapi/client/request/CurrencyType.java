package com.pddon.framework.easyapi.client.request;

/**
 * @ClassName: CurrencyType
 * @Description: 币种类型
 * 标价币种，该参数的值为支付时传入的trans_currency，支持英镑：GBP、港币：HKD、美元：USD、新加坡元：SGD、日元：JPY、加拿大元：CAD、澳元：AUD、欧元：EUR、新西兰元：NZD、韩元：KRW、泰铢：THB、瑞士法郎：CHF、瑞典克朗：SEK、丹麦克朗：DKK、挪威克朗：NOK、马来西亚林吉特：MYR、印尼卢比：IDR、菲律宾比索：PHP、毛里求斯卢比：MUR、以色列新谢克尔：ILS、斯里兰卡卢比：LKR、俄罗斯卢布：RUB、阿联酋迪拉姆：AED、捷克克朗：CZK、南非兰特：ZAR、人民币：CNY、新台币：TWD。当trans_currency 和 settle_currency 不一致时，trans_currency支持人民币：CNY、新台币：TWD
 * @Author: Allen
 * @Date: 2024-02-28 17:17
 * @Addr: https://pddon.cn
 */
public enum CurrencyType {
    /**
     * 人民币
     */
    CNY,
    /**
     * 美元
     */
    USD,
    /**
     * 港币
     */
    HKD,
    /**
     * 日元
     */
    JPY,
    /**
     * 欧元
     */
    EUR
}
