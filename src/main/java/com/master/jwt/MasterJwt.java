package com.master.jwt;

import com.master.utils.ZipUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class MasterJwt implements Serializable {
    public static final String DELIM = "\\|";
    public static final String EMPTY_STRING = "<>";
    private Long tokenId;

    private Long accountId = -1L;
    private Long restaurantId = -1L;
    private String kind = EMPTY_STRING;//token kind
    private String permission = EMPTY_STRING;
    private Long deviceId = -1L;// id cua thiet bi, lưu ở table device để get firebase url..
    private String posId = EMPTY_STRING;
    private Integer userKind = -1; //loại user là admin hay là gì
    private String username = EMPTY_STRING;// username hoac order code
    private String fullName = EMPTY_STRING;
    private Integer tabletKind = -1;
    private Long orderId = -1L;
    private Boolean isSuperAdmin = false;
    private String tenantId = EMPTY_STRING;

    public String toClaim() {
        if (deviceId == null) {
            deviceId = -1L;
        }
        if (userKind == null) {
            userKind = -1;
        }
        if (username == null) {
            username = EMPTY_STRING;
        }
        if (tabletKind == null) {
            tabletKind = -1;
        }
        if (orderId == null) {
            orderId = -1L;
        }
        return ZipUtils.zipString(accountId + DELIM + restaurantId + DELIM + kind + DELIM + permission + DELIM + deviceId + DELIM + userKind + DELIM + username + DELIM + tabletKind + DELIM + orderId + DELIM + isSuperAdmin + DELIM + tenantId);
    }

    public static MasterJwt decode(String input) {
        MasterJwt result = null;
        try {
            String[] items = ZipUtils.unzipString(input).split(DELIM, 13);
            if (items.length >= 12) {
                result = new MasterJwt();
                result.setAccountId(parserLong(items[0]));
                result.setRestaurantId(parserLong(items[1]));
                result.setKind(checkString(items[2]));
                result.setPermission(checkString(items[3]));
                result.setDeviceId(parserLong(items[4]));
                result.setPosId(checkString(items[5]));
                result.setUserKind(parserInt(items[6]));
                result.setUsername(checkString(items[7]));
                result.setFullName(checkString(items[8]));
                result.setTabletKind(parserInt(items[9]));
                result.setOrderId(parserLong(items[10]));
                result.setIsSuperAdmin(checkBoolean(items[11]));
                if (items.length > 12) {
                    result.setTenantId(checkString(items[12]));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private static Long parserLong(String input) {
        try {
            Long out = Long.parseLong(input);
            if (out > 0) {
                return out;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static Integer parserInt(String input) {
        try {
            Integer out = Integer.parseInt(input);
            if (out > 0) {
                return out;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static String checkString(String input) {
        if (!input.equals(EMPTY_STRING)) {
            return input;
        }
        return null;
    }

    private static Boolean checkBoolean(String input) {
        try {
            return Boolean.parseBoolean(input);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
