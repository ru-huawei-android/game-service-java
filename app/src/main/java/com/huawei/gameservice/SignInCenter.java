package com.huawei.gameservice;

import com.huawei.hms.support.hwid.result.AuthHuaweiId;

class SignInCenter {

    private static SignInCenter INS = new SignInCenter();

    private static AuthHuaweiId currentAuthHuaweiId;

    static SignInCenter get() {
        return INS;
    }

    void updateAuthHuaweiId(AuthHuaweiId AuthHuaweiId) {
        currentAuthHuaweiId = AuthHuaweiId;
    }

    AuthHuaweiId getAuthHuaweiId() {
        return currentAuthHuaweiId;
    }
}
