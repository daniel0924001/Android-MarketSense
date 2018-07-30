package com.idroi.marketsense.datasource;

import com.idroi.marketsense.data.StatisticDataItem;

import java.util.ArrayList;

/**
 * Created by daniel.hsieh on 2018/7/24.
 */

public class CriticalStatisticDataSource {
    public static ArrayList<StatisticDataItem> getStockStatisticDataList() {
        ArrayList<StatisticDataItem> list = new ArrayList<>();

        // Fundamental Analysis
        list.add(
                new StatisticDataItem
                        .Builder("基本面", null)
                        .type(StatisticDataItem.TYPE_TITLE)
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("%s 營收", "vGET_MONEY")
                        .prefixKeyName("vGET_MONEY_DATE")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("%s 毛利率%% (累計/單季)", "vFLD_PROFIT")
                        .prefixKeyName("vFLD_PRCQ_YMD")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("%s EPS (累計/單季)", "vFLD_EPS")
                        .prefixKeyName("vFLD_PRCQ_YMD")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("本益比", "vFLD_PER")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("每股淨值", "vSTK_VALUE")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("股價淨值比", "vFLD_PBR")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("%s ROE", "vFLD_ROE")
                        .prefixKeyName("vFLD_PRCQ_YMD")
                        .build()
        );

        // Technical Analysis
        list.add(
                new StatisticDataItem
                        .Builder("技術面", null)
                        .type(StatisticDataItem.TYPE_TITLE)
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("計算日期", "vFLD_YMD")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("近一週成交量", "vFLD_TXN_VOLUME")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("近一週股價表現", "vFLD_CLOSE_WEEK")
                        .colorful(true)
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("近一個月股價表現", "vFLD_CLOSE_MONTH")
                        .colorful(true)
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("近三個月股價表現", "vFLD_CLOSE_SEASON")
                        .colorful(true)
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("K 值", "vFLD_K9_UPDNRATE")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("D 值", "vFLD_D9_UPDNRATE")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("MACD 值", "vMACD")
                        .build()
        );

        // Chips Analysis
        list.add(
                new StatisticDataItem
                        .Builder("籌碼面 (三大法人進出 (張))", null)
                        .type(StatisticDataItem.TYPE_TITLE)
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("日期", null)
                        .type(StatisticDataItem.TYPE_5_COLUMNS)
                        .values("外資", "投信", "自營商", "合計")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("%s", null)
                        .prefixKeyName("vFLD_YMD1")
                        .colorful(true)
                        .type(StatisticDataItem.TYPE_5_COLUMNS)
                        .keyNames("vFLD_FRN_AMT1", "vFLD_ITH_AMT1", "vFLD_DLR_AMT1", "vFLD_TOT_AMT1")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("%s", null)
                        .prefixKeyName("vFLD_YMD2")
                        .colorful(true)
                        .type(StatisticDataItem.TYPE_5_COLUMNS)
                        .keyNames("vFLD_FRN_AMT2", "vFLD_ITH_AMT2", "vFLD_DLR_AMT2", "vFLD_TOT_AMT2")
                        .build()
        );
        list.add(
                new StatisticDataItem
                        .Builder("%s", null)
                        .prefixKeyName("vFLD_YMD3")
                        .colorful(true)
                        .type(StatisticDataItem.TYPE_5_COLUMNS)
                        .keyNames("vFLD_FRN_AMT3", "vFLD_ITH_AMT3", "vFLD_DLR_AMT3", "vFLD_TOT_AMT3")
                        .build()
        );

        return list;
    }
}
