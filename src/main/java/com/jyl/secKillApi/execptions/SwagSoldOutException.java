package com.jyl.secKillApi.execptions;

public class SwagSoldOutException extends RuntimeException {

    public SwagSoldOutException(String swagTitle) {
        super(swagTitle + " sold out.");
    }
}
