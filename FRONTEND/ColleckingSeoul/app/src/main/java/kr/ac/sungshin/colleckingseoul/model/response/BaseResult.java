package kr.ac.sungshin.colleckingseoul.model.response;

/**
 * Created by kakao on 2018. 1. 14..
 */

public class BaseResult {
    private String message;
    private String detail;

    public BaseResult(String message , String detail) {
        this.message = message;
        this.detail = detail;
    }

    public String getMessage() {
        return message;
    }
    public String getDetail() {return  detail;}
}
