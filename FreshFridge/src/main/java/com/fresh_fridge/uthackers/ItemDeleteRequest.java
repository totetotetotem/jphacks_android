package com.fresh_fridge.uthackers;

class ItemDeleteRequest {
    private Integer[] user_item_id;

    ItemDeleteRequest(Integer[] ids) {
        user_item_id = ids;
    }
}
