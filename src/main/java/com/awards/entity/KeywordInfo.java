package com.awards.entity;

/**
 * @author mike_yi
 * @version 1.0
 * @since 2017-11-10
 */
public class KeywordInfo {
    /**
     * 相关关键字
     */
    String keyword;

    /**
     * 总的搜索次数
     */
    String relateSercheNum;

    /**
     * 关键字搜索竞争度
     */
    String competition;

    /**
     * 关键字总点费用
     */
//    double totalCost;

    /**
     * 关键字总点击次数 近一年
     */
    String averageMoney;

    /**
     * 指定地理位置的搜索次数
     */
    String lastMonthSearchNum;

    /**
     * 每次点击费用
     */
//    double clicksCost;

    /**
     * 类型
     */
//    String type;


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getRelateSercheNum() {
        return relateSercheNum;
    }

    public void setRelateSercheNum(String relateSercheNum) {
        this.relateSercheNum = relateSercheNum;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public String getAverageMoney() {
        return averageMoney;
    }

    public void setAverageMoney(String averageMoney) {
        this.averageMoney = averageMoney;
    }

    public String getLastMonthSearchNum() {
        return lastMonthSearchNum;
    }

    public void setLastMonthSearchNum(String lastMonthSearchNum) {
        this.lastMonthSearchNum = lastMonthSearchNum;
    }


    @Override
    public String toString() {
        return "KeywordInfo{" +
                "keyword='" + keyword + '\'' +
                ", relateSercheNum='" + relateSercheNum + '\'' +
                ", competition='" + competition + '\'' +
                ", averageMoney='" + averageMoney + '\'' +
                ", lastMonthSearchNum='" + lastMonthSearchNum + '\'' +
                '}';
    }
}
