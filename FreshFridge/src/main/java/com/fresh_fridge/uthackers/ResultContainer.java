package com.fresh_fridge.uthackers;

import com.google.gson.annotations.Expose;

class ResultContainer {
    @Expose
    private Meta meta;

    Meta getMeta() {
        return meta;
    }
}
