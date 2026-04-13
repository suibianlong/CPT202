package com.cpt202.module3.util;

import com.cpt202.module3.entity.Resource;
import com.cpt202.module3.enums.ResourceStatusEnum;
import com.cpt202.module3.exception.AppException;

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
