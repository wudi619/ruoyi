package com.ruoyi.socket.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bussiness.domain.TContractCoin;
import com.ruoyi.bussiness.domain.TCurrencySymbol;
import com.ruoyi.bussiness.domain.TSecondCoinConfig;
import com.ruoyi.bussiness.service.ITContractCoinService;
import com.ruoyi.bussiness.service.ITCurrencySymbolService;
import com.ruoyi.bussiness.service.ITSecondCoinConfigService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.socket.manager.WebSocketUserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
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

/**
 * energy 能源
 */
@Slf4j
@EnableScheduling
@Component
public class MarketThreadEnergy {

    @Resource
    private ITSecondCoinConfigService secondCoinConfigService;
    @Resource
    private ITContractCoinService contractCoinService;
    @Resource
    private ITCurrencySymbolService tCurrencySymbolService;
    @Resource
    private WebSocketUserManager webSocketUserManager;

    private static final Set<String> coins = new HashSet<>();

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void getSecondCoins() {
        //秒合约Crude原油
        TSecondCoinConfig tSecondCoinConfig = new TSecondCoinConfig();
        tSecondCoinConfig.setMarket("energy");
        tSecondCoinConfig.setStatus(1L);
        List<TSecondCoinConfig> tSecondCoinConfigs = secondCoinConfigService.selectTSecondCoinConfigList(tSecondCoinConfig);
        for (TSecondCoinConfig secondCoinConfig : tSecondCoinConfigs) {
            coins.add(secondCoinConfig.getCoin().toUpperCase());
        }
        //U本位
        TContractCoin tContractCoin = new TContractCoin();
        tContractCoin.setEnable(0L);
        tContractCoin.setMarket("energy");
        List<TContractCoin> tContractCoins = contractCoinService.selectTContractCoinList(tContractCoin);
        for (TContractCoin contractCoin : tContractCoins) {
            coins.add(contractCoin.getCoin().toUpperCase());
        }
        //币币
        TCurrencySymbol tCurrencySymbol = new TCurrencySymbol();
        tCurrencySymbol.setEnable("1");
        tCurrencySymbol.setMarket("energy");
        List<TCurrencySymbol> tCurrencySymbols = tCurrencySymbolService.selectTCurrencySymbolList(tCurrencySymbol);
        for (TCurrencySymbol currencySymbol : tCurrencySymbols) {
            coins.add(currencySymbol.getCoin().toUpperCase());
        }
        log.info(JSONObject.toJSONString(coins));
    }

    @Async
    @Scheduled(cron = "*/15 * * * * ?")
    public void marketThreadRun() throws URISyntaxException {
        if (isOffDay()) {
            log.info("星期六星期日 不查");
            return;
        }
        for (String coinStr : coins) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Random random = new Random();
                    double v = random.nextDouble();
                    String body = "{ \"trace\": \"culpa cillum ea reprehenderit occaecat\", \"data\": { \"code\": \""+ coinStr.toUpperCase() +"\", \"kline_type\": 1, \"kline_timestamp_end\": 0, \"query_kline_num\": 1, \"adjust_type\": 0 } }";
                    String url = "https://quote.tradeswitcher.com/quote-b-api/kline?token=*&query="+ URLEncoder.encode(body);

                    String result = HttpRequest.get(url)
                            .timeout(20000)
                            .execute().body();
                    com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(result);
                    com.alibaba.fastjson.JSONObject data = com.alibaba.fastjson.JSONObject.parseObject(jsonObject.get("data").toString());
                    JSONArray kline_list = com.alibaba.fastjson.JSONObject.parseArray(data.get("kline_list").toString());
                    com.alibaba.fastjson.JSONObject listJSONObject = kline_list.getJSONObject(0);
                    com.alibaba.fastjson.JSONObject jsonObjectNew = new com.alibaba.fastjson.JSONObject();
                    jsonObjectNew.put("t",listJSONObject.get("timestamp"));
                    jsonObjectNew.put("h",listJSONObject.get("high_price"));
                    jsonObjectNew.put("l",listJSONObject.get("low_price"));
                    jsonObjectNew.put("o",listJSONObject.get("open_price"));
                    jsonObjectNew.put("c",listJSONObject.get("close_price"));
                    result = jsonObjectNew.toJSONString();
                    if (StringUtils.isNotEmpty(result) && result.length() > 100) {
                        webSocketUserManager.crudeKlineSendMeg(result, coinStr);
                        webSocketUserManager.metalDETAILSendMeg(result, coinStr);
                    }
                }
            });
            thread.start();
        }
    }

    private boolean isOffDay() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            log.info("星期六星期日 不查");
            return true;
        }
        return false;
    }

}
