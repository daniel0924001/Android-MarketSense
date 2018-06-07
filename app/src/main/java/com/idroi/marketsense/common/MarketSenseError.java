package com.idroi.marketsense.common;

/**
 * Created by daniel.hsieh on 2018/4/18.
 */

public enum MarketSenseError {
    OK(0, "OK."),
    UNSPECIFIC_ERROR(1000, "Unspecific error."),
    NETWORK_CONNECTION_FAILED(1001, "Network connection failed."),
    NETWORK_VOLLEY_ERROR(1002, "Volley error."),
    JSON_PARSED_ERROR(1003, "JSON response parsed error."),
    IMAGE_DOWNLOAD_FAILURE(1004, "Image download failure."),
    NETWORK_CONNECTION_TIMEOUT(1005, "Network connection timeout"),
    JSON_PARSED_NO_DATA(1006, "JSON response null");

    private final int mCode;
    private final String mMessage;

    MarketSenseError(int code, String message) {
        mCode = code;
        mMessage = message;
    }

    public static MarketSenseError fromErrorCode(String stringCode) {
        int code = Integer.valueOf(stringCode);
        for(MarketSenseError error : MarketSenseError.values()){
            if(error.getCode() == code){
                return error;
            }
        }
        return UNSPECIFIC_ERROR;
    }

    public int getCode(){
        return mCode;
    }

    public String getMessage(){
        return mMessage;
    }

    public boolean isOK(){
        return mCode == OK.mCode;
    }

    @Override
    public String toString() {
        return mMessage;
    }
}
