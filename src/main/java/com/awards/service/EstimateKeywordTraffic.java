package com.awards.service;

/**
 * Created by Administrator on 2018/7/9 0009.
 */

import com.awards.entity.KeywordInfo;
import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.axis.v201806.cm.*;
import com.google.api.ads.adwords.axis.v201806.o.*;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.adwords.lib.factory.AdWordsServicesInterface;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;
import com.google.api.client.auth.oauth2.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


/**
 * This example gets keyword traffic estimates.
 * <p>
 * <p>Credentials and properties in {@code fromFile()} are pulled from the
 * "ads.properties" file. See README for more info.
 */
@Service
public class EstimateKeywordTraffic {

    private static Logger logger = LoggerFactory.getLogger(EstimateKeywordTraffic.class);

    /**
     * Runs the example.
     *
     * @throws ApiException    if the API request failed with one or more service errors.
     * @throws RemoteException if the API request failed due to other errors.
     */
    public KeywordInfo runExample(String word, long location) throws RemoteException {


        Credential oAuth2Credential = null;
        AdWordsSession session = null;
        try {
            oAuth2Credential = new OfflineCredentials.Builder().forApi(Api.ADWORDS).fromFile().build().generateCredential();
            session = new AdWordsSession.Builder().fromFile().withOAuth2Credential(oAuth2Credential).build();
        } catch (OAuthException e) {
            logger.error("授权错误", e);
//            e.printStackTrace();
        } catch (ValidationException e) {
            logger.error("===", e);
//            e.printStackTrace();
        } catch (ConfigurationLoadException e) {
            logger.error("加载配置错误", e);
//            e.printStackTrace();
        }

        AdWordsServicesInterface adWordsServices = AdWordsServices.getInstance();

        TrafficEstimatorServiceInterface trafficEstimatorService =
                adWordsServices.get(session, TrafficEstimatorServiceInterface.class);

        List<Keyword> keywords = new ArrayList<Keyword>();

        Keyword marsCruiseKeyword = new Keyword();
        marsCruiseKeyword.setText(word);
        marsCruiseKeyword.setMatchType(KeywordMatchType.BROAD);
        keywords.add(marsCruiseKeyword);


        // Create a keyword estimate request for each keyword.
        List<KeywordEstimateRequest> keywordEstimateRequests = new ArrayList<KeywordEstimateRequest>();
        for (Keyword keyword : keywords) {
            KeywordEstimateRequest keywordEstimateRequest = new KeywordEstimateRequest();
            keywordEstimateRequest.setKeyword(keyword);
            keywordEstimateRequests.add(keywordEstimateRequest);
        }

        // Add a negative keyword to the traffic estimate.
        KeywordEstimateRequest negativeKeywordEstimateRequest = new KeywordEstimateRequest();
        negativeKeywordEstimateRequest.setKeyword(new Keyword(null, null, null, word,
                KeywordMatchType.BROAD));
        negativeKeywordEstimateRequest.setIsNegative(true);
        keywordEstimateRequests.add(negativeKeywordEstimateRequest);

        // Create ad group estimate requests.
        List<AdGroupEstimateRequest> adGroupEstimateRequests = new ArrayList<AdGroupEstimateRequest>();
        AdGroupEstimateRequest adGroupEstimateRequest = new AdGroupEstimateRequest();
        adGroupEstimateRequest.setKeywordEstimateRequests(keywordEstimateRequests
                .toArray(new KeywordEstimateRequest[]{}));
        adGroupEstimateRequest.setMaxCpc(new Money(null, 1000000000L));
        adGroupEstimateRequests.add(adGroupEstimateRequest);

        // Create campaign estimate requests.
        List<CampaignEstimateRequest> campaignEstimateRequests =
                new ArrayList<CampaignEstimateRequest>();
        CampaignEstimateRequest campaignEstimateRequest = new CampaignEstimateRequest();
        campaignEstimateRequest.setAdGroupEstimateRequests(adGroupEstimateRequests
                .toArray(new AdGroupEstimateRequest[]{}));

        Location unitedStates = new Location();
        unitedStates.setId(location);
        campaignEstimateRequest.setCriteria(new Criterion[]{unitedStates});
        campaignEstimateRequests.add(campaignEstimateRequest);

        // Create selector.
        TrafficEstimatorSelector selector = new TrafficEstimatorSelector();
        selector.setCampaignEstimateRequests(campaignEstimateRequests
                .toArray(new CampaignEstimateRequest[]{}));

        // Optional: Request a list of campaign level estimates segmented by platform.
        selector.setPlatformEstimateRequested(true);

        // Get traffic estimates.
        TrafficEstimatorResult result = trafficEstimatorService.get(selector);

        // Display traffic estimates.
        if (result != null
                && result.getCampaignEstimates() != null
                && result.getCampaignEstimates().length > 0) {
            CampaignEstimate campaignEstimate = result.getCampaignEstimates()[0];


            // Display the keyword estimates.
            KeywordEstimate[] keywordEstimates =
                    campaignEstimate.getAdGroupEstimates()[0].getKeywordEstimates();
            for (int i = 0; i < keywordEstimates.length; i++) {
                if (Boolean.TRUE.equals(keywordEstimateRequests.get(i).getIsNegative())) {
                    continue;
                }

                Keyword keyword = keywordEstimateRequests.get(i).getKeyword();
                KeywordEstimate keywordEstimate = keywordEstimates[i];

                StatsEstimate keywordMin = keywordEstimate.getMin();
                StatsEstimate keywordMax = keywordEstimate.getMax();
                Double meanAverageCpc = calculateMean(keywordMin.getAverageCpc(),
                        keywordMax.getAverageCpc());
                Double meanClicks = calculateMean(keywordMin.getClicksPerDay(),
                        keywordMax.getClicksPerDay());
                Double meanTotalCost = calculateMean(keywordMin.getTotalCost(),
                        keywordMax.getTotalCost());

                meanClicks = new BigDecimal(meanClicks).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                meanAverageCpc= new BigDecimal(meanAverageCpc).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//                meanTotalCost= new BigDecimal(meanTotalCost).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                KeywordInfo keywordInfo = new KeywordInfo();

                keywordInfo.setAverageMoney(meanAverageCpc+"");
//                keywordInfo.setClicksCost(meanClicks);
//                keywordInfo.setTotalCost(meanTotalCost);
//                keywordInfo.setType(keyword.getMatchType().toString());
                return keywordInfo;
            }
        } else {
            System.out.println("No traffic estimates were returned.");
        }
        return null;
    }

    /**
     * Returns the mean of the {@code microAmount} of the two Money values if neither is null, else
     * returns null.
     */
    private static Double calculateMean(Money minMoney, Money maxMoney) {
        if (minMoney == null || maxMoney == null) {
            return null;
        }
        return calculateMean(minMoney.getMicroAmount(), maxMoney.getMicroAmount());
    }

    /**
     * Returns the mean of the two Number values if neither is null, else returns null.
     */
    private static Double calculateMean(Number min, Number max) {
        if (min == null || max == null) {
            return null;
        }
        return (min.doubleValue() + max.doubleValue()) / 2;
    }
}