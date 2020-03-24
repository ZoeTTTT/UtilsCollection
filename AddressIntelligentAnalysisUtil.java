package com.iredfish.club.util;

import com.iredfish.club.Constant;
import com.iredfish.club.model.City;
import com.iredfish.club.model.County;
import com.iredfish.club.model.Province;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class AddressIntelligentAnalysisUtil {

    private static final String PUNCTUATION =
        "[`\\\\~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%…&*（）——+|{}【】‘；：”“’。，、？]";
    private static final String BLANK = " ";
    private static final String PHONE_NUMBER_REGEX = "([1][3-9][\\d]{9})|(0\\d{2,4}-\\d{7,8})";
    private static final String AUTONOMOUS_REGION = "自治区";
    private static final String GUANGXI_AUTONOMOUS_REGION = "壮族自治区";
    private static final String AUTONOMOUS_PREFECTURE = "自治州";
    private static final String AUTONOMOUS_COUNTY = "自治县";
    private static final String PROVINCE = "省";
    private static final String CITY = "市";
    private static final String COUNTY = "县";
    private static final String REGION = "区";

    public static Map<String, String> discernAddressInfo(String addressInfo) {
        Map<String, String> returnMap = new HashMap<>();

        Province currentProvince = null;
        City currentCity = null;

        String regionAreaValue, nameValue = "", phoneValue, detailAddressValue = "";
        String provinceName = "", cityName = "", countyName = "";

        String provinceKeyWord = "";

        addressInfo = addressInfo.replaceAll(PUNCTUATION, BLANK);
        addressInfo.trim();

        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);
        Matcher matcher = pattern.matcher(addressInfo);
        StringBuffer bf = new StringBuffer(64);
        while (matcher.find()) {
            bf.append(matcher.group());
        }
        phoneValue = bf.toString();
        if (!StringUtils.isEmpty(phoneValue)) {
            addressInfo = addressInfo.replace(phoneValue, "");
            addressInfo.trim();
        }

        List<Province> proList =
            (List<Province>) SharePreferencesUtil.getInstance().getObject(Constant.SP_KEY_PROVINCE);
        for (Province provinceObj : proList) {
            String province = provinceObj.getName();
            if (province.contains(AUTONOMOUS_REGION)) {
                provinceKeyWord = province.replace(AUTONOMOUS_REGION, "");
            } else if (province.contains(PROVINCE)) {
                provinceKeyWord = province.replace(PROVINCE, "");
            } else if (province.contains(GUANGXI_AUTONOMOUS_REGION)) {
                provinceKeyWord = province.replace(GUANGXI_AUTONOMOUS_REGION, "");
            } else if (province.contains(CITY)) {
                provinceKeyWord = province.replace(CITY, "");
            } else {
                provinceKeyWord = province;
            }
            if (addressInfo.contains(provinceKeyWord)) {
                provinceName = province;
                currentProvince = provinceObj;
                break;
            }
        }

        if (!StringUtils.isEmpty(provinceName)) {
            List<City> cityList = ((Map<String, List<City>>) SharePreferencesUtil.getInstance()
                .getObject(Constant.SP_KEY_CITY)).get(currentProvince.getUid());
            if (cityList.size() == 1) {
                String city = cityList.get(0).getName();
                cityName = city;
                currentCity = cityList.get(0);
            } else {
                for (City cityObj : cityList) {
                    String cityKeyWord;
                    String city = cityObj.getName();
                    if (city.contains(AUTONOMOUS_PREFECTURE)) {
                        cityKeyWord = city.replace(AUTONOMOUS_PREFECTURE, "");
                    } else if (city.contains(CITY)) {
                        cityKeyWord = city.replace(CITY, "");
                    } else {
                        cityKeyWord = city;
                    }
                    if (addressInfo.contains(cityKeyWord)) {
                        cityName = city;
                        currentCity = cityObj;
                        break;
                    }
                }
            }
        }

        if (!StringUtils.isEmpty(cityName)) {
            List<County> areaList = ((Map<String, List<County>>) SharePreferencesUtil.getInstance()
                .getObject(Constant.SP_KEY_COUNTY)).get(currentCity.getUid());
            for (County countyObj : areaList) {
                String countyKeyWord;
                String county = countyObj.getName();
                if (county.contains(AUTONOMOUS_COUNTY)) {
                    countyKeyWord = county.replace(AUTONOMOUS_COUNTY, "");
                } else if (county.contains(REGION)) {
                    countyKeyWord = county.replace(REGION, "");
                } else if (county.contains(COUNTY)) {
                    countyKeyWord = county.replace(COUNTY, "");
                } else {
                    countyKeyWord = county;
                }
                if (addressInfo.contains(countyKeyWord)) {
                    countyName = county;
                    break;
                }
            }
        }

        regionAreaValue = provinceName + cityName + countyName;
        int regionIndex = addressInfo.indexOf(countyName);

        String[] spStringArray = addressInfo.split("\\s+");
        if (spStringArray.length >= 1) {
            for (String spStr : spStringArray) {
                if (spStr.length() > 4 && !spStr.contains(provinceKeyWord)) {
                    detailAddressValue = spStr;
                } else if (spStr.length() <= 4) {
                    nameValue = spStr;
                } else {
                    if (regionIndex < spStr.length()) {
                        detailAddressValue = spStr.substring(regionIndex, spStr.length());
                    } else {
                        detailAddressValue = "";
                    }
                }
            }
        }

        returnMap.put(Constant.MAP_KEY_PHONE_NUMBER, phoneValue);
        returnMap.put(Constant.MAP_KEY_NAME, nameValue);
        returnMap.put(Constant.MAP_KEY_REGION_AREA, regionAreaValue);
        returnMap.put(Constant.MAP_KEY_DETAIL_ADDRESS, detailAddressValue);

        return returnMap;
    }
}
