package com.awards.api;

import java.rmi.RemoteException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.awards.service.EstimateKeywordTraffic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.awards.entity.KeywordInfo;
import com.awards.service.AdwardsService;
import com.google.api.ads.adwords.axis.v201710.cm.ApiException;
import com.google.api.ads.common.lib.conf.ConfigurationLoadException;
import com.google.api.ads.common.lib.exception.OAuthException;
import com.google.api.ads.common.lib.exception.ValidationException;

/**
 * @author mike_yi
 * @version 1.0
 * @since 2017-11-10
 */
@SuppressWarnings("deprecation")
@RestController
@RequestMapping(value = "/awards")
public class AdwardsApi {
    @Autowired
    private AdwardsService adwardsService;

    @Autowired
    private EstimateKeywordTraffic estimateKeywordTraffic;

    @RequestMapping(value = "/keywordInfo", method = RequestMethod.POST)
    public List<KeywordInfo> selectKeywordInfo(HttpServletRequest req) throws ApiException, OAuthException, RemoteException, ValidationException, ConfigurationLoadException {
        String location = req.getParameter("location");
        long locationId = Long.parseLong(location);
        String keyword = req.getParameter("keyword");
        System.out.println(location + keyword);
        List<KeywordInfo> keywordInfoList = adwardsService.getKeywordInfo(locationId, keyword);
        if (keywordInfoList == null) {
            return null;
        }
        return keywordInfoList;
    }

    @RequestMapping(value = "/keywordTraffic", method = RequestMethod.POST)
    public KeywordInfo getKeywordInfo(HttpServletRequest req) throws OAuthException, RemoteException, ValidationException, ConfigurationLoadException {
        String location = req.getParameter("location");
        long locationId = Long.parseLong(location);
        String keyword = req.getParameter("keyword");
        System.out.println(location + keyword);
        KeywordInfo keywordInfoList = estimateKeywordTraffic.runExample(keyword, locationId);
        if (keywordInfoList == null) {
            return null;
        }
        return keywordInfoList;
    }
}
