package com.healix.util;

import com.healix.model.InsuranceRequest;
import java.util.ArrayList;
import java.util.List;

public class PatientValidator {

    public static List<String> validateInsurance(InsuranceRequest insurance) {
        List<String> missingFields = new ArrayList<>();

        if (insurance != null && Boolean.TRUE.equals(insurance.getHasInsurance())) {
            if (insurance.getInsuranceType() == null) {
                missingFields.add("insuranceType");
            }
            if (insurance.getPolicyCardNumber() == null || insurance.getPolicyCardNumber().trim().isEmpty()) {
                missingFields.add("policyCardNumber");
            }
        }

        return missingFields;
    }
}
