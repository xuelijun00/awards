package com.awards.service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.api.ads.adwords.axis.v201806.cm.*;
import com.google.api.ads.adwords.axis.v201806.o.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.awards.entity.KeywordInfo;
import com.google.api.ads.adwords.axis.factory.AdWordsServices;

import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.factory.AdWordsServicesInterface;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.ads.common.lib.utils.Maps;
import com.google.api.client.auth.oauth2.Credential;

/**
 * @author mike_yi
 * @since 2017-11-24
 * @deprecated 谷歌关键词搜索指数服务配置
 */
@Deprecated
@Service
public class AdwardsService {
    private Logger logger = LoggerFactory.getLogger(AdwardsService.class);


    /**
     * @param
     * @param keyword
     * @return
     * @deprecated 刷新refreshtoken 运行 adwords-axis-3.9.0 refreshtoken 获取
     */
    @Deprecated
    public List<KeywordInfo> getKeywordInfo(long locationId, String keyword) {
        return startTargetIdeaing(locationId, keyword);
    }


    public List<KeywordInfo> startTargetIdeaing(long locationId, String keyword) {
        List<KeywordInfo> keywordInfoList = new ArrayList<KeywordInfo>();
        try {
            System.out.println("start=========");
            Credential oAuth2Credenial = new OfflineCredentials.Builder().forApi(Api.ADWORDS).fromFile().build()
                    .generateCredential();
            AdWordsSession session = new AdWordsSession.Builder().fromFile().withOAuth2Credential(oAuth2Credenial)
                    .build();
            AdWordsServicesInterface adWordsServices = AdWordsServices.getInstance();
            TargetingIdeaServiceInterface targetingIdeaService = adWordsServices.get(session,
                    TargetingIdeaServiceInterface.class);
            // 设置投放网络
            NetworkSearchParameter networkSearchParameter = new NetworkSearchParameter();
            NetworkSetting networkSetting = new NetworkSetting();
            networkSetting.setTargetGoogleSearch(true);
            networkSetting.setTargetSearchNetwork(false);
            networkSetting.setTargetContentNetwork(false);
            networkSetting.setTargetPartnerSearchNetwork(false);
            networkSearchParameter.setNetworkSetting(networkSetting);
            // 设置投放关键字
            RelatedToQuerySearchParameter relatedToQuerySearchParameter = new RelatedToQuerySearchParameter();
            relatedToQuerySearchParameter.setQueries(new String[]{keyword});
            // 设置投放国家
            LocationSearchParameter locationSearchParameter = new LocationSearchParameter();
            Location targetLocation = new Location();
            targetLocation.setId(locationId);

            locationSearchParameter.setLocations(new Location[]{targetLocation});
            // 设置搜索结果：1、起始页，2、总条数(按业务需求默认配置300)
            Paging paging = new Paging();
            paging.setStartIndex(1);
            paging.setNumberResults(300);

            TargetingIdeaSelector selector = new TargetingIdeaSelector();
            selector.setRequestType(RequestType.IDEAS);
            selector.setIdeaType(IdeaType.KEYWORD);
            selector.setRequestedAttributeTypes(new AttributeType[]{AttributeType.AVERAGE_CPC,
                    AttributeType.COMPETITION, AttributeType.KEYWORD_TEXT, AttributeType.SEARCH_VOLUME,
                    AttributeType.TARGETED_MONTHLY_SEARCHES, AttributeType.CATEGORY_PRODUCTS_AND_SERVICES});
            selector.setSearchParameters(
                    new SearchParameter[]{networkSearchParameter, relatedToQuerySearchParameter, locationSearchParameter});
            selector.setPaging(paging);
            System.out.println("========================执行");
            TargetingIdeaPage page = targetingIdeaService.get(selector);
            System.out.println("============================没问题");
            for (TargetingIdea targetingIdea : page.getEntries()) {
                KeywordInfo keywordInfo = new KeywordInfo();
                Map<AttributeType, Attribute> data = Maps.toMap(targetingIdea.getData());
                // 相关关键字
                StringAttribute keywords = (StringAttribute) data.get(AttributeType.KEYWORD_TEXT);
                keywordInfo.setKeyword(keywords.getValue());
                System.out.println("key=========>" + keywords.getValue());
                // 相关关键字搜索时给定的提示数量
                Long relateSercheNum = ((LongAttribute) data.get(AttributeType.SEARCH_VOLUME)).getValue();
                keywordInfo.setRelateSercheNum(relateSercheNum + "");
//                System.out.println("搜索数量=====" + relateSercheNum);
                // 表示关键字搜索的竞争度
                try {
                    DoubleAttribute competition = (DoubleAttribute) data.get(AttributeType.COMPETITION);
                    keywordInfo.setCompetition(competition.getValue().toString());
                } catch (NullPointerException e) {
                    keywordInfo.setCompetition("");
                }
                // 关键字支付的平均每次点击费用
                try {
                    MoneyAttribute cpc = (MoneyAttribute) data.get(AttributeType.AVERAGE_CPC);
                    keywordInfo.setAverageMoney((double)cpc.getValue().getMicroAmount() / 1000000 + "");
//                    System.out.println("费用======" + cpc.getValue().getMicroAmount());
                } catch (NullPointerException e) {
                    keywordInfo.setAverageMoney("");
                }
                try {
                    // 过去12个月）的搜索次数。
                    MonthlySearchVolumeAttribute MonthlyVolume = (MonthlySearchVolumeAttribute) data
                            .get(AttributeType.TARGETED_MONTHLY_SEARCHES);
                    keywordInfo.setLastMonthSearchNum(MonthlyVolume.getValue(1).getCount() + "");

//                    System.out.println("搜索时间====" + MonthlyVolume.getValue(1).getCount());
                } catch (NullPointerException e) {
                    keywordInfo.setLastMonthSearchNum("");
                }

//                estimateKeywordTraffic.runExample(session, keywordInfo, locationId);
                keywordInfoList.add(keywordInfo);
            }
        } catch (OAuthException e) {
            logger.error("授权错误", e);
        } catch (ValidationException e) {
            logger.error("===", e);
        } catch (ConfigurationLoadException e) {
            logger.error("加载配置错误", e);
        } catch (ApiException e) {
            logger.error("api error =======>" + e);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return keywordInfoList;
    }
}
