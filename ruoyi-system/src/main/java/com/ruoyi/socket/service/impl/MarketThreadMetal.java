package com.ruoyi.socket.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.bussiness.domain.TContractCoin;
import com.ruoyi.bussiness.domain.TCurrencySymbol;
import com.ruoyi.bussiness.domain.TSecondCoinConfig;
import com.ruoyi.bussiness.service.ITContractCoinService;
import com.ruoyi.bussiness.service.ITCurrencySymbolService;
import com.ruoyi.bussiness.service.ITSecondCoinConfigService;
import com.ruoyi.bussiness.service.ITSymbolManageService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.socket.manager.WebSocketUserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


@Slf4j
@Component
public class MarketThreadMetal {


    @Resource
    private ITSecondCoinConfigService secondCoinConfigService;
    @Resource
    private ITContractCoinService contractCoinService;
    @Resource
    private ITCurrencySymbolService tCurrencySymbolService;
    @Resource
    private WebSocketUserManager webSocketUserManager;
    @Async
    @Scheduled(cron = "*/15 * * * * ?")
    public void marketThreadRun() throws URISyntaxException {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        Set<String> strings = new HashSet<>();
        //秒合约
        TSecondCoinConfig tSecondCoinConfig = new TSecondCoinConfig();
        tSecondCoinConfig.setMarket("metal");
        tSecondCoinConfig.setStatus(1L);
        List<TSecondCoinConfig> tSecondCoinConfigs = secondCoinConfigService.selectTSecondCoinConfigList(tSecondCoinConfig);
        for (TSecondCoinConfig secondCoinConfig : tSecondCoinConfigs) {
            strings.add(secondCoinConfig.getCoin().toUpperCase());
        }
        //U本位
        TContractCoin tContractCoin =new TContractCoin();
        tContractCoin.setEnable(0L);
        tContractCoin.setMarket("metal");
        List<TContractCoin> tContractCoins = contractCoinService.selectTContractCoinList(tContractCoin);
        for (TContractCoin contractCoin : tContractCoins) {
            strings.add(contractCoin.getCoin().toUpperCase());
        }
        //币币
        TCurrencySymbol tCurrencySymbol = new TCurrencySymbol();
        tCurrencySymbol.setEnable("1");
        tCurrencySymbol.setMarket("metal");
        List<TCurrencySymbol> tCurrencySymbols = tCurrencySymbolService.selectTCurrencySymbolList(tCurrencySymbol);
        for (TCurrencySymbol currencySymbol : tCurrencySymbols) {
            strings.add(currencySymbol.getCoin().toUpperCase());
        }
        //兑换
        for (String string : strings) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    double v = random.nextDouble();
                    String body = "{ \"trace\": \"culpa cillum ea reprehenderit occaecat\", \"data\": { \"code\": \""+ string.toUpperCase() +"\", \"kline_type\": 1, \"kline_timestamp_end\": 0, \"query_kline_num\": 1, \"adjust_type\": 0 } }";
                    String url = "https://quote.tradeswitcher.com/quote-b-api/kline?token=*&query="+ URLEncoder.encode(body);

                    String result = HttpRequest.get(url)
                            .timeout(20000)
                            .execute().body();
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    JSONObject data = JSONObject.parseObject(jsonObject.get("data").toString());
                    JSONArray kline_list = JSONObject.parseArray(data.get("kline_list").toString());
                    JSONObject listJSONObject = kline_list.getJSONObject(0);
                    JSONObject jsonObjectNew = new JSONObject();
                    jsonObjectNew.put("t",listJSONObject.get("timestamp"));
                    jsonObjectNew.put("h",listJSONObject.get("high_price"));
                    jsonObjectNew.put("l",listJSONObject.get("low_price"));
                    jsonObjectNew.put("o",listJSONObject.get("open_price"));
                    jsonObjectNew.put("c",listJSONObject.get("close_price"));
                    result = jsonObjectNew.toJSONString();
                    webSocketUserManager.metalKlineSendMeg(result,string);
                    webSocketUserManager.metalDETAILSendMeg(result,string);
                }
            });
            thread.start();
        }
    }





}
