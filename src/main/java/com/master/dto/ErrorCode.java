package com.master.dto;

public class ErrorCode {

    /**
     * Starting error code Account
     */
    public static final String ACCOUNT_ERROR_NOT_FOUND = "ERROR-ACCOUNT-0000";
    public static final String ACCOUNT_ERROR_USERNAME_EXISTED = "ERROR-ACCOUNT-0001";
    public static final String ACCOUNT_ERROR_EMAIL_EXISTED = "ERROR-ACCOUNT-0002";
    public static final String ACCOUNT_ERROR_WRONG_PASSWORD = "ERROR-ACCOUNT-0003";
    public static final String ACCOUNT_ERROR_NOT_ALLOW_DELETE_SUPPER_ADMIN = "ERROR-ACCOUNT-0004";
    public static final String ACCOUNT_ERROR_EXCEEDED_NUMBER_OF_INPUT_ATTEMPT_OTP = "ERROR-ACCOUNT-0005";
    public static final String ACCOUNT_ERROR_OTP_INVALID = "ERROR-ACCOUNT-0006";
    public static final String ACCOUNT_ERROR_LOGIN_FAILED = "ERROR-ACCOUNT-0007";
    public static final String ACCOUNT_ERROR_PRIVATE_KEY_INVALID = "ERROR-PRIVATE-KEY-INVALID";
    public static final String ACCOUNT_ERROR_PASSWORD_INVALID = "ERROR-ACCOUNT-0008";
    public static final String ACCOUNT_ERROR_NEW_PASSWORD_INVALID = "ERROR-ACCOUNT-0009";
    public static final String ACCOUNT_ERROR_BIRTHDATE_INVALID = "ERROR-ACCOUNT-0010";
    public static final String ACCOUNT_ERROR_PHONE_EXISTED = "ERROR-ACCOUNT-0011";
    public static final String ACCOUNT_ERROR_NOT_ALLOW_DELETE_YOURSELF = "ERROR-ACCOUNT-0012";
    public static final String ACCOUNT_ERROR_NOT_ALLOW_REQUEST_KEY = "ERROR-ACCOUNT-0013";
    public static final String ACCOUNT_ERROR_KIND_NOT_MATCH = "ERROR-ACCOUNT-0014";

    /**
     * Starting error code Group
     */
    public static final String GROUP_ERROR_NOT_FOUND = "ERROR-GROUP-0000";
    public static final String GROUP_ERROR_NAME_EXISTED = "ERROR-GROUP-0001";
    public static final String GROUP_ERROR_NOT_ALLOW_UPDATE = "ERROR-GROUP-0002";
    public static final String GROUP_ERROR_NOT_ALLOW_DELETE = "ERROR-GROUP-0003";
    public static final String GROUP_ERROR_NOT_ALLOW_DELETE_SYSTEM_ROLE = "ERROR-GROUP-0004";

    /**
     * Starting error code Permission
     */
    public static final String PERMISSION_ERROR_NAME_EXISTED = "ERROR-PERMISSION-0000";
    public static final String PERMISSION_ERROR_PERMISSION_CODE_EXISTED = "ERROR-PERMISSION-0001";

    /**
     * Starting error code Setting
     */
    public static final String SETTING_ERROR_NOT_FOUND = "ERROR-SETTING-0000";
    public static final String SETTING_ERROR_EXISTED_GROUP_NAME_AND_KEY_NAME = "ERROR-SETTING-0001";

    /**
     * Starting error code Server Provider
     */
    public static final String SERVER_PROVIDER_ERROR_NOT_FOUND = "ERROR-SERVER-PROVIDER-0000";
    public static final String SERVER_PROVIDER_ERROR_URL_EXISTED = "ERROR-SERVER-PROVIDER-0001";
    public static final String SERVER_PROVIDER_ERROR_NOT_ALLOW_DELETE = "ERROR-SERVER-PROVIDER-0002";
    public static final String SERVER_PROVIDER_ERROR_NOT_ALLOW_UPDATE = "ERROR-SERVER-PROVIDER-0003";

    /**
     * Starting error code Db Config
     */
    public static final String DB_CONFIG_ERROR_NOT_FOUND = "ERROR-DB-CONFIG-0000";
    public static final String DB_CONFIG_ERROR_LOCATION_EXISTED = "ERROR-DB-CONFIG-0001";
    public static final String DB_CONFIG_ERROR_USERNAME_EXISTED = "ERROR-DB-CONFIG-0002";
    public static final String DB_CONFIG_ERROR_URL_EXISTED = "ERROR-DB-CONFIG-0003";
    public static final String DB_CONFIG_ERROR_CREATE_DATABASE = "ERROR-DB-CONFIG-0004";
    public static final String DB_CONFIG_ERROR_DELETE_DATABASE = "ERROR-DB-CONFIG-0005";
    public static final String DB_CONFIG_ERROR_REACHED_LIMIT = "ERROR-DB-CONFIG-0006";

    /**
     * Starting error code Customer
     */
    public static final String CUSTOMER_ERROR_NOT_FOUND = "ERROR-CUSTOMER-0000";
    public static final String CUSTOMER_ERROR_NOT_ALLOW_DELETE = "ERROR-CUSTOMER-0001";

    /**
     * Starting error code Location
     */
    public static final String LOCATION_ERROR_NOT_FOUND = "ERROR-LOCATION-0000";
    public static final String LOCATION_ERROR_TENANT_ID_EXISTED = "ERROR-LOCATION-0001";
    public static final String LOCATION_ERROR_NAME_EXISTED = "ERROR-LOCATION-0002";
}
