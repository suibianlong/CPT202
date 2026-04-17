package com.example.cpt202heritage.util;

import com.example.cpt202heritage.entity.Resource;
import com.example.cpt202heritage.enums.ResourceStatusEnum;
import com.example.cpt202heritage.exception.AppException;

// resource status verification
public final class ResourceStatusValidator {

    private ResourceStatusValidator() {
    }

    public static void assertEditable(Resource resource) {
        if (resource == null) {
            throw new AppException(404, "Resource does not exist.");
        }

        String status = resource.getStatus();
        if (!ResourceStatusEnum.DRAFT.getValue().equals(status)
                && !ResourceStatusEnum.REJECTED.getValue().equals(status)) {
            throw new AppException(409, "Current resource status does not allow editing.");
        }
    }

    public static void assertSubmittable(Resource resource) {
        if (resource == null) {
            throw new AppException(404, "Resource does not exist.");
        }

        String status = resource.getStatus();
        if (!ResourceStatusEnum.DRAFT.getValue().equals(status)
                && !ResourceStatusEnum.REJECTED.getValue().equals(status)) {
            throw new AppException(409, "Current resource status does not allow submission.");
        }
    }
}
